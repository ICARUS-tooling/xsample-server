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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Markus Gärtner
 *
 */
@Entity
@NamedQueries({ 
	@NamedQuery(name = "Dataverse.findAll", query = "SELECT d FROM Dataverse d ORDER BY d.url"),
	@NamedQuery(name = "Dataverse.findByUrl", query = "SELECT d FROM Dataverse d WHERE d.url = :url"), 
})
public class Dataverse {

	@Column(length = 36, nullable = false, unique = true)
	private String masterKey;

	@Id
	private String url;
	
	public Dataverse() { /* no-op */ }
	
	public Dataverse(String url, String masterKey) {
		setUrl(url);
		setMasterKey(masterKey);
	}

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public String getMasterKey() { return masterKey; }
	public void setMasterKey(String masterKey) { this.masterKey = masterKey; }

	@Override
	public String toString() { return "Dataverse@"+url; }
}
