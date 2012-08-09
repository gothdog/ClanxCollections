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

package com.mackenzieresearch.clanx.collections;

import com.google.common.collect.BoundType;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RankedSet<T> extends AbstractCollection<ScoredItem<T>> implements Multiset<ScoredItem<T>>, SortedMultiset<ScoredItem<T>> {
  public enum Order {ASCENDING, DESCENDING}

  private static float MAX_SCORE = Float.MAX_VALUE;
  private static float MIN_SCORE = Float.MIN_VALUE;

  private final SortedMultiset<ScoredItem<T>> _elements;
  private final Order _order;

  public RankedSet() {
    _order = Order.ASCENDING;
    _elements = TreeMultiset.create();
  }

  public RankedSet(Order direction) {
    switch (direction) {
      case ASCENDING:
        _order = Order.ASCENDING;
        _elements = TreeMultiset.create();
        break;
      case DESCENDING:
        _order = Order.DESCENDING;
        _elements = TreeMultiset.<ScoredItem<T>>create().descendingMultiset();
        break;
      default:
        throw new IllegalArgumentException("Unrecognized value for ordering direction.  Use ASCENDING or DESCENDING");
    }
  }

  private RankedSet(Order order, SortedMultiset<ScoredItem<T>> elements) {
    _order = order;
    _elements = elements;
  }

  public Order getOrder() {
    return _order;
  }

  /**
   * Override to establish a different maximum score.
   */
  public float getMaxScore() {
    return MAX_SCORE;
  }

  /**
   * Override to establish a different minimum score.
   */
  public float getMinScore() {
    return MIN_SCORE;
  }

  public boolean add(float score, T entity) {
    checkArgument(score >= 0);
    checkNotNull(entity);

    return _elements.add(new ScoredItem<T>(score, entity));
  }

  @Override
  public Comparator<? super ScoredItem<T>> comparator() {
    return _elements.comparator();
  }

  @Override
  public Entry<ScoredItem<T>> firstEntry() {
    return _elements.firstEntry();
  }

  @Override
  public Entry<ScoredItem<T>> lastEntry() {
    return _elements.lastEntry();
  }

  @Override
  public Entry<ScoredItem<T>> pollFirstEntry() {
    return _elements.pollFirstEntry();
  }

  @Override
  public Entry<ScoredItem<T>> pollLastEntry() {
    return _elements.pollLastEntry();
  }

  @Override
  public int count(Object o) {
    return _elements.count(o);
  }

  @Override
  public int add(ScoredItem<T> tScoredItem, int i) {
    return _elements.add(tScoredItem, i);
  }

  @Override
  public int remove(Object o, int i) {
    return _elements.remove(o, i);
  }

  @Override
  public int setCount(ScoredItem<T> tScoredItem, int i) {
    return _elements.setCount(tScoredItem, i);
  }

  @Override
  public boolean setCount(ScoredItem<T> tScoredItem, int i, int i1) {
    return _elements.setCount(tScoredItem, i, i1);
  }

  @Override
  public SortedSet<ScoredItem<T>> elementSet() {
    return _elements.elementSet();
  }

  @Override
  public Set<Entry<ScoredItem<T>>> entrySet() {
    return _elements.entrySet();
  }

  @Override
  public Iterator<ScoredItem<T>> iterator() {
    return _elements.iterator();
  }

  @Override
  public SortedMultiset<ScoredItem<T>> descendingMultiset() {
    return new RankedSet<T>(Order.DESCENDING);
  }

  @Override
  public SortedMultiset<ScoredItem<T>> headMultiset(ScoredItem<T> tScoredItem, BoundType boundType) {
    return new RankedSet<T>(_order, _elements.headMultiset(tScoredItem, boundType));
  }

  @Override
  public SortedMultiset<ScoredItem<T>> subMultiset(ScoredItem<T> tScoredItem, BoundType boundType, ScoredItem<T> tScoredItem1, BoundType boundType1) {
    return new RankedSet<T>(_order, _elements.subMultiset(tScoredItem, boundType, tScoredItem1, boundType1));
  }

  @Override
  public SortedMultiset<ScoredItem<T>> tailMultiset(ScoredItem<T> tScoredItem, BoundType boundType) {
    return new RankedSet<T>(_order, _elements.tailMultiset(tScoredItem, boundType));
  }

  @Override
  public int size() {
    return _elements.size();
  }

  /**
   * Returns a RankedSet containing the INNER JOIN (only the rows that are in L && R)
   * of the two RankedEntitySets.  The ranking assigned to each entry will be the weighted average
   * of the rankings from the L and R entries.
   *
   * @param rSet
   * @param lWeight
   * @param rWeight
   * @return
   */
  public RankedSet<T> weightedInsideJoin(RankedSet<T> rSet, float lWeight, float rWeight) {
    checkNotNull(rSet);
    checkArgument(lWeight >= 0);
    checkArgument(rWeight >= 0);

    RankedSet<T> results = new RankedSet<T>();
    Map<ScoredItem<T>, ScoredItem<T>> joinIndex = new HashMap<ScoredItem<T>, ScoredItem<T>>();

    //  Create an index of all the righthand tuples so that we can do this join in O2n instead of On^2.
    //  We can be this cheezy because the RankedEntitySets are presumed to have unique tuples.  If they didn't,
    //  we'd have to promote <joinIndex> to be a chain-bucket hash...
    for (ScoredItem<T> rTuple : rSet)
      joinIndex.put(rTuple, rTuple);

    //  Now pass thru the lefthand tuples, doing index probes for each one to see if there is a match...
    for (ScoredItem<T> lTuple : _elements) {
      ScoredItem<T> probeResult = joinIndex.get(lTuple);
      if (probeResult != null)
        results.add((((lTuple.getScore() * lWeight) + (probeResult.getScore() * rWeight)) / 2), lTuple.getItem());
    }

    return results;
  }

  /**
   * Returns a RankedSet containing the LEFT OUTER JOIN (the rows that are in L || R || BOTH)
   * of the two RankedEntitySets.  The ranking assigned to each entry will be the weighted average
   * of the rankings from the L and R entries.
   *
   * @param rSet
   * @param lWeight
   * @param rWeight
   * @return
   */
  public RankedSet<T> weightedLeftOuterJoin(RankedSet<T> rSet, float lWeight, float rWeight, float threshold) {
    checkNotNull(rSet);
    checkArgument(lWeight >= 0);
    checkArgument(rWeight >= 0);
    checkArgument(threshold >= 0);

    RankedSet<T> results = new RankedSet<T>();
    Map<ScoredItem<T>, ScoredItem<T>> joinIndex = new HashMap<ScoredItem<T>, ScoredItem<T>>();

    //  Figure out the appropriate scoring value to supply if a tuple is missing on one side of the join or the other...
    float lMissingScore = getOrder() == Order.ASCENDING ? getMaxScore() : getMinScore();
    float rMissingScore = getOrder() == Order.ASCENDING ? getMaxScore() : getMinScore();

    //  Create an index of all the righthand tuples so that we can do this join in O2n instead of On^2.
    //  We can be this cheezy because the RankedEntitySets are presumed to have unique tuples.  If they didn't,
    //  we'd have to promote <joinIndex> to be a chain-bucket hash...
    for (ScoredItem<T> rTuple : rSet)
      joinIndex.put(rTuple, rTuple);

    //  Now pass thru the lefthand tuples, doing index probes for each one to see if there is a match...
    for (ScoredItem<T> lTuple : _elements) {
      float lTupleScore = (lTuple.getScore() * lWeight);

      //  See if this tuple is present in the righthand set.  If not, adjust score accordingly...
      ScoredItem<T> probeResult = joinIndex.get(lTuple);
      float rTupleScore = probeResult != null ? (probeResult.getScore() * rWeight) : (rMissingScore * rWeight);

      //  Add tuple to result set:
      float score = (lTupleScore + rTupleScore) / 2;
      if ((getOrder() == Order.ASCENDING && score <= threshold) || (getOrder() == Order.DESCENDING && score >= threshold))
        results.add(score, lTuple.getItem());

      //  if a join was found, drop it out of the join index so we don't include it when we do the R-only tuples...
      if (probeResult != null)
        joinIndex.remove(lTuple);
    }

    //  Now add all the tuples from the R side with no matching L tuples...
    for (ScoredItem<T> rTuple : joinIndex.values()) {
      float lTupleScore = lMissingScore * lWeight;
      float rTupleScore = rTuple.getScore() * rWeight;

      float score = (lTupleScore + rTupleScore) / 2;
      if ((getOrder() == Order.ASCENDING && score <= threshold) || (getOrder() == Order.DESCENDING && score >= threshold))
        results.add(score, rTuple.getItem());
    }

    return results;
  }
}
