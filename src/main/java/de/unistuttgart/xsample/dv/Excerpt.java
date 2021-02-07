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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * @author Markus Gärtner
 *
 */
@Entity
@NamedQueries({ 
	@NamedQuery(name = "Excerpt.find", query = "SELECT e FROM Excerpt e WHERE e.dataverseUser = :user AND e.resource = :resource"), 
})
public class Excerpt {

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne(optional = false)
	private DataverseUser dataverseUser;

	@ManyToOne(optional = false)
	private Resource resource;

	@OneToMany
	private List<Fragment> fragments;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public DataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(DataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }

	public Resource getResource() { return resource; }
	public void setResource(Resource resource) { this.resource = resource; }

	public List<Fragment> getFragments() { return fragments; }
	public void setFragments(List<Fragment> fragments) { this.fragments = fragments; }
	
	public long size() {
		if(fragments.isEmpty()) {
			return 0;
		}
		//TODO not overflow conscious!!
		return fragments.stream().mapToLong(Fragment::size).sum();
	}
}
