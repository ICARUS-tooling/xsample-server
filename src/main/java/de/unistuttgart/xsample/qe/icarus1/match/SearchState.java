/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
public enum SearchState {
	
	/**
	 * Search object has been initialized but not started.
	 */
	BLANK,
	
	/**
	 * Search has been started.
	 */
	RUNNING,
	
	/**
	 * Search performed without interruptions.
	 */
	DONE,
	
	/**
	 * Search got cancelled either by user decision or by
	 * errors.
	 */
	CANCELLED;
}