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
package de.unistuttgart.xsample.pages.shared;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.io.Serializable;
import java.util.List;

import de.unistuttgart.xsample.dv.XmpFragment;

/**
 * 
 * @author Markus Gärtner
 *
 */
public class ExcerptEntry implements Serializable {

	private static final long serialVersionUID = 3111407155877405794L;
	
	/** Corpus or subcorpus to extract from */
	private String corpusId;
	/** Designated output */
	private List<XmpFragment> fragments;
	/** Limit within the associated corpus */
	private long limit;
	
	public String getCorpusId() {
		return corpusId;
	}
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}
	public List<XmpFragment> getFragments() {
		return fragments;
	}
	public void setFragments(List<XmpFragment> fragments) {
		this.fragments = fragments;
	}
	
	public boolean isEmpty() { return fragments==null || fragments.isEmpty(); }
	
	/** Reset the fragment data on this excerpt */
	public void clear() {
		fragments = null;
	}
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	@Override
	public String toString() {
		return String.format("%s@[corpus=%s, limit=%d, fragments=%s]", getClass().getSimpleName(), corpusId, _long(limit), fragments);
	}
}