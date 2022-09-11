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
package de.unistuttgart.xsample.qe;

import static java.util.Objects.requireNonNull;

/**
 * Models raw results from a corpus query evaluation.
 * 
 * @author Markus Gärtner
 *
 */
public class QueryResult {

	private final Result result;
	private final long segments;
	
	public QueryResult(Result result, long segments) {
		this.result = requireNonNull(result);
		this.segments = segments;
	}
	
	public Result getResult() {
		return result;
	}
	/** Total number of searchable segments in the corpus or part that was searched */
	public long getSegments() {
		return segments;
	}
	
	public boolean isEmpty() { return result.isEmpty(); }
}
