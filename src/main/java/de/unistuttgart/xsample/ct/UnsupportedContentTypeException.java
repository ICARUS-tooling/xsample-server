/**
 * 
 */
package de.unistuttgart.xsample.ct;

/**
 * @author Markus Gärtner
 *
 */
public class UnsupportedContentTypeException extends XsampleException {

	private static final long serialVersionUID = -4599333100873465625L;

	public UnsupportedContentTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedContentTypeException(String message) {
		super(message);
	}

}
