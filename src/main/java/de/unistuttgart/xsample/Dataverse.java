/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Markus Gärtner
 *
 */
@Entity
@NamedQueries({ 
	@NamedQuery(name = "Dataverse.findAll", query = "SELECT d FROM Dataverse d ORDER BY d.id"),
	@NamedQuery(name = "Dataverse.findByUrl", query = "SELECT d FROM Dataverse d WHERE d.url = :url"), 
})
public class Dataverse {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true)
	private String url;

	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }

	public String getUrl() { return url; }

	public void setUrl(String url) { this.url = url; }

	@Override
	public String toString() { return String.format("Dataverse@[id=%d, url=%s]", id, url); }
}
