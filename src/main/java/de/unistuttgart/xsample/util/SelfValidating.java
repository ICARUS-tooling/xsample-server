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
package de.unistuttgart.xsample.util;

import java.util.List;

/**
 * @author Markus Gärtner
 *
 */
public interface SelfValidating {

	/**
	 * Verifies integrity of this object.
	 * @throws IllegalStateException iff any check fails
	 */
	void validate();
	
	public static void validateNested(List<? extends SelfValidating> items, String name) {
		if(items==null) {
			throw new IllegalStateException("Missing '"+name+"' field");
		} else if(items.isEmpty()) {
			throw new IllegalStateException("Field '"+name+"' is empty");			
		}
		items.forEach(SelfValidating::validate);
	}
	
	public static void validateNested(SelfValidating item, String name) {
		if(item==null)
			throw new IllegalStateException("Missing '"+name+"' field");
		
		item.validate();
	}
	
	public static void validateOptionalNested(List<? extends SelfValidating> items) {
		if(items!=null) {
			items.forEach(SelfValidating::validate);	
		}
	}
	
	public static void validateOptionalNested(SelfValidating item) {
		if(item!=null) {
			item.validate();
		}
	}
}
