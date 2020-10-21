/**
 * 
 */
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.nio.charset.Charset;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;

/**
 * @author Markus Gärtner
 *
 */
public abstract class DataInput extends Payload {
	
	public static DataInput virtual(Charset encoding, String contentType, byte[] data) {
		return new VirtualDataInput(encoding, contentType, data);
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
}
