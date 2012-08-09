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

import com.mackenzieresearch.clanx.collections.index.BucketedFuzzyIndex;
import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class BucketFuzzydexTest {
  private BucketedFuzzyIndex<String> _fuzzydex;

  @Before
  public void setUp() throws Exception {
    _fuzzydex = new BucketedFuzzyIndex<String>();
    _generateFuzzydexEntries();
  }

  @Test
  public void testSetWeight() throws Exception {
    _fuzzydex.setWeight(2.0f);
    assert(Math.abs(_fuzzydex.getWeight() - 2.0f) < 0.0001F);
  }

  @Test
  public void testGetExact() throws Exception {
    assert(_fuzzydex.getExactMatch("foxtrot").equals("foxtrot1"));
  }

  @Test
  public void testGetNearestMatch() throws Exception {
    assert(_fuzzydex.getNearestMatch("foxtrot").equals("foxtrot1"));
  }

  @Test
  public void testGetRankedMatches() throws Exception {
    RankedSet<String> results = _fuzzydex.getRankedMatches("baker");
    assert(results.size() == 1);
    assert(results.iterator().next().getItem().equals("baker1"));
    
    RankedSet<String> results2 = _fuzzydex.getRankedMatches("charlie");
    assert(results2.size() == 3);
    Iterator<ScoredItem<String>> iterator = results2.iterator();
    iterator.next().getItem().equals("charlie1");
    iterator.next().getItem().equals("charlie2");
    iterator.next().getItem().equals("charlie3");
  }

  private void _generateFuzzydexEntries() throws EncoderException {
    _fuzzydex.addEntry("alpha", "alpha1");
    _fuzzydex.addEntry("baker", "baker1");
    _fuzzydex.addEntry("charlie", "charlie1");
    _fuzzydex.addEntry("charlie1", "charlie2");
    _fuzzydex.addEntry("charlie2", "charlie3");
    _fuzzydex.addEntry("delta", "delta1");
    _fuzzydex.addEntry("eager", "eager1");
    _fuzzydex.addEntry("foxtrot", "foxtrot1");
    _fuzzydex.addEntry("epsilon", "epsilon1");
  }
}
