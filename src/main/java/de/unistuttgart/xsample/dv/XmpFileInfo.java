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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

import de.unistuttgart.xsample.mf.SourceType;

/**
 * Metadata about a file. Mainly geared towards content based info.
 * @author Markus Gärtner
 *
 */
@Entity(name = XmpFileInfo.TABLE_NAME)
@NamedQueries({
	@NamedQuery(name = "FileInfo.findByResource", query = "SELECT f FROM FileInfo f WHERE f.resource = :resource"),
})
public class XmpFileInfo {
	
    public static final String TABLE_NAME= "FileInfo";

	@Id
	@GeneratedValue
	private Long id;
	
	@OneToOne
	@JoinColumn(name = "resourceId", nullable = false, unique = true)
	private XmpResource resource;
	
	/** Number of segments that can be extracted. */
	@Column
	private long segments = -1;
	
	/** Flag to indicate that the file is too small for excerpt generation */
	@Column
	private boolean smallFile = false;

	@Column
	private SourceType sourceType;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public XmpResource getResource() {
		return resource;
	}
	public void setResource(XmpResource resource) {
		this.resource = resource;
	}
	public long getSegments() { return segments; }
	public void setSegments(long segments) { this.segments = segments; }
	
	public boolean isSmallFile() { return smallFile; }
	public void setSmallFile(boolean smallFile) { this.smallFile = smallFile; }
	
	public SourceType getSourceType() {
		return sourceType;
	}
	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}
	
	// Utility methods
	public boolean isSet() { return segments!=-1L; }
}
