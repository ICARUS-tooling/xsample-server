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
package de.unistuttgart.xsample.qe.icarus1;

import java.io.IOException;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CONLL09SentenceDataReader implements SentenceDataReader {
	
	private final boolean gold;

	protected CharTableBuffer buffer;
	protected int count;

	public CONLL09SentenceDataReader(boolean gold) {
		this.gold = gold;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#init(de.ims.icarus.util.location.Location,
	 *      de.ims.icarus.util.Options)
	 */
	@Override 
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {

		if (options == null) {
			options = Options.emptyOptions;
		}

		count = 0;

		buffer = new CharTableBuffer();

		buffer.startReading(IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options)));
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#next()
	 */
	@Override
	public SentenceData next() throws IOException, UnsupportedFormatException {

		SentenceData resultdd = null;

		if (buffer.next()) {
			try {
				if(gold) {
					resultdd = CONLLUtils.readGold09(buffer, count++);
				} else {
					resultdd = CONLLUtils.readPredicted09(buffer, count++);
				}
			} catch(Exception e) {
				// Cannot be IOException or UnsupportedFormatException

				throw new IOException(buffer.getErrorMessage("Failed to read predicted CoNLL09 data"), e); //$NON-NLS-1$
			}
		}

		return resultdd;
	}

	/**
	 * @see de.ims.icarus.language.SentenceDataReader#close()
	 */
	@Override
	public void close() {
		try {
			buffer.close();
		} catch (IOException e) {
			throw new InternalError(e);
		}
	}
}
