/**
 * 
 */
package de.unistuttgart.xsample.qe;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

/**
 * @author Markus Gärtner
 *
 */
public class MappingException extends Exception {
	
	private static final long serialVersionUID = -6953230608145103722L;
	
	private final MappingErrorCode code;
	private Optional<String> resourceId = Optional.empty();

	public MappingException(String message, MappingErrorCode code, Throwable cause) {
		this(message, code, null, cause);
	}

	public MappingException(String message, MappingErrorCode code, String resourceId, Throwable cause) {
		super(message, cause);
		this.code = requireNonNull(code);
		this.resourceId = Optional.ofNullable(resourceId);
	}

	public MappingException(String message, MappingErrorCode code) {
		this(message, code, (String)null);
	}

	public MappingException(String message, MappingErrorCode code, String resourceId) {
		super(message);
		this.code = requireNonNull(code);
		this.resourceId = Optional.ofNullable(resourceId);
	}

	public MappingErrorCode getCode() { return code; }

	public Optional<String> getResourceId() { return resourceId; }
	public void setCorpusId(String corpusId) {
		this.resourceId = Optional.ofNullable(corpusId);
	}

	public enum MappingErrorCode {
		UNSUPPORTED_FORMAT,
		IO_ERROR,
		INTERNAL_ERROR,
		RESOURCE_LOCKED,
		SECURITY_ERROR,
		MISSING_MANIFEST,
		MISSING_MAPPING,
		;
	}
}
