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
package de.unistuttgart.xsample.gen;

class FileUploadInfo {
	/**
	 * <pre>
	 * {
		  "description": "My description.",
		  "directoryLabel": "data/subdir1",
		  "categories": [
		    "Data"
		  ],
		  "restrict": "false"
		}
	 * </pre>
	 */
	private String description;
	private String directoryLabel;
	private String mimeType;
	private String fileName;
	private boolean restricted;
	private String[] categories;
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getDirectoryLabel() { return directoryLabel; }
	public void setDirectoryLabel(String directoryLabel) { this.directoryLabel = directoryLabel; }
	public String getMimeType() { return mimeType; }
	public void setMimeType(String mimeType) { this.mimeType = mimeType; }
	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }
	public boolean isRestricted() { return restricted; }
	public void setRestricted(boolean restricted) { this.restricted = restricted; }
	public String[] getCategories() { return categories; }
	public void setCategories(String[] categories) { this.categories = categories; }
}