/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.pdfbox.io.IOUtils;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.util.FileInfo;
import de.unistuttgart.xsample.util.Property;

/**
 * @author Markus G�rtner
 *
 */
public class XsampleExcerptConfig implements Serializable {

	private static final long serialVersionUID = -3897121271961387495L;
	
	/** The file id as supplied by the external-tools URL */
	private Long file;	
	/** The user's API key as supplied by the external-tools URL */
	private String key;
	/** The source URL of the dataverse the request originated from */
	private String site;
	
	private FileInfo fileInfo;
	/** The handler responsible for managing the source file and creating excerpts from it */
	private ExcerptHandler handler;
	
	/** Begin of user defined excerpt */
	private long start = 1;
	/** End of user defined excerpt */
	private long end = 1;
	
	
	public Long getFile() { return file; }
	public void setFile(Long file) { this.file = file; }
	
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	
	public String getSite() { return site; }
	public void setSite(String site) { this.site = site; }
	
	public long getStart() { return start; }
	public void setStart(long excerptStart) { this.start = excerptStart; }

	public long getEnd() { return end; }
	public void setEnd(long excerptEnd) { this.end = excerptEnd; }
	
	public FileInfo getFileInfo() { return fileInfo; }
	public long getSegments() { return handler==null ? 0 : handler.segments(); }
	public ExcerptHandler getHandler() { return handler; }
	
	// Validation method
	
	//TODO rethink approach
	public void validateData(FacesContext fc, UIComponent comp, Object obj) {
		System.out.println("validating");
	}
	
	// Bulk modifications
	
	public void setFileData(FileInfo fileInfo, ExcerptHandler handler) {
		this.fileInfo = requireNonNull(fileInfo);
		this.handler = requireNonNull(handler);
	}
	
	public void resetFileData() {
		fileInfo = null;
		IOUtils.closeQuietly(handler);
		handler = null;
	}
	
	// Utility methods
	
	public int getPercent() {
		return (int) Math.ceil((end-start+1.0) / getSegments() * 100.0);
	}
	
	public long getRange() { return end-start+1; }
	
	public boolean isHasFile() { return handler!=null; }


	private DecimalFormat decimalFormat = new DecimalFormat("#,###");

	private String formatDecimal(long value) {
		return decimalFormat.format(value);
	}
	
	public List<Property> getProperties() {
		if(!isHasFile()) {
			return Collections.emptyList();
		}
		
		List<Property> props = new ArrayList<>();
		props.add(new Property("Title", fileInfo.getTitle()));
		props.add(new Property("Content Type", fileInfo.getContentType()));
		props.add(new Property("Character Encoding", fileInfo.getEncoding().displayName(Locale.US)));
		props.add(new Property("Segments", formatDecimal(getSegments())));
		props.add(new Property("Size", formatDecimal(fileInfo.getSize()/1024)+"KB"));
		return props;
	}
}
