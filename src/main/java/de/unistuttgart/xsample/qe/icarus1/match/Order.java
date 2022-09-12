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
package de.unistuttgart.xsample.qe.icarus1.match;

/**
 * Type safe definitions for the order possible bewteen two objects.  
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum Order {
	
	/**
	 * Indicates that for two objects {@code A} and {@code B} and order
	 * is defined such that {@code A} is placed <i>before</i> {@code B}.
	 */
	BEFORE("before"), //$NON-NLS-1$

	/**
	 * Indicates that for two objects {@code A} and {@code B} and order
	 * is defined such that {@code B} is placed <i>before</i> {@code A}.
	 */
	AFTER("after"), //$NON-NLS-1$
	
	/**
	 * No particular order exists between the two objects.
	 */
	UNDEFINED("undefined"); //$NON-NLS-1$
	
	private final String token;
	
	private Order(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}

	public static Order parseOrder(String s) {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$
		
		for(Order order : values()) {
			if(order.name().toLowerCase().startsWith(s)) {
				return order;
			}
		}
		
		throw new IllegalArgumentException("Unknown order string: "+s); //$NON-NLS-1$
	}
}
