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
package de.unistuttgart.xsample;

import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleInputData implements Serializable {

	private static final long serialVersionUID = 8906894558636517419L;
	
	/** The file id as supplied by the external-tools URL */
	private Long file;	
	/** The user's API key as supplied by the external-tools URL */
	private String key;
	/** The source URL of the dataverse the request originated from */
	private String site;
	
	public Long getFile() { return file; }
	public void setFile(Long file) { this.file = file; }
	
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	
	public String getSite() { return site; }
	public void setSite(String site) { this.site = site; }
}
