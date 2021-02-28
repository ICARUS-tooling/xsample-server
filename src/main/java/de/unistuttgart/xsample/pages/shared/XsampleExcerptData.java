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
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.DataverseUser;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.dv.Resource;
import de.unistuttgart.xsample.mf.XsampleManifest;

/**
 * Input information regarding the excerpt to be created.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsampleExcerptData implements Serializable {

	private static final long serialVersionUID = 142653554299182977L;

	/** Wrapper for remote server to download data from */
	private Dataverse server;
	/** User to be used for tracking excerpt quota */
	private DataverseUser dataverseUser;
	/** DB wrapper for the source */
	private Resource resource;
	/** Used up quota */
	private Excerpt quota;
	/** Designated output */
	private List<Fragment> excerpt;
	
	private String staticExcerptLabel;
	
//	/** Type info for raw input file, e.g. 'MANIFEST', 'PDF' */
//	private InputType inputType;
	/** Physical info about primary source file */
	private FileInfo fileInfo;	
	
	/** The root manifest for the current workflow. */
	private XsampleManifest manifest;
	
	/** The designated handler to manage excerpt generation and analysis of the source file */
	private ExcerptHandler excerptHandler;
	
	/** Type of excerpt generation, legal values are 'static', 'window' and 'query'. */
	private ExcerptType excerptType = ExcerptType.STATIC;
	/** Flag to indicate that annotations should be made part of the final excerpt */
	private boolean includeAnnotations = false;
	/** Flag to indicate that the file is too small for excerpt generation */
	private boolean smallFile = false;
	
	private long staticExcerptBegin = 0;
	private long staticExcerptEnd = 10;
	private boolean staticExcerptFixed = false;
	
	public Dataverse getServer() { return server; }
	public void setServer(Dataverse server) { this.server = server; }
	
	public DataverseUser getDataverseUser() { return dataverseUser; }
	public void setDataverseUser(DataverseUser dataverseUser) { this.dataverseUser = dataverseUser; }
	
	public ExcerptType getExcerptType() { return excerptType; }
	public void setExcerptType(ExcerptType type) { this.excerptType = requireNonNull(type); }
	
	public XsampleManifest getManifest() { return manifest; }
	public void setManifest(XsampleManifest manifest) { this.manifest = manifest; }
	
//	public InputType getInputType() { return inputType; }
//	public void setInputType(InputType inputType) { this.inputType = inputType; }
	
	public FileInfo getFileInfo() { return fileInfo; }
	public void setFileInfo(FileInfo fileInfo) { this.fileInfo = fileInfo; }

	public boolean isIncludeAnnotations() { return includeAnnotations; }
	public void setIncludeAnnotations(boolean includeAnnotations) { this.includeAnnotations = includeAnnotations; }
	
	public Resource getResource() { return resource; }
	public void setResource(Resource resource) { this.resource = resource; }
	
	public Excerpt getQuota() { return quota; }
	public void setQuota(Excerpt quota) { this.quota = quota; }
	
	public List<Fragment> getExcerpt() { return excerpt; }
	public void setExcerpt(List<Fragment> excerpt) { this.excerpt = excerpt; }
	
	public boolean isSmallFile() { return smallFile; }
	public void setSmallFile(boolean smallFile) { this.smallFile = smallFile; }
	
	public long getStaticExcerptBegin() { return staticExcerptBegin; }
	public void setStaticExcerptBegin(long staticExcerptBegin) { this.staticExcerptBegin = staticExcerptBegin; }
	
	public long getStaticExcerptEnd() { return staticExcerptEnd; }
	public void setStaticExcerptEnd(long staticExcerptEnd) { this.staticExcerptEnd = staticExcerptEnd; }
	
	public boolean isStaticExcerptFixed() { return staticExcerptFixed; }
	public void setStaticExcerptFixed(boolean staticExcerptFixed) { this.staticExcerptFixed = staticExcerptFixed; }
	
	public String getStaticExcerptLabel() { return staticExcerptLabel; }
	public void setStaticExcerptLabel(String staticExcerptLabel) { this.staticExcerptLabel = staticExcerptLabel; }
	
	public ExcerptHandler getExcerptHandler() { return excerptHandler; }
	public void setExcerptHandler(ExcerptHandler excerptHandler) { this.excerptHandler = excerptHandler; }

}