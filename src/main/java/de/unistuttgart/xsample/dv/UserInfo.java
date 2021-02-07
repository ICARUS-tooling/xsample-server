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

/**
 * 
 * @author Markus Gärtner
 *
 */
public class UserInfo implements Serializable {

	private static final long serialVersionUID = -3275019152850763485L;
	
	/**
	 * <pre>
	 * {
		  "status": "OK",
		  "data": {
		    "id": 1,
		    "identifier": "@dataverseAdmin",
		    "displayName": "Dataverse Admin",
		    "firstName": "Dataverse",
		    "lastName": "Admin",
		    "email": "dataverse@mailinator.com",
		    "superuser": true,
		    "affiliation": "Dataverse.org",
		    "position": "Admin",
		    "persistentUserId": "dataverseAdmin",
		    "createdTime": "2020-12-15T17:21:59Z",
		    "lastLoginTime": "2020-12-15T17:24:51Z",
		    "lastApiUseTime": "2020-12-15T17:22:00Z",
		    "authenticationProviderId": "builtin"
		  }
		}
	 * </pre>
	 */
	
	private String status;
	private Data data;
	
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	
	public Data getData() { return data; }
	public void setData(Data data) { this.data = data; }

	public static class Data implements Serializable {
		
		private static final long serialVersionUID = -479480350109052969L;
		
		private long id;
		private String identifier;
		private String displayName;
		private String firstName;
		private String lastName;
		private String email;
		private boolean superuser;
		private String affiliation;
		private String position;
		private String persistentUserId;
		private String authenticationProviderId;
		// We ignore the date fields in user info
		
		public long getId() { return id; }
		public void setId(long id) { this.id = id; }
		
		public String getIdentifier() { return identifier; }
		public void setIdentifier(String identifier) { this.identifier = identifier; }
		
		public String getDisplayName() { return displayName; }
		public void setDisplayName(String displayName) { 	this.displayName = displayName; }
		
		public String getFirstName() { return firstName; }
		public void setFirstName(String firstName) { this.firstName = firstName; }
		
		public String getLastName() { return lastName; }
		public void setLastName(String lastName) { this.lastName = lastName; }
		
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		
		public boolean isSuperuser() { return superuser; }	
		public void setSuperuser(boolean superuser) { this.superuser = superuser; }
		
		public String getAffiliation() { return affiliation; }
		public void setAffiliation(String affiliation) { this.affiliation = affiliation; }
		
		public String getPosition() { return position; }
		public void setPosition(String position) { this.position = position; }
		
		public String getPersistentUserId() { return persistentUserId; }
		public void setPersistentUserId(String persistentUserId) { this.persistentUserId = persistentUserId; }
		
		public String getAuthenticationProviderId() { return authenticationProviderId; } 
		public void setAuthenticationProviderId(String authenticationProviderId) { this.authenticationProviderId = authenticationProviderId; }
	}
	
}