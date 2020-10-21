/**
 * 
 */
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Markus Gärtner
 *
 */
public abstract class Payload {
	
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
}
