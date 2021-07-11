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
package de.unistuttgart.xsample.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple {@link OutputStream} implementation that wraps around
 * another {@link OutputStream}, delegating all method calls to 
 * it, <b>except</b> the {@link #close()} call!
 * 
 * @author Markus Gärtner
 *
 */
public class NonClosingOutputStreamDelegate extends OutputStream {

	private final OutputStream out;
	
	public NonClosingOutputStreamDelegate(OutputStream out) {
		this.out = requireNonNull(out);
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		/* do nothing */
	}
}
