/*

   CLANX, A foundation library for java application development.
   Version 0.2

   Copyright 2011-2012 Kenneth R. Mackenzie (www.mackenzieresearch.com)

   This program is free software: you can redistribute it and/or modify
   it under the terms of Version 3 of the GNU Affero General Public
   License as published by the Free Software Foundation.

   Unless required by applicable law or agreed to in writing, this
   software is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   If you require a version of this software which can be used as
   part of a commercial for-profit program, please contact Mackenzie
   Research for a commercial license.

   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mackenzieresearch.clanx.collections.multidex;

import com.mackenzieresearch.clanx.collections.RankedSet;
import com.mackenzieresearch.clanx.collections.index.Index;
import com.mackenzieresearch.clanx.collections.index.LevenshsteinFuzzydex;
import com.mackenzieresearch.clanx.collections.index.MutableIndex;
import com.mackenzieresearch.clanx.collections.queryable.Match;
import com.mackenzieresearch.clanx.collections.queryable.NAryQuery;
import com.mackenzieresearch.clanx.collections.queryable.Query;
import com.mackenzieresearch.clanx.collections.queryable.UnaryQuery;
import com.mackenzieresearch.clanx.entity.Attribute;
import org.apache.commons.codec.EncoderException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class SimpleMultidimensionalFuzzydex<T extends Comparable> implements MutableMultidex<T> {
  private TreeSet<T> _measureDimension = new TreeSet<T>();
  private Map<String, MutableIndex<T>> _dimensions = new HashMap<String, MutableIndex<T>>();

  /**
   * Invoke this to save memory and speed up performance by discarding the measure dimension.
   * This will disable validation checks that seek to insure that there are no duplication of
   * facts and that a fact must exist in order for an index member referring to it to be defined.
   */
  @Override
  public void disableMeasureDimensionValidation() {
    _measureDimension = null;
  }

  @Override
  public void addIndexDimension(String dimension) {
    checkNotNull(dimension);

    _dimensions.put(dimension, new LevenshsteinFuzzydex<T>());
  }

  @Override
  public void addIndexDimension(String name, MutableIndex<T> dimension) {
    checkNotNull(name);
    checkNotNull(dimension);
    _dimensions.put(name, dimension);
  }

  @Override
  public Index<T> getIndex(String name) {
    checkNotNull(name);
    return _dimensions.get(name);
  }

  @Override
  public void addFact(T fact, Attribute<String>... attributes) throws EncoderException {
    checkNotNull(fact);
    checkNotNull(attributes);

    //  If measure dimension isn't disabled, make sure this fact doesn't already exist...
    if (_measureDimension != null) {
      checkState(!_measureDimension.contains(fact));
      _measureDimension.add(fact);
    }

    //  Add index members referring to this fact (aliases) to the appropriate index dimensions...
    for (Attribute<String> attribute : attributes) {
      checkNotNull(attribute);
      MutableIndex<T> dimension = _dimensions.get(attribute.name());
      checkNotNull(dimension, "Attribute " + attribute.name() + " refers to a dimension that doesn't exist.");
      dimension.addEntry(attribute.value(), fact);
    }
  }

  @Override
  public void addIndexMembersForExistingFact(T fact, Attribute<String>... attributes) throws EncoderException {
    checkNotNull(fact);
    checkNotNull(attributes);

    //  If measure dimension isn't disabled, make sure this fact already exists...
    if (_measureDimension != null)
      checkState(_measureDimension.contains(fact));

    //  Add index members referring to this fact (aliases) to the appropriate index dimensions...
    for (Attribute<String> attribute : attributes) {
      checkNotNull(attribute);
      MutableIndex<T> dimension = _dimensions.get(attribute.name());
      checkNotNull(dimension, "Attribute " + attribute.name() + " refers to a dimension that doesn't exist.");
      dimension.addEntry(attribute.value(), fact);
    }
  }

  @Override
  public RankedSet<T> getExactMatches(Query query) {
    checkNotNull(query);

    RankedSet<T> results = new RankedSet<T>();
    float weightOfLastDimension = 1.0f;


    NAryQuery cquery = null;
    if (query instanceof NAryQuery)
      cquery = (NAryQuery) query;
    else if (query instanceof Match)
      cquery = new NAryQuery((UnaryQuery) query);
    else
      throw new IllegalArgumentException("Expected a Match or Compound query.  Actually got: " + query.getClass().getSimpleName());

    for (UnaryQuery subquery : cquery) {
      Index<T> dimension = _dimensions.get(((Match) subquery).name());
      checkNotNull(dimension);
      RankedSet<T> partialResults = dimension.getExactMatches(subquery);
      if (results.size() == 0) {
        results = partialResults;
        weightOfLastDimension = dimension.getWeight();
      } else {
        results = results.weightedInsideJoin(partialResults, weightOfLastDimension, dimension.getWeight());
        weightOfLastDimension = dimension.getWeight();
      }
    }

    return results;
  }

  /**
   * returns a RankedSet that is the INNER JOIN of the sets of partial matches found for each
   * attribute.  The returned set contains (L^R) to be precise.  The resulting ranking is the weighted
   * average of the rankings from each of the partial matches, weighted by the weight assigned to each
   * dimension (default dimensional weighting is 1.0)...
   *
   * @param query
   * @return
   */
  @Override
  public RankedSet<T> getNearestMatches(Query query) {
    checkNotNull(query);

    RankedSet<T> results = new RankedSet<T>();
    float weightOfLastDimension = 1.0f;


    NAryQuery cquery = null;
    if (query instanceof NAryQuery)
      cquery = (NAryQuery) query;
    else if (query instanceof Match)
      cquery = new NAryQuery((UnaryQuery) query);
    else
      throw new IllegalArgumentException("Expected a Match or Compound query.  Actually got: " + query.getClass().getSimpleName());

    for (UnaryQuery subquery : cquery) {
      Index<T> dimension = _dimensions.get(((Match) subquery).name());
      checkNotNull(dimension);
      RankedSet<T> partialResults = dimension.getNearestMatches(subquery);
      if (results.size() == 0) {
        results = partialResults;
        weightOfLastDimension = dimension.getWeight();
      } else {
        results = results.weightedInsideJoin(partialResults, weightOfLastDimension, dimension.getWeight());
        weightOfLastDimension = dimension.getWeight();
      }
    }

    return results;
  }

  /**
   * returns a RankedSet that is the OUTER JOIN DISTINCT of the sets of partial matches found for each
   * attribute.  The resulting ranking is the weighted average of the rankings from each of the partial matches,
   * weighted by the weight assigned to each dimension (default dimensional weighting is 1.0).
   * <p/>
   * This will return partial matches that are missing data in one or more dimensions.  The missing dimensions are
   * reflected in the score by averaging in the weighted max (or min, depending on the ordering of the dimension) value
   * for the missing data.  The number of matches returned is governed by the scoreThreshold.  Note that if scoreThreshold
   * is set to the max (or min, in the case of descending ranked sets) this will result in a minimum of (L + R) rows
   * being returned. ((L - L&R) + L&R + (R - L&R) to be precise.)
   *
   * @param query
   * @return
   */
  @Override
  public RankedSet<T> getRankedMatches(float scoreThreshold, Query query) {
    checkNotNull(query);

    RankedSet<T> results = new RankedSet<T>();
    float weightOfLastDimension = 1.0f;

    NAryQuery cquery = null;
    if (query instanceof NAryQuery)
      cquery = (NAryQuery) query;
    else if (query instanceof Match)
      cquery = new NAryQuery((UnaryQuery) query);
    else
      throw new IllegalArgumentException("Expected a Match or Compound query.  Actually got: " + query.getClass().getSimpleName());

    for (UnaryQuery subquery : cquery) {
      Index<T> dimension = _dimensions.get(((Match) subquery).name());
      checkNotNull(dimension);
      RankedSet<T> partialResults = dimension.getRankedMatches(scoreThreshold, subquery);
      if (results.size() == 0) {
        results = partialResults;
        weightOfLastDimension = dimension.getWeight();
      } else {
        results = results.weightedLeftOuterJoin(partialResults, weightOfLastDimension, dimension.getWeight(), scoreThreshold);
        weightOfLastDimension = dimension.getWeight();
      }
    }

    return results;
  }

  public void dumpFacts() {
    for (T fact : _measureDimension) {
      System.out.println(fact.toString());
    }
  }
}
