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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Markus Gärtner
 *
 */
class CONLL09SentenceDataReaderTest {

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void testFullFile(boolean gold) throws IOException, UnsupportedLocationException, UnsupportedFormatException {
		final InputStream in = CONLL09SentenceDataReaderTest.class.getResourceAsStream("icarus.conll09");
		final Location location = new Location.Base() {
			@Override
			public InputStream openInputStream() throws IOException { return in; }
		};
		final CONLL09SentenceDataReader reader = new CONLL09SentenceDataReader(gold);
		
		reader.init(location, Options.emptyOptions);
		
		List<SentenceData> sentences = new ArrayList<>();
		
		SentenceData data;
		while((data=reader.next()) != null) {
			sentences.add(data);
		}
		
		assertThat(sentences).hasSize(10);
		assertThat(sentences.get(0).length()).isEqualTo(7);
		assertThat(sentences.get(1).length()).isEqualTo(36);
	}

}
