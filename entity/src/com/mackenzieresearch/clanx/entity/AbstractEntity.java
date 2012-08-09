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

package com.mackenzieresearch.clanx.entity;

import java.util.Collection;
import java.util.LinkedHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractEntity<T> implements Entity<T>, Comparable<T> {
  private LinkedHashMap<String, Attribute> _attributes = new LinkedHashMap<String, Attribute>();

  public Collection<Attribute> attributes() {
    return _attributes.values();
  }

  @Override
  public void addAttribute(Attribute attribute) {
    checkNotNull(attribute);
    String name = attribute.name();
    Object entry = _attributes.get(name);
    checkState(!_attributes.containsKey(attribute.name()));
    _attributes.put(attribute.name(), attribute);
  }

  @Override
  public void addAll(Collection<Attribute> attributes) {
    checkNotNull(attributes);
    for (Attribute attribute : attributes) {
      checkState(!_attributes.containsKey(attribute.name()));
      _attributes.put(attribute.name(), attribute);
    }
  }

  @Override
  public Attribute getAttribute(String attributeName) {
    checkNotNull(attributeName);

    return _attributes.get(attributeName);
  }

  @Override
  public void setAttribute(Attribute attr) {
    checkNotNull(attr);

    Attribute attribute = _attributes.get(attr.name());
    checkNotNull(attribute);

    _attributes.put(attribute.name(), attr);
  }
}
