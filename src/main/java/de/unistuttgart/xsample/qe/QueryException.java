/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class QueryException extends Exception {

	private static final long serialVersionUID = 5487420639009729815L;
	
	private final QueryErrorCode code;
	private Optional<String> resourceId = Optional.empty();

	public QueryException(String message, QueryErrorCode code, Throwable cause) {
		this(message, code, null, cause);
	}

	public QueryException(String message, QueryErrorCode code, String resourceId, Throwable cause) {
		super(message, cause);
		this.code = requireNonNull(code);
		this.resourceId = Optional.ofNullable(resourceId);
	}

	public QueryException(String message, QueryErrorCode code) {
		this(message, code, (String)null);
	}

	public QueryException(String message, QueryErrorCode code, String resourceId) {
		super(message);
		this.code = requireNonNull(code);
		this.resourceId = Optional.ofNullable(resourceId);
	}

	public QueryErrorCode getCode() { return code; }

	public Optional<String> getResourceId() { return resourceId; }
	public void setCorpusId(String corpusId) {
		this.resourceId = Optional.ofNullable(corpusId);
	}

	public enum QueryErrorCode {
		SYNTAX_ERROR,
		UNSUPPORTED_FORMAT,
		IO_ERROR,
		INTERNAL_ERROR,
		RESOURCE_LOCKED,
		SECURITY_ERROR,
		;
	}
}
