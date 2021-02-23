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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import de.unistuttgart.xsample.util.XSampleUtils;

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
	
	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private DataverseUser dataverseUser;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private Resource resource;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "excerpt")
	private List<Fragment> fragments = new ArrayList<>();

	private transient long size = -1;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public DataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(DataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }

	public Resource getResource() { return resource; }
	public void setResource(Resource resource) { this.resource = resource; }

	public List<Fragment> getFragments() { return fragments; }
	public void setFragments(List<Fragment> fragments) {
		this.fragments = requireNonNull(fragments); 
		invalidateSize();
	}
	
	public void addFragment(Fragment fragment) {
		fragments.add(fragment);
		fragment.setExcerpt(this);
		invalidateSize();
	}
	
	public void removeFragment(Fragment fragment) {
		fragments.remove(fragment);
		fragment.detach();
		invalidateSize();
	}
	
	private void invalidateSize() { size = -1; }
	
	private void validateSize() {
		if(fragments.isEmpty()) {
			size = 0;
		}
		//TODO not overflow conscious!!
		size = fragments.stream().mapToLong(Fragment::size).sum();
	}
	
	public long size() {
		if(size==-1) {
			validateSize();
		}
		return size;
	}
	
	public boolean isEmpty() { return fragments.isEmpty(); }
	
	public void clear() {
		fragments.forEach(Fragment::detach);
		fragments.clear(); 
		invalidateSize();
	}
	
	public void merge(List<Fragment> others) {
		if(others.isEmpty()) {
			return;
		}
		
		List<Fragment> ours = new ArrayList<>(fragments);
		fragments.clear();
		invalidateSize();
		
		Consumer<Fragment> distinct = f -> {
			f.setExcerpt(this);
			fragments.add(f);
		};
		BiConsumer<Fragment, Fragment> overlap = (f1, f2) -> {
			// f1 is ours, f2 is theirs
			f1.setBeginIndex(Math.min(f1.getBeginIndex(), f2.getBeginIndex()));
			f1.setEndIndex(Math.max(f1.getEndIndex(), f2.getEndIndex()));
			fragments.add(f1);
		};
		
		XSampleUtils.merge(ours, others, distinct, overlap);
	}
}
