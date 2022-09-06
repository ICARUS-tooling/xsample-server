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
package de.unistuttgart.xsample.pages.slice;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.pages.shared.ExcerptUtilityData;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class SliceData extends ExcerptUtilityData {

	private static final long serialVersionUID = -5558438814838979800L;

	/** Begin of slice. 1-based. */
	private long begin = 1;
	/** End of slice. 1-based. */
	private long end = 1;

	/** Total number of segments available */
	private long segments = 1;
	/** Upper limit of allowed segments to be published */
	private long limit = 1;
	/** Encoded used up quota */
	private String quota = "";
	
	public long getBegin() { return begin; }
	public void setBegin(long begin) { this.begin = begin; }
	
	public long getEnd() { return end; }
	public void setEnd(long end) { this.end = end; }
	
	public long getSegments() { return segments; }
	public void setSegments(long range) { this.segments = range; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	public String getQuota() { return quota; }
	public void setQuota(String quota) { this.quota = quota; }
	
	public long getSize() { return end-begin+1; }
	
	/** Size of current slice in percent. Including quota */
	public double getPercent() {
		return (double)getSize() / segments * 100.0;
	}
}
