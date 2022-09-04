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
package de.unistuttgart.xsample.util;

import java.io.Serializable;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ExcerptUtilityData implements Serializable {

	private static final long serialVersionUID = -6599365533185080045L;

	/** Encoded global excerpt */
	private String globalExcerpt = "";
	/** Encoded global quota */
	private String globalQuota = "";
	
	/** Total number of segments available in entire corpus */
	private long globalSegments = 1;
	/** Upper limit of allowed segments to be published */
	private long globalLimit = 1;
	/** Total number of segments accumulated in all excerpts defined here */
	private long globalUsed = 0;

	
	public String getGlobalExcerpt() { return globalExcerpt; }
	public void setGlobalExcerpt(String globalExcerpt) { this.globalExcerpt = globalExcerpt; }
	
	public String getGlobalQuota() { return globalQuota; }
	public void setGlobalQuota(String globalQuota) { this.globalQuota = globalQuota; }
	
	public long getGlobalSegments() { return globalSegments; }
	public void setGlobalSegments(long globalSize) { this.globalSegments = globalSize; }
	
	public long getGlobalLimit() { return globalLimit; }
	public void setGlobalLimit(long globalLimit) { this.globalLimit = globalLimit; }
	
	public long getGlobalUsed() { return globalUsed; }
	public void setGlobalUsed(long globalUsed) { this.globalUsed = globalUsed; }
	
	/** Size of current slice in percent. Including quota */
	public double getGlobalPercent() {
		return (double)globalUsed / globalSegments * 100.0;
	}
}
