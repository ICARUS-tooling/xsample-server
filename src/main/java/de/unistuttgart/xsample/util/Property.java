/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

/**
 * @author Markus G�rtner
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
