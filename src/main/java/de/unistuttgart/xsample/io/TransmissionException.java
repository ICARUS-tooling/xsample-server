/**
 * 
 */
package de.unistuttgart.xsample.io;

import java.io.IOException;

/**
 * @author Markus Gärtner
 *
 */
public class TransmissionException extends IOException {

	private static final long serialVersionUID = 2295895235486899165L;

	public TransmissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransmissionException(String message) {
		super(message);
	}

}
