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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum Orientation {

	LEFT_TO_RIGHT("leftToRight"), //$NON-NLS-1$
	
	RIGHT_TO_LEFT("rightToLeft"); //$NON-NLS-1$
	
	private final String key;
	
	private Orientation(String key) {
		this.key = key;
	}
}