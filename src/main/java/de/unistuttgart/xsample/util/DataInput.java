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

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

/**
 * @author Markus G�rtner
 *
 */
@Deprecated
public abstract class DataInput extends Payload {
	
	public static DataInput virtual(Charset encoding, String contentType, byte[] data) {
		return new VirtualDataInput(encoding, contentType, data);
	}
	
	public static DataInput wrapped(Charset encoding, String contentType, Supplier<InputStream> source) {
		return new WrappedDataInput(encoding, contentType, source);
	}
	
	
	private DataInput(Charset encoding, String contentType) {
		super(encoding, contentType);
	}

	/** Returns actual content of the resource as a (buffered) stream. */
	public abstract InputStream content(); 

	@VisibleForTesting
	static final class VirtualDataInput extends DataInput {

		private final byte[] data;
		
		VirtualDataInput(Charset encoding, String contentType, byte[] data) {
			super(encoding, contentType);
			this.data = requireNonNull(data);
		}
		
		@Override
		public InputStream content() { return new FastByteArrayInputStream(data); }
		
		@VisibleForTesting
		byte[] getData() { return data; }
	}
	
	static final class WrappedDataInput extends DataInput {
		private final Supplier<InputStream> source;

		WrappedDataInput(Charset encoding, String contentType, Supplier<InputStream> source) {
			super(encoding, contentType);
			this.source = requireNonNull(source);
		}

		@Override
		public InputStream content() { return requireNonNull(source.get(), "soruce produced invalid stream"); }
	}
}
