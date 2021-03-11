/**
 * 
 */
package de.unistuttgart.xsample.io;

import java.io.IOException;

/**
 * @author Markus Gärtner
 *
 */
public class AccessException extends IOException {

	private static final long serialVersionUID = 8681291156666061358L;
	
	private final int code;

	public AccessException(String message, int code, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public AccessException(String message, int code) {
		super(message);
		this.code = code;
	}

	public int getCode() { return code; }
}
