/**
 * 
 */
package de.unistuttgart.xsample.io;

import java.io.IOException;

/**
 * @author Markus Gärtner
 *
 */
public class InternalServerException extends IOException {

	private static final long serialVersionUID = 5487420639009729815L;
	
	private final int code;

	public InternalServerException(String message, int code, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public InternalServerException(String message, int code) {
		super(message);
		this.code = code;
	}

	public int getCode() { return code; }

}
