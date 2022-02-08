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

import java.io.IOException;

/**
 * @author Markus Gärtner
 *
 */
public class QueryException extends IOException {

	private static final long serialVersionUID = 5487420639009729815L;
	
	private final ErrorCode code;

	public QueryException(String message, ErrorCode code, Throwable cause) {
		super(message, cause);
		this.code = requireNonNull(code);
	}

	public QueryException(String message, ErrorCode code) {
		super(message);
		this.code = requireNonNull(code);
	}

	public ErrorCode getCode() { return code; }

	public enum ErrorCode {
		SYNTAX_ERROR,
		UNSUPPORTED_FORMAT,
		IO_ERROR,
		INTERNAL_ERROR,
		;
	}
}
