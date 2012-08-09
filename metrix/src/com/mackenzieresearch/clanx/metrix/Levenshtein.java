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

package com.mackenzieresearch.clanx.metrix;

/**
 * Various implementations of a Levenshtein Metric calculator.  Calculates the
 * distance between any two strings in terms of insertions, deletions and substitutions.
 * <p/>
 * See: http://www.wikipedia.org/wiki/Levenshtein_distance for a description of
 * how the algorithm works.
 */
public class Levenshtein {
  /**
   * This naive version calculates a Levenshtein metric in Order MxN time, using an MxN
   * matrix, so it will consume memory geometrically as the size of the compared strings
   * increases.  It's really only useful for small string field comparisons.
   * 
   * @param mParam
   * @param nParam
   * @return levenshtein distance
   */
  public static int naive(String mParam, String nParam) {
    //  Prime the matrix for us to iterate thru the (m x n) combinations, counting differences...
    int m = mParam.length();
    int n = nParam.length();

    int[][] lMatrix = new int[m + 1][n + 1];

    for (int i = 0; i < m; i++)
      lMatrix[i][0] = i;

    for (int j = 0; j < n; j++)
      lMatrix[0][j] = j;

    //  Spin thru the cartesian product of the two strings (m x n) doing essentially a floodfill,
    //  pushing the current highwater lev number forward with us thru the matrix...
    for (int i = 1; i <= m; i++) {
      for (int j = 1; j <= n; j++) {
        if (mParam.charAt(i - 1) == nParam.charAt(j - 1))
          lMatrix[i][j] = lMatrix[i - 1][j - 1];
        else {
          lMatrix[i][j] = Math.min(lMatrix[i - 1][j] + 1,
                  Math.min(lMatrix[i][j - 1] + 1,
                          lMatrix[i - 1][j - 1] + 1));
        }
      }
    }

    //  The corner cell of the matrix should contain the results of our hard work:
    return lMatrix[m][n];
  }


  /**
   * This version takes theoretically the same amount of time as the naive version (it's still order MxN).
   * However, because it only uses a matrix 2xN in size, it will DRASTICALLY reduce memory used and therefore
   * garbage collection time, when used on file-sized strings.  (A quasi-scanline approach.)
   * 
   * @param mParam
   * @param nParam
   * @return levenshtein distance
   */
  public static int scanLine(String mParam, String nParam) {
    //  Prime a two-line buffer in lieu of the matrix for us to iterate thru the
    // (m x n) combinations, counting differences...
    int m = mParam.length();
    int n = nParam.length();

    int[][] refMatrix = new int[2][Math.max(n, m) + 1];
    int[][] lMatrix = new int[2][];

    for (int j = 0; j < n; j++)
      refMatrix[0][j] = j;

    //  Set up our two-line working buffer backwards so that the first flip our loop does below
    //  lines things up correctly and can continue to iterate thru rotating buffers until the end...
    lMatrix[0] = refMatrix[1];
    lMatrix[1] = refMatrix[0];

    for (int i = 1; i <= m; i++) {
      //  Rotate our twoline buffer so previous working line is now at [0] and we set [1] up as
      // the new working line...
      int[] tmpMatrix = lMatrix[0];
      lMatrix[0] = lMatrix[1];
      lMatrix[1] = tmpMatrix;
      lMatrix[1][i - 1] = i - 1;

      //  Make the next pass...
      for (int j = 1; j <= n; j++) {
        if (mParam.charAt(i - 1) == nParam.charAt(j - 1))
          lMatrix[1][j] = lMatrix[0][j - 1];
        else {
          lMatrix[1][j] = Math.min(lMatrix[0][j] + 1,
                  Math.min(lMatrix[1][j - 1] + 1,
                          lMatrix[0][j - 1] + 1));
        }
      }
    }

    //  The corner cell of the matrix should contain the results of our hard work:
    return lMatrix[1][n];
  }
}
