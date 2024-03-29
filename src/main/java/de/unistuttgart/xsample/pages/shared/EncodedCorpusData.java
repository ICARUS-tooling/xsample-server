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
package de.unistuttgart.xsample.pages.shared;

import de.unistuttgart.xsample.util.DataBean;

/**
 * @author Markus Gärtner
 *
 */
@SuppressWarnings("serial")
public abstract class EncodedCorpusData implements DataBean {

	/** Total number of segments available */
	private long segments = -1;
	/** Upper limit of allowed segments to be published */
	private long limit = -1;
	/** Encoded used up quota */
	private String quota = "";
	/** Encoded user-selected excerpt */
	private String excerpt = "";
	
	public long getSegments() { return segments; }
	public void setSegments(long range) { this.segments = range; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	public String getQuota() { return quota; }
	public void setQuota(String quota) { this.quota = quota; }
	
	public String getExcerpt() { return excerpt; }
	public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
	
	public void reset() {
		// We use -1 here to prevent div-by-zero issues and to easily indicate missing values in the ui
		segments = -1;
		limit = -1;
		quota = "";
		excerpt = "";
	}
}
