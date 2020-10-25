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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Markus Gärtner
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "Resource.findAll", query = "SELECT r FROM Resource r ORDER BY r.id"),
	@NamedQuery(name = "Resource.findByFile", query = "SELECT r FROM Resource r WHERE r.file = :file"), 
})
public class Resource {

	@Column
	private Long file;

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	private Dataverse dataverse;

	public Dataverse getDataverse() { return dataverse; }

	public void setDataverse(Dataverse dataverse) { this.dataverse = dataverse; }

	public Long getFile() { return file; }

	public void setFile(Long file) { this.file = file; }

	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }
	
	@Override
	public String toString() { return String.format("Resource@[id=%d, file=%d]", id, file); }
}
