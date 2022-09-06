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
package de.unistuttgart.xsample.qe.icarus1;

import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import de.unistuttgart.xsample.qe.QueryException;
import de.unistuttgart.xsample.qe.QueryException.QueryErrorCode;
import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.qe.icarus1.match.ConstraintContext;
import de.unistuttgart.xsample.qe.icarus1.match.Search;
import de.unistuttgart.xsample.qe.icarus1.match.SearchQuery;

/**
 * @author Markus Gärtner
 *
 */
public class Icarus1Wrapper {
	
	private SearchQuery query;
	private Options options;
	
	public void init(String queryString, Properties settings) throws QueryException {
		checkNotEmpty(queryString);
		requireNonNull(settings);
		
		ConstraintContext context = ConstraintContext.defaultContext();
		query = new SearchQuery(context);		
		try {
			query.parseQueryString(queryString);
		} catch (UnsupportedFormatException e) {
			throw new QueryException("Unsupported format in query: "+queryString, QueryErrorCode.SYNTAX_ERROR, e);
		}
		
		options = new Options(settings);
	}

	public ResultPart evaluate(Reader reader) throws QueryException {
		requireNonNull(reader);
		checkState("Query not initialized", query!=null);
		
		final CONLL09SentenceDataReader conllReader = new CONLL09SentenceDataReader(true);
		final List<SentenceData> corpus;
		try {
			corpus = conllReader.readAll(reader, null);
		} catch (IOException e) {
			throw new QueryException("Failed to load corpus file", QueryErrorCode.IO_ERROR, e);
		} catch (UnsupportedFormatException e) {
			throw new QueryException("Failed to parse corpus data", QueryErrorCode.UNSUPPORTED_FORMAT, e);
		}
		
		final Search search = new Search(query, options, corpus);
		
		try {
			search.init();
			search.execute();
		} catch(RuntimeException e) {
			throw new QueryException("Internal search error", QueryErrorCode.INTERNAL_ERROR, e);
		}
	
		return new ResultPart(search.getResult(), corpus.size());
	}
	
	public static class ResultPart {
		private final Result result;
		private final int segments;
		
		public ResultPart(Result result, int segments) {
			this.result = result;
			this.segments = segments;
		}
		
		public Result getResult() { return result; }
		public int getSegments() { return segments; }
		
		public boolean isEmpty() { return result.isEmpty(); }
	}
}
