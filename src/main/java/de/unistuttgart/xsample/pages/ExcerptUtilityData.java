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
package de.unistuttgart.xsample.pages;

import java.io.Serializable;

/**
 * @author Markus Gärtner
 *
 */
public abstract class ExcerptUtilityData implements Serializable {

	private static final long serialVersionUID = -6599365533185080045L;

	/** Total number of segments available */
	private long range = 1;
	/** Upper limit of allowed segments to be published */
	private long limit = 1;
	/** Encoded used up quota */
	private String quota = "";
	
	public long getRange() { return range; }
	public void setRange(long range) { this.range = range; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	public String getQuota() { return quota; }
	public void setQuota(String quota) { this.quota = quota; }
}
