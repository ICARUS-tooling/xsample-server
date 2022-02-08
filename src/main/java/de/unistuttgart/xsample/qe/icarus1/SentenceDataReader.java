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
package de.unistuttgart.xsample.qe.icarus1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 

/**
 * Deserialization handler for {@code SentenceData} objects.
 * It is used to sequentially access {@code SentenceData} from
 * arbitrary locations. Typically a {@code SentenceDataReader}
 * is not thread-safe since {@link #init(Location, Options)} could alter
 * the {@code location} of the reader from another thread while
 * the original {@code "owner-thread"} is still accessing data via
 * {@link #next()}. For this reason every entity that uses a reader
 * should obtain a private instance and not share it!
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface SentenceDataReader {

	/**
	 * Sets the {@code Location} to load data from and initializes
	 * internal state so that calls to {@link #next()} will actually
	 * start to read {@code SentenceData} objects.
	 * 
	 * @param location the {@code Location} to load data from
	 * @param options a collection of additional info for the reader
	 * @throws IOException forwarding of encountered {@code IOException}s
	 * @throws UnsupportedLocationException if the provided {@code Location}
	 * is not supported or not valid
	 */
	void init(Location location, Options options) throws IOException, UnsupportedLocationException;
		
	/**
	 * Returns the next {@code SentenceData} object available or {@code null}
	 * if the end of the {@code "data stream"} is reached. {@code IOException}s
	 * should simply be forwarded to the calling method and in the case of
	 * data that is {@code unreadable} for this reader an {@code UnsupportedFormatException}
	 * should be thrown instead of returning {@code null}.
	 * @return the next {@code SentenceData} object available for this reader or
	 * {@code null} if the end of the {@code "data stream"} is reached
	 * @throws IOException simple forwarding of encountered {@code IOException}s
	 * @throws UnsupportedFormatException if the reader could not construct
	 * a new {@code SentenceData} object from the loaded data.
	 */
	SentenceData next() throws IOException, UnsupportedFormatException;
	
	/**
	 * Closes all underlying I/O-objects. Subsequent calls to {@link #next()}
	 * should throw {@code IOException}.
	 */
	void close() throws IOException;
	
	default List<SentenceData> readAll(Location location, Options options) throws IOException, UnsupportedLocationException, UnsupportedFormatException {
		init(location, options);
		
		List<SentenceData> sentences = new ArrayList<>();
		
		SentenceData sentence;
		while((sentence=next()) != null) {
			sentences.add(sentence);
		}
		
		return sentences;
	}
}
