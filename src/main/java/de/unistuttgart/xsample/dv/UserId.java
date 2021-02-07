/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package de.unistuttgart.xsample.dv;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;


/**
 * Primar key wrapper class for {@link DataverseUser}.
 * @author Markus Gärtner
 *
 */
@Embeddable
public class UserId implements Serializable {
	private static final long serialVersionUID = 6675659702120722414L;
	
	private String dataverseUrl;
	private String persistentUserId;
	
	public UserId() { /* no-op */ }
	
	public UserId(String dataverseUrl, String persistentUserId) {
		setDataverseUrl(dataverseUrl);
		setPersistentUserId(persistentUserId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(dataverseUrl, persistentUserId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof UserId) {
			UserId other = (UserId) obj;
			return Objects.equals(dataverseUrl, other.dataverseUrl)
					&& Objects.equals(persistentUserId, other.persistentUserId);
		}
		return false;
	}
	
	public String getDataverseUrl() { return dataverseUrl; }
	public void setDataverseUrl(String dataverse) { this.dataverseUrl = dataverse; }
	
	public String getPersistentUserId() { return persistentUserId; }
	public void setPersistentUserId(String persistentUserId) { this.persistentUserId = persistentUserId; }
}
