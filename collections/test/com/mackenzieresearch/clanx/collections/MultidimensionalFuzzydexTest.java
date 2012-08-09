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
import com.mackenzieresearch.clanx.collections.multidex.SimpleMultidimensionalFuzzydex;
import com.mackenzieresearch.clanx.collections.queryable.NAryQuery;
import com.mackenzieresearch.clanx.entity.Attribute;
import com.mackenzieresearch.clanx.entity.GeneralAttribute;
import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class MultidimensionalFuzzydexTest {
  private Random random;
  private String chars = " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  @Before
  public void setUp() throws Exception {
    random = new Random(1024);

  }

  @Test
  public void randomGetNearestFactsPrecise() throws EncoderException {
    //  Create the indices...
    BucketedFuzzyIndex<String> index1 = new BucketedFuzzyIndex<String>();
    BucketedFuzzyIndex<String> index2 = new BucketedFuzzyIndex<String>();
    BucketedFuzzyIndex<String> index3 = new BucketedFuzzyIndex<String>();

    SimpleMultidimensionalFuzzydex<String> multiIndex = new SimpleMultidimensionalFuzzydex<String>();
    multiIndex.addIndexDimension("index0", index1);
    multiIndex.addIndexDimension("index1", index2);
    multiIndex.addIndexDimension("index2", index3);

    //  Populate the indices and the master table:
    Attribute<String>[][] master = _populateIndex(1000, multiIndex, 3);

    //  One time thru to check correctness (and warm up hotspot)...
    for (int probeCount = 1; probeCount < 1000; probeCount++) {
      int fact = random.nextInt(1000);
      Attribute<String>[] searchTerms = master[fact];
      RankedSet<String> results = multiIndex.getNearestMatches(new NAryQuery(searchTerms));
      assert (results.firstEntry().getElement().getItem().equals(Integer.toString(fact)));
    }

    //  Second time to get a performance number...
    long startTime = System.nanoTime();
    for (int probeCount = 1; probeCount < 1000; probeCount++) {
      int fact = random.nextInt(1000);
      Attribute<String>[] searchTerms = master[fact];
      RankedSet<String> results = multiIndex.getNearestMatches(new NAryQuery(searchTerms));
    }
    long elapsedTime = (System.nanoTime() - startTime);
    System.out.println("Elapsed nanos for 1000 probes of 3-index fuzzy multidimensional index: " + elapsedTime);
    System.out.println("For an average time of " + elapsedTime / 1000 + " nanos or " + (1000000000 / (elapsedTime / 1000)) + " per second");
  }

  @Test
  public void randomGetNearestFactsSloppy() throws EncoderException {
    //  Create the indices...
    BucketedFuzzyIndex<String> index1 = new BucketedFuzzyIndex<String>();
    BucketedFuzzyIndex<String> index2 = new BucketedFuzzyIndex<String>();
    BucketedFuzzyIndex<String> index3 = new BucketedFuzzyIndex<String>();

    SimpleMultidimensionalFuzzydex<String> multiIndex = new SimpleMultidimensionalFuzzydex<String>();
    multiIndex.addIndexDimension("index0", index1);
    multiIndex.addIndexDimension("index1", index2);
    multiIndex.addIndexDimension("index2", index3);

    //  Populate the indices and the master table:
    Attribute<String>[][] master = _populateIndex(100000, multiIndex, 3);

    //  One time thru to check correctness (and warm up hotspot)...
    for (int probeCount = 1; probeCount < 1000; probeCount++) {
      int fact = random.nextInt(100000);
      Attribute<String>[] searchTerms = master[fact];
      RankedSet<String> results = multiIndex.getRankedMatches(10, new NAryQuery(searchTerms));
      assert (results.firstEntry().getElement().getItem().equals(Integer.toString(fact)));
    }

    //  Second time to get a performance number...
    long startTime = System.nanoTime();
    for (int probeCount = 1; probeCount < 1000; probeCount++) {
      int fact = random.nextInt(100000);
      Attribute<String>[] searchTerms = master[fact];
      RankedSet<String> results = multiIndex.getRankedMatches(10, new NAryQuery(searchTerms));
    }
    long elapsedTime = (System.nanoTime() - startTime);
    System.out.println("Elapsed nanos for 1000 probes of 3-index fuzzy multidimensional index: " + elapsedTime);
    System.out.println("For an average time of " + elapsedTime / 1000 + " nanos or " + (1000000000 / (elapsedTime / 1000)) + " per second");
  }

  private Attribute<String>[][] _populateIndex(int count, SimpleMultidimensionalFuzzydex<String> multiIndex, int indices) throws EncoderException {
    Attribute<String>[][] words = new Attribute[count][indices];

    for (int i = 0; i < count; i++) {
      for (int j = 0; j < indices; j++)
        words[i][j] = new GeneralAttribute<String>(("index" + j), _makeRandomWord(10));
      multiIndex.addFact(Integer.toString(i), words[i]);
    }

    return words;
  }

  private String _makeRandomWord(int length) {
    StringBuilder buf = new StringBuilder();

    for (int x = 1; x <= length; x++)
      buf.append(chars.charAt(random.nextInt(49)));

    return buf.toString();
  }

}
