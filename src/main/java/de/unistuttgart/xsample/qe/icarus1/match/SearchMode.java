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
public enum SearchMode {

	/**
	 * Every single hit encountered in a target graph
	 * should be cached and the graph reported as a whole.
	 * This effectively implies exhaustive searching!
	 *
	 * @deprecated The interaction of this search mode and the {@link DefaultSearchOperator#GROUPING grouping operator}
	 * causes undesired side effects in result set creation that are not easily fixed. Therefore searching will be restricted
	 * to only use the other 2 "basic" modes!
	 */
	@Deprecated
	HITS("hits", true), //$NON-NLS-1$

	/**
	 * Every single hit encountered in a target graph
	 * should be reported independently. This effectively
	 * implies exhaustive searching!
	 */
	INDEPENDENT_HITS("independentHits", true), //$NON-NLS-1$

	/**
	 * Only the first hit in a target graph should be reported.
	 * Further processing of that graph is not necessary.
	 */
	MATCHES("matches", false); //$NON-NLS-1$

	private final String key;
	private final boolean exhaustive;

	private SearchMode(String key, boolean exhaustive) {
		this.key = key;
		this.exhaustive = exhaustive;
	}

	public boolean isExhaustive() {
		return exhaustive;
	}

	public static SearchMode[] supportedModes() {
		return new SearchMode[]{INDEPENDENT_HITS, MATCHES};
	}
}
