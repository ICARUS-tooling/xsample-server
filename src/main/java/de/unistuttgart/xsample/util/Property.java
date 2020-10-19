/**
 * 
 */
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

/**
 * @author Markus Gärtner
 *
 */
public class Property implements Serializable {

	private static final long serialVersionUID = 2579391883796737266L;

	private final String key, value;

	public Property(String key, String value) {
		this.key = requireNonNull(key, "key missing");
		this.value = requireNonNull(value, "value missing");
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
