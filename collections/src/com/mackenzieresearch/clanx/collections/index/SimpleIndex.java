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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mackenzieresearch.clanx.collections.KVPair;
import com.mackenzieresearch.clanx.collections.RankedSet;
import com.mackenzieresearch.clanx.collections.queryable.Match;
import com.mackenzieresearch.clanx.collections.queryable.Query;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SimpleIndex is an in-memory HashMultimap() implementation of Index.
 *
 * @param <V>
 */
public class SimpleIndex<V> implements MutableIndex<V> {
  public static final int DEFAULT_RANKING = 1;

  private Multimap<String, KVPair<V>> _index = HashMultimap.create();
  private float _weight = 1.0f;


  public SimpleIndex() {
  }

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

    _index.put(key, new KVPair<V>(key, entry));
  }

  @Override
  public V getExactMatch(String key) {
    checkNotNull(key);

    Collection<KVPair<V>> results = _index.get(key);

    //  Go thru the close matches and return the first one that matches exactly...
    for (KVPair<V> result : results) {
      if (key.equals(result.getKey()))
        return result.getValue();
    }

    //  There was no exact match:
    return null;
  }

  /**
   * Should return the same result as getExactMatch(String key)...
   *
   * @param key
   * @return
   * @throws org.apache.commons.codec.EncoderException
   *
   */
  @Override
  public V getNearestMatch(String key) {
    checkNotNull(key);

    Collection<KVPair<V>> results = _index.get(key);

    if (results.size() > 0)
      return _rankMatches(key, results).firstEntry().getElement().getItem();
    else
      return null;
  }

  @Override
  public RankedSet<V> getExactMatches(String key) {
    checkNotNull(key);

    Collection<KVPair<V>> results = _index.get(key);

    RankedSet<V> resultSet = new RankedSet<V>();

    if (results.size() > 0) {
      for (KVPair<V> result : results)
        resultSet.add(DEFAULT_RANKING, result.getValue());
    }

    return resultSet;
  }

  /**
   * Return all results that exactly match the key
   *
   * @param key
   * @return
   * @throws org.apache.commons.codec.EncoderException
   *
   */
  @Override
  public RankedSet<V> getRankedMatches(String key) {
    checkNotNull(key);

    Collection<KVPair<V>> results = _index.get(key);

    if (results.size() > 0)
      return _rankMatches(key, results);
    else
      return new RankedSet<V>();
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

  private RankedSet<V> _rankMatches(String key, Collection<KVPair<V>> matches) {
    RankedSet<V> results = new RankedSet<V>();

    for (KVPair<V> match : matches)
      results.add(DEFAULT_RANKING, match.getValue());

    return results;
  }
}
