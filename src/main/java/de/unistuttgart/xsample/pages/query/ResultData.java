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
package de.unistuttgart.xsample.pages.query;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import de.unistuttgart.xsample.qe.Result;

/**
 * Search results and utility data for a single corpus part.
 * 
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class ResultData extends EncodedResultData {

	private static final long serialVersionUID = 5842207950786664322L;

	/** Result segments as returned by the engine */
	private Result rawResult;
	/** Result mapped to primary segments for excerpt generation */
	private Result mappedResult;
	/** Number of possible result segments */
	private long rawSegments = 0;
	
	public Result getRawResult() { return rawResult; }
	public void setRawResult(Result rawResult) { this.rawResult = rawResult; }
	
	public Result getMappedResult() { return mappedResult; }
	public void setMappedResult(Result mappedResult) { this.mappedResult = mappedResult; }
	
	public long getRawSegments() { return rawSegments; }
	public void setRawSegments(long limit) { this.rawSegments = limit; }
	
	@Override
	public void reset() {
		super.reset();
		
		rawResult = null;
		mappedResult = null;
		rawSegments = 0;
	}
	
	public boolean isEmpty() { return rawResult==null || rawResult.isEmpty(); }
	
	@Override
	public String toString() {
		return String.format("%s@[rawHits='%s', mappedHits='%s', rawResult=%s, mappedResult=%s, rawSegments=%d]", 
				getClass().getSimpleName(), getRawHits(), getMappedHits(),
				rawResult, mappedResult, _long(rawSegments));
	}
}
