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

package com.mackenzieresearch.clanx.collections.index;

import com.mackenzieresearch.clanx.collections.KVPair;
import com.mackenzieresearch.clanx.collections.RankedSet;
import com.mackenzieresearch.clanx.collections.queryable.Match;
import com.mackenzieresearch.clanx.collections.queryable.Query;
import com.mackenzieresearch.clanx.collections.queryable.Queryable;
import com.mackenzieresearch.clanx.metrix.Levenshtein;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Note that this is not an efficient implementation of a fuzzy index.  It is used as a placeholder for a more
 * sophisticated index mechanism (such as Lucene) that is capable of efficiently performing approximate matches
 * on a large set of keys...
 *
 * @param <V>
 */
public class LevenshsteinFuzzydex<V> implements MutableIndex<V> {
  public static final int DEFAULT_RANKING = 1;

  private List<KVPair<V>> _index = new ArrayList<KVPair<V>>();
  private float _weight = 1.0f;
  private int _tolerance = 6;


  public void setWeight(float weight) {
    _weight = weight;
  }

  @Override
  public float getWeight() {
    return _weight;
  }

  @Override
  public void addEntry(String key, V entry) {
    checkNotNull(key);
    checkNotNull(entry);

    _index.add(kv(key, entry));
  }

  @Override
  public V getExactMatch(String key) {
    checkNotNull(key);

    for (KVPair<V> kvp : _index) {
      if (kvp.getKey().equals(key))
        return kvp.getMeasure();
    }

    return null;
  }

  @Override
  public V getNearestMatch(String key) {
    checkNotNull(key);

    int bestMatch = Integer.MAX_VALUE;
    V matchingEntity = null;

    for (KVPair<V> kvp : _index) {
      int match = Levenshtein.scanLine(kvp.getKey(), key);
      if (match < bestMatch) {
        bestMatch = match;
        matchingEntity = kvp.getMeasure();
      }
    }

    return matchingEntity;
  }

  @Override
  public RankedSet<V> getExactMatches(String key) {
    checkNotNull(key);

    RankedSet<V> results = new RankedSet<V>();

    for (KVPair<V> kvp : _index) {
      if (kvp.getKey().equals(key))
        results.add(DEFAULT_RANKING, kvp.getMeasure());
    }

    return results;
  }

  @Override
  public RankedSet<V> getRankedMatches(String key) {
    checkNotNull(key);

    RankedSet<V> results = new RankedSet<V>();

    for (KVPair<V> kvp : _index) {
      int match = Levenshtein.scanLine(kvp.getKey(), key);
      if (match <= _tolerance)
        results.add(match, kvp.getMeasure());
    }

    return results;
  }

  public void setTolerance(int tolerance) {
    _tolerance = tolerance;
  }

  public RankedSet<V> getRankedMatchesWithinTolerance(String key, int tolerance) {
    checkNotNull(key);
    checkArgument(tolerance >= 0);

    RankedSet<V> results = new RankedSet<V>();

    for (KVPair<V> kvp : _index) {
      int match = Levenshtein.scanLine(kvp.getKey(), key);
      if (match <= tolerance)
        results.add(match, kvp.getMeasure());
    }

    return results;
  }

  @Override
  public RankedSet<V> getExactMatches(Query query) {
    if (query instanceof Match)
      return getExactMatches(((Match<String>) query).value());
    else
      throw new IllegalArgumentException("Expected a Match query.  Actually got: " + query.getClass().getSimpleName());
  }

  @Override
  public RankedSet<V> getNearestMatches(Query query) {
    if (query instanceof Match) {
      V result = getNearestMatch(((Match<String>) query).value());
      RankedSet<V> resultSet = new RankedSet<V>();
      resultSet.add(DEFAULT_RANKING, result);
      return resultSet;
    } else
      throw new IllegalArgumentException("Expected a Match query.  Actually got: " + query.getClass().getSimpleName());
  }

  @Override
  public RankedSet<V> getRankedMatches(float scoreThreshold, Query query) {
    if (query instanceof Match)
      return getRankedMatches(((Match<String>) query).value());
    else
      throw new IllegalArgumentException("Expected a Match query.  Actually got: " + query.getClass().getSimpleName());
  }


  private KVPair<V> kv(String key, V value) {
    checkNotNull(key);
    checkNotNull(value);

    return new KVPair<V>(key, value);
  }
}
