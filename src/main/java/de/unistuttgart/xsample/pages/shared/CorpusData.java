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

import static java.util.Objects.requireNonNull;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.mf.Corpus;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Accumulated (global) info about corpus.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class CorpusData extends EncodedCorpusData {
	
	private static final long serialVersionUID = 2235465150226921953L;
	
	/** Maps corpus ids to the determined segment counts that should be used for them */
	private Object2LongMap<String> segmentsByCorpus = new Object2LongOpenHashMap<>();
	private Object2LongMap<String> limitByCorpus = new Object2LongOpenHashMap<>();
	private Object2LongMap<String> offsetByCorpus = new Object2LongOpenHashMap<>();
	
	/** Number of segments used up by quota */
	private long quotaSize = 0;

	public CorpusData() {
		segmentsByCorpus.defaultReturnValue(-1);
		limitByCorpus.defaultReturnValue(-1);
		offsetByCorpus.defaultReturnValue(-1);
	}
	
	@Override
	public void reset() {
		super.reset();
		
		segmentsByCorpus.clear();
		limitByCorpus.clear();
		offsetByCorpus.clear();
		
		quotaSize = 0;
	}
	
	public long getQuotaSize() { return quotaSize; }
	public void setQuotaSize(long quotaSize) { this.quotaSize = quotaSize; }
	
	// SEGMENTS

	public void registerSegments(String corpusId, long segments) {
		segmentsByCorpus.put(requireNonNull(corpusId), segments);
	}	
	public long getSegments(String partId) {
		long segments = segmentsByCorpus.getLong(requireNonNull(partId));
		if(segments==-1)
			throw new IllegalArgumentException("Unknown corpus id: "+partId);
		return segments;
	}	
	public long getSegments(Corpus part) {
		return getSegments(part.getId());
	}
	
	// LIMIT
	
	public void registerLimit(String corpusId, long limit) {
		limitByCorpus.put(requireNonNull(corpusId), limit);
	}	
	public long getLimit(String partId) {
		long limit = limitByCorpus.getLong(requireNonNull(partId));
		if(limit==-1)
			throw new IllegalArgumentException("Unknown corpus id: "+partId);
		return limit;
	}	
	public long getLimit(Corpus part) {
		return getLimit(part.getId());
	}
	
	// OFFSET
	
	public void registerOffset(String corpusId, long limit) {
		offsetByCorpus.put(requireNonNull(corpusId), limit);
	}	
	public long getOffset(String partId) {
		long limit = offsetByCorpus.getLong(requireNonNull(partId));
		if(limit==-1)
			throw new IllegalArgumentException("Unknown corpus id: "+partId);
		return limit;
	}	
	public long getOffset(Corpus part) {
		return getOffset(part.getId());
	}
	
}
