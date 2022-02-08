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
package de.unistuttgart.xsample.qe.icarus1.match;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.qe.icarus1.CONLL09SentenceDataReader;
import de.unistuttgart.xsample.qe.icarus1.Location;
import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.SentenceData;

/**
 * @author Markus Gärtner
 *
 */
class SearchTest {
	
	private List<SentenceData> loadCorpus() throws Exception {
		final InputStream in = SearchTest.class.getResourceAsStream("/de/unistuttgart/xsample/qe/icarus1/icarus.conll09");
		final Location location = new Location.Base() {
			@Override
			public InputStream openInputStream() throws IOException { return in; }
		};
		final CONLL09SentenceDataReader reader = new CONLL09SentenceDataReader(true);
		return reader.readAll(location, null);
	}
	
	static Stream<Arguments> queryResultProvider() {
		return Stream.of(
				Arguments.of(Options.emptyOptions, "[form=Icarus]", new long[] {0, 4, 6, 7, 9}),
				Arguments.of(new Options(SearchParameters.SEARCH_CASESENSITIVE, true), 
						"[form=He]", new long[] {3, 4})
		);
	}

	@ParameterizedTest
	@MethodSource("queryResultProvider")
	void testDummyCorpus(Options options, String queryString, long[] hits) throws Exception {
		List<SentenceData> corpus = loadCorpus();
		
		ConstraintContext context = ConstraintContext.defaultContext();
		SearchQuery query = new SearchQuery(context);		
		query.parseQueryString(queryString);
		Search search = new Search(query, options, corpus);
		
		assertThat(search.init()).isTrue();
		search.execute();
		assertThat(search.isDone()).isTrue();
		
		Result result = search.getResult();
		assertThat(result.getHits()).containsExactly(hits);
	}

}
