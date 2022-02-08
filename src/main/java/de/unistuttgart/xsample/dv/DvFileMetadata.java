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

/**
 * @author Markus Gärtner
 *
 */
public class DvFileMetadata implements Serializable {

	private static final long serialVersionUID = 4190333903371053495L;

	/**
	 * <pre>
	 * {
	 * 	"label":"xsample.png",
	 * 	"directoryLabel":"pictures",
	 * 	"restricted":false,
	 * 	"id":2205
	 * }
	 * </pre>
	 */
	
	private String label;
	private String directoryLabel;
	private boolean restricted;
	private long id;
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public String getDirectoryLabel() { return directoryLabel; }
	public void setDirectoryLabel(String directoryLabel) { this.directoryLabel = directoryLabel; }
	
	public boolean isRestricted() { return restricted; }
	public void setRestricted(boolean restricted) { this.restricted = restricted; }
	
	public long getId() { return id; }
	public void setId(long id) { this.id = id; }
}
