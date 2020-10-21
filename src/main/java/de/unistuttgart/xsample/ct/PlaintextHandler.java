/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.ct;

import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.util.Payload;

/**
 * @author Markus G�rtner
 *
 */
public class PlaintextHandler implements ExcerptHandler {
	
	//TODO currently we're not really unicode aware here
	
	private StringBuilder data;
	
	private void checkData() {
		if(data==null)
			throw new IllegalStateException("No text data available");
	}

	@Override
	public void close() throws IOException {
		data = null;
	}

	@Override
	public void init(Payload input) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		ReadableByteChannel ch = Channels.newChannel(input.inputStream());
		Reader reader = Channels.newReader(ch, input.encoding().newDecoder(), -1);
		char[] buffer = new char[1<<13];
		int read;
		while((read = reader.read(buffer)) > 0) {
			data.append(buffer, 0, read);
		}
	}

	@Override
	public long segments() {
		checkData();
		return data.length();
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(de.unistuttgart.xsample.Fragment[])
	 */
	@Override
	public void excerpt(Fragment[] fragments, Payload output) throws IOException {
		// TODO Auto-generated method stub
	}

}
