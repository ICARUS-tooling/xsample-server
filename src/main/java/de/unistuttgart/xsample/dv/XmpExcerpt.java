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
@Entity(name = XmpExcerpt.TABLE_NAME)
@NamedQueries({ 
	@NamedQuery(name = "Excerpt.find", query = "SELECT e FROM Excerpt e WHERE e.dataverseUser = :user AND e.resource = :resource"), 
})
public class XmpExcerpt {
	
    public static final String TABLE_NAME= "Excerpt";

	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private XmpDataverseUser dataverseUser;

	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	private XmpResource resource;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "excerpt")
	private List<XmpFragment> fragments = new ArrayList<>();

	private transient long size = -1;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public XmpDataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(XmpDataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }

	public XmpResource getResource() { return resource; }
	public void setResource(XmpResource xmpResource) { this.resource = xmpResource; }

	public List<XmpFragment> getFragments() { return fragments; }
	public void setFragments(List<XmpFragment> xmpFragments) {
		this.fragments = requireNonNull(xmpFragments); 
		invalidateSize();
	}
	
	public void addFragment(XmpFragment xmpFragment) {
		fragments.add(xmpFragment);
		xmpFragment.setExcerpt(this);
		invalidateSize();
	}
	
	public void removeFragment(XmpFragment xmpFragment) {
		fragments.remove(xmpFragment);
		xmpFragment.detach();
		invalidateSize();
	}
	
	private void invalidateSize() { size = -1; }
	
	private void validateSize() {
		if(fragments.isEmpty()) {
			size = 0;
		}
		//TODO not overflow conscious!!
		size = fragments.stream().mapToLong(XmpFragment::size).sum();
	}
	
	public long size() {
		if(size==-1) {
			validateSize();
		}
		return size;
	}
	
	public boolean isEmpty() { return fragments.isEmpty(); }
	
	public void clear() {
		fragments.forEach(XmpFragment::detach);
		fragments.clear(); 
		invalidateSize();
	}
	
	public void merge(List<XmpFragment> others) {
		if(others.isEmpty()) {
			return;
		}
		
		List<XmpFragment> ours = new ArrayList<>(fragments);
		fragments.clear();
		invalidateSize();
		
		Consumer<XmpFragment> distinct = f -> {
			f.setExcerpt(this);
			fragments.add(f);
		};
		BiConsumer<XmpFragment, XmpFragment> overlap = (f1, f2) -> {
			// f1 is ours, f2 is theirs
			f1.setBeginIndex(Math.min(f1.getBeginIndex(), f2.getBeginIndex()));
			f1.setEndIndex(Math.max(f1.getEndIndex(), f2.getEndIndex()));
			fragments.add(f1);
		};
		
		XSampleUtils.merge(ours, others, distinct, overlap);
	}
}
