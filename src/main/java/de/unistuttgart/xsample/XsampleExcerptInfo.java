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

import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.DataverseUser;

/**
 * Raw information regarding the excerpt to be created.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleExcerptInfo implements Serializable {

	private static final long serialVersionUID = 142653554299182977L;

	private Dataverse server;
	private DataverseUser dataverseUser;
	private ExcerptType excerptType;
	private InputType inputType;
	private FileInfo fileInfo;
	
	public Dataverse getServer() { return server; }
	public void setServer(Dataverse server) { this.server = server; }
	
	public DataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(DataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }
	
	public ExcerptType getExcerptType() { return excerptType; }
	public void setExcerptType(ExcerptType type) { this.excerptType = type; }
	
	public InputType getInputType() { return inputType; }
	public void setInputType(InputType inputType) { this.inputType = inputType; }
	
	public FileInfo getFileInfo() { return fileInfo; }
	public void setFileInfo(FileInfo fileInfo) { this.fileInfo = fileInfo; }
}
