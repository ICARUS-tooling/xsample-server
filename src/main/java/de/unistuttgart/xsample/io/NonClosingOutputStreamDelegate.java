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
