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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ScoredItem<T> implements Comparable<ScoredItem<T>> {
  private float _score = 0;
  private T _entity = null;
  private float _threshold = 0.0004f;

  public ScoredItem(float score, T entity) {
    checkArgument(score >= 0);
    checkNotNull(entity);

    _score = score;
    _entity = entity;
  }

  public void setComparisonThreshold(float threshold) {
    checkArgument(threshold > 0, "Threshold should be some small value close to but greater than zero.");

    _threshold = threshold;
  }

  public float getScore() {
    return _score;
  }

  public void setScore(float score) {
    checkArgument(score >= 0);

    _score = score;
  }

  public T getItem() {
    return _entity;
  }

  public void setEntity(T entity) {
    checkNotNull(entity);

    _entity = entity;
  }

  @Override
  public int compareTo(ScoredItem<T> o) {
    if (Math.abs(_score - o.getScore()) < _threshold) {
      //  This finer-grained subcomparison is necessary to allow these objects to be put into
      //  multisets without being counted as duplicates of each other...
      if (_entity.equals(o._entity))
        return 0;
      else if (Comparable.class.isAssignableFrom(_entity.getClass()))
        return ((Comparable<T>)_entity).compareTo(o._entity);
      else
        return -1;
    }
    else if (_score < o.getScore())
      return -1;
    else return 1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ScoredItem that = (ScoredItem) o;

    return _entity.equals(that._entity);

  }

  @Override
  public int hashCode() {
    return _entity.hashCode();
  }
}
