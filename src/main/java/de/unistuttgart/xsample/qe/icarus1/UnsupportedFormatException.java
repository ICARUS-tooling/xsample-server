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
package de.unistuttgart.xsample.qe.icarus1;

/**
 * Signals that some data did not met the requirements defined
 * by some method or framework in terms of structural format.
 * For example a parser that reads data from a text-file would
 * use this exception to signal unexpected syntactical structures
 * he was unable to interpret or recognize.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class UnsupportedFormatException extends Exception {

	private static final long serialVersionUID = 4049528089908906476L;

	public UnsupportedFormatException() {
	}

	public UnsupportedFormatException(String message) {
		super(message);
	}

	public UnsupportedFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
