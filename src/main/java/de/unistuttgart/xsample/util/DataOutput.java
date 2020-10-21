/**
 * 
 */
package de.unistuttgart.xsample.util;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;

/**
 * @author Markus Gärtner
 *
 */
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
