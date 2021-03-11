/**
 * 
 */
package de.unistuttgart.xsample.io;

import java.io.IOException;

/**
 * @author Markus Gärtner
 *
 */
public class LocalIOException extends IOException {

	private static final long serialVersionUID = -8358900224137025023L;

	public LocalIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public LocalIOException(String message) {
		super(message);
	}

}
