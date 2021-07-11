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
package de.unistuttgart.xsample.qe;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

/**
 * Contains 0-based hits for the designated target corpus.
 * 
 * @author Markus Gärtner
 *
 */
public class Result implements Serializable {

	private static final long serialVersionUID = -48675199041173516L;
	
	private static final long[] EMPTY = {};

	/** Identifier for the corpus as specified in the manifest. */
	private String corpusId;

	/** The raw (sub)segments returned by the query engine. */
	private long[] hits = EMPTY;

	public String getCorpusId() {
		return corpusId;
	}

	public void setCorpusId(String corpusId) {
		this.corpusId = requireNonNull(corpusId);
	}

	public long[] getHits() {
		return hits;
	}

	public void setHits(long[] hits) {
		this.hits = requireNonNull(hits);
	}
	
	public boolean isEmpty() { return hits.length==0; }
	
	public void clear() { setHits(EMPTY); }
}
