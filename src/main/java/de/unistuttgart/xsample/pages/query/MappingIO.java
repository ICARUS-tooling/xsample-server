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
package de.unistuttgart.xsample.pages.query;

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.unistuttgart.xsample.pages.query.Mapping.Builder;

/**
 * @author Markus Gärtner
 *
 */
public class MappingIO {
	
	public static final char DELIMITER_TAB = '\t';

	/**
	 * Parses a tabular mapping file that follows a specific format per row of data:
	 * <pre>{@code <source-index>DELIMITER<target-begin>DELIMITER<target-end>}</pre>
	 * This method ignores all {@link String#isEmpty() empty} lines!!
	 * 
	 * @param builder pre-configured builder to accumulate the mapping data into
	 * @param reader the data source, expected to contain one mapping entry per row
	 * @param delimiter a single character or regualr expression used to split rows
	 * @return the result of {@link Builder#build()} after parsing all the content of {@code reader}
	 * @throws IOException 
	 */
	public static Mapping loadTabular(Mapping.Builder builder, Reader reader, String delimiter)
			throws IOException {
		requireNonNull(builder);
		requireNonNull(reader);
		checkNotEmpty(delimiter);
		
		final BufferedReader input = buffer(reader);
		final Matcher m = Pattern.compile(delimiter).matcher("");
		
		String line;
		int row = 1;
		while((line = input.readLine())!=null) {
			if(line.isEmpty()) {
				continue;
			}
			
			m.reset(line);
			
			// Go for source index
			if(!m.find())
				throw new IOException("Invalid mapping syntax (no delimiter at all) at line "+row);
			final long sourceIndex = Long.parseLong(m.group());
			
			// Go for target begin
			if(!m.find())
				throw new IOException("Invalid mapping syntax (no delimiter after source index) at line "+row);
			final long targetBegin = Long.parseLong(m.group());
			
			// Consume rest of line as target end
			final long targetEnd = Long.parseLong(line.substring(m.end()));
			
			builder.addMapping(sourceIndex, targetBegin, targetEnd);
		}
		
		return builder.build();
	}
}
