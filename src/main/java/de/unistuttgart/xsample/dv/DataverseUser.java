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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Markus Gärtner
 *
 */
@Entity
@NamedQueries({ 
	@NamedQuery(name = "DataverseUser.findAll", query = "SELECT u FROM DataverseUser u ORDER BY u.id"),
	@NamedQuery(name = "DataverseUser.find", query = "SELECT u FROM DataverseUser u WHERE u.id.dataverseUrl = :url AND u.id.persistentUserId = :id"), 
})
public class DataverseUser {
	
	@ManyToOne(optional = false)
	private Dataverse dataverse;
	
	@EmbeddedId
	private UserId id;
	
	@Override
	public String toString() { return String.format("DataverseUser@[id=%s]", id); }

	public Dataverse getDataverse() { return dataverse; }
	public void setDataverse(Dataverse dataverse) { this.dataverse = dataverse; }

	public UserId getId() { return id; }
	public void setId(UserId id) { this.id = id; }
}