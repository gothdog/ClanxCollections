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


public class KVPair<T> extends TupleArityTwo<String, T> {
  public KVPair(String key, T value) {
    super(key, value);
  }
}
