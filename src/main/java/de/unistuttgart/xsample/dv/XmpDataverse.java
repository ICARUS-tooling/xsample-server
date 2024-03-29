/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Markus Gärtner
 *
 */
@Entity(name = XmpDataverse.TABLE_NAME)
@NamedQueries({ 
	@NamedQuery(name = "Dataverse.findAll", query = "SELECT d FROM Dataverse d ORDER BY d.url"),
	@NamedQuery(name = "Dataverse.findByUrl", query = "SELECT d FROM Dataverse d WHERE d.url = :url"), 
})
public class XmpDataverse implements Serializable {
	
	private static final long serialVersionUID = -8182208410331899396L;

	public static final String TABLE_NAME= "Dataverse";

	@Column(length = 36, nullable = false, unique = true)
	private String masterKey;

	@Id
	private String url;
	
	@Column(nullable = true, unique = true)
	private String overrideUrl;
	
	public XmpDataverse() { /* no-op */ }
	
	public XmpDataverse(String url, String masterKey) {
		setUrl(url);
		setMasterKey(masterKey);
	}

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public String getOverrideUrl() { return overrideUrl; }
	public void setOverrideUrl(String overrideUrl) { this.overrideUrl = overrideUrl; }

	public String getMasterKey() { return masterKey; }
	public void setMasterKey(String masterKey) { this.masterKey = masterKey; }

	@Override
	public String toString() { return "Dataverse@"+url; }
	
	public String getUsableUrl() { return overrideUrl==null ? url : overrideUrl; }
}
