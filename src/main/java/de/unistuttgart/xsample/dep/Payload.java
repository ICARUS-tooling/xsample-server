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
package de.unistuttgart.xsample.dep;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Markus Gärtner
 *
 */
@Deprecated
public abstract class Payload {
	
	public static Payload forInput(Charset encoding, String contentType, InputStream input) {
		return new Payload(encoding, contentType) {
			@Override
			public InputStream inputStream() { return input; }
		};
	}
	
	public static Payload forOutput(Charset encoding, String contentType, OutputStream output) {
		return new Payload(encoding, contentType) {
			@Override
			public OutputStream outputStream() { return output; }
		};
	}
	
	private final Charset encoding;
	private final String contentType;
	
	protected Payload(Charset encoding, String contentType) {
		this.encoding = requireNonNull(encoding);
		this.contentType = requireNonNull(contentType);
	}

	/** 
	 * Returns custom character encoding of the resource or {@link StandardCharsets#UTF_8}
	 * if the resource does not specify or require a separate character encoding 
	 * (e.g. if it is a binary data file, such as PDF). 
	 */
	public Charset encoding() { return encoding; }
	
	/** Returns the content type of the underlying resource, such as {@code application/pdf}. */
	public String contentType() { return contentType; }
	
	public InputStream inputStream() {
		throw new IllegalStateException("Not designed for reading");
	}
	
	public OutputStream outputStream() {
		throw new IllegalStateException("Not designed for writing");
	}
	
	@Deprecated
	public enum Type {
		READ,
		WRITE,
		READ_WRITE,
		;
	}
}
