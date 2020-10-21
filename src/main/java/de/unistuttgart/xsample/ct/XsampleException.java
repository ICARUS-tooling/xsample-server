/**
 * 
 */
package de.unistuttgart.xsample.ct;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleException extends Exception {

	private static final long serialVersionUID = 8844973164047065333L;

	public XsampleException(String message, Throwable cause) {
		super(message, cause);
	}

	public XsampleException(String message) {
		super(message);
	}
}
