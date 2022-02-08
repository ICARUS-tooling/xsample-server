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
 * Exception to indicate that a certain {@code Location} is not
 * supported by some method or framework. For example a reader
 * class might be able to only handle local files and would fail
 * to access remotely located data. This exception can also be used
 * when remote resources require some unsupported protocol or
 * are not accessible due to firewall or other technical means.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class UnsupportedLocationException extends Exception {

	private static final long serialVersionUID = 1648257446268571576L;
	
	private final Location location;

	/**
	 * 
	 */
	public UnsupportedLocationException(Location location) {
		this(null, location, null);
	}

	/**
	 * @param message
	 */
	public UnsupportedLocationException(String message, Location location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

	/**
	 * @param cause
	 */
	public UnsupportedLocationException(Throwable cause) {
		this(null, null, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedLocationException(String message, Throwable cause) {
		this(message, null, cause);
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
}
