/* 
  Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  USA

  http://www.gnu.org/licenses/gpl.txt 
*/

package org.lamsfoundation.lams.contentrepository;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents the two part key - UUID and version.
 * Version may be null;
 * 
 * @author Fiona Malikoff
 */
public class NodeKey {

	private Long uuid = null;
	private Long version = null;
	
	/**
	 * 
	 */
	public NodeKey(Long uuid, Long version) {
		this.uuid = uuid;
		this.version = version;
	}

	/**
	 * @return Returns the uuid.
	 */
	public Long getUuid() {
		return uuid;
	}
	/**
	 * @return Returns the version.
	 */
	public Long getVersion() {
		return version;
	}
	
    public String toString() {
        return new ToStringBuilder(this)
            .append("uuid", uuid)
            .append("version", version)
            .toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof NodeKey) ) return false;
        NodeKey castOther = (NodeKey) other;
        return new EqualsBuilder()
            .append(uuid, castOther.getUuid())
            .append(version, castOther.getVersion())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(uuid)
            .append(version)
            .toHashCode();
    }	
}
