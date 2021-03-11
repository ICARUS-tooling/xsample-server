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

import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class that extends basic {@link InputStream} and counts the total 
 * number of bytes being read.
 * 
 * @author Markus Gärtner
 *
 */
public class CountingSplitStream extends InputStream implements Flushable {

	/** Total bytes read  */
		private long count = 0;
  
		/** Original source */
	private final InputStream in;
	/** Destination for cloned input data */
	private final OutputStream out;
	
	public CountingSplitStream(InputStream in, OutputStream out) {
		this.in = requireNonNull(in);
		this.out = requireNonNull(out);
	}

	public long getCount() { return count; }

	@Override
	public int read() throws IOException {
		int b = in.read();
		if(b!=-1) {
			count++;
			out.write(b);
		}
		return b;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
//			System.out.printf("read b=%d off=%d len=%d%n",b.length,off, len);
		int read = in.read(b, off, len);
		if(read>0) {
			count += read;
			out.write(b, off, read);
		}
		return read;
	}

	@Override
	public long skip(long n) throws IOException {
//			System.out.printf("skip n=%d%n",n);
        long remaining = n;
        int nr;

        if (n <= 0) {
            return 0;
        }

        int size = strictToInt(Math.min(2048, remaining));
        byte[] skipBuffer = new byte[size];
        while (remaining > 0) {
            nr = read(skipBuffer, 0, (int)Math.min(size, remaining));
            if (nr < 0) {
                break;
            }
            remaining -= nr;
            out.write(skipBuffer, 0, nr);
        }

        return n - remaining;
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
        /* no-op */
	}

	@Override
	public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() { 
		return false;
	}

	@Override
	public void flush() throws IOException { out.flush(); }
}