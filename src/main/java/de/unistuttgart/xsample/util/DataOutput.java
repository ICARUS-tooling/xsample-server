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
package de.unistuttgart.xsample.util;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

/**
 * @author Markus G�rtner
 *
 */
@Deprecated
public abstract class DataOutput extends Payload {
	
	public static DataOutput virtual(String contentType, Charset encoding) { 
		return new VirtualDataOutput(encoding, contentType); 
	}

	private DataOutput(Charset encoding, String contentType) {
		super(encoding, contentType);
	}
	
	/** Provides write access to the content of the resource as a (buffered) stream. */
	public abstract OutputStream content(); 
	
	public abstract DataInput bridge();

	@VisibleForTesting
	static class VirtualDataOutput extends DataOutput {
		
		private final FastByteArrayOutputStream out;

		VirtualDataOutput(Charset encoding, String contentType) {
			super(encoding, contentType);
			out = new FastByteArrayOutputStream();
		}

		@Override
		public OutputStream content() { return out; }
		
		@VisibleForTesting
		byte[] getData() { return Arrays.copyOf(out.array, out.length); }

		@Override
		public DataInput bridge() {
			return DataInput.virtual(encoding(), contentType(), getData());
		}
	}
}
