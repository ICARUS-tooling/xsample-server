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
package de.unistuttgart.xsample.qe.icarus1;

import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import de.unistuttgart.xsample.qe.QueryException;
import de.unistuttgart.xsample.qe.QueryException.ErrorCode;
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
			throw new QueryException("Unsupported format in query: "+queryString, ErrorCode.SYNTAX_ERROR, e);
		}
		
		options = new Options(settings);
	}

	public Result evaluate(InputStream in) throws QueryException {
		requireNonNull(in);
		checkState("Query not initialized", query!=null);
		
		final Location location = new Location.Base() {
			@Override
			public InputStream openInputStream() throws IOException { return in; }
		};
		final CONLL09SentenceDataReader reader = new CONLL09SentenceDataReader(true);
		final List<SentenceData> corpus;
		try {
			corpus = reader.readAll(location, null);
		} catch (IOException e) {
			throw new QueryException("Failed to load corpus file", ErrorCode.IO_ERROR, e);
		} catch (UnsupportedLocationException e) {
			throw new QueryException("Failed to load corpus file (location issue)", ErrorCode.IO_ERROR, e);
		} catch (UnsupportedFormatException e) {
			throw new QueryException("Failed to parse corpus data", ErrorCode.UNSUPPORTED_FORMAT, e);
		}
		
		final Search search = new Search(query, options, corpus);
		
		try {
			search.init();
			search.execute();
		} catch(RuntimeException e) {
			throw new QueryException("Internal search error", ErrorCode.INTERNAL_ERROR, e);
		}
	
		return search.getResult();
	}
}
