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
public final class SearchParameters {

	public static final String SEARCH_MODE = "searchMode"; //$NON-NLS-1$

	public static final String SEARCH_ORIENTATION = "searchOrientation"; //$NON-NLS-1$

	public static final String SEARCH_CASESENSITIVE = "searchCaseSensitive"; //$NON-NLS-1$

	public static final String OPTIMIZE_SEARCH = "optimizeSearch"; //$NON-NLS-1$

	public static final String SEARCH_RESULT_LIMIT = "searchResultLimit"; //$NON-NLS-1$

	public static final String SEARCH_MIN_LENGTH = "searchMinLength"; //$NON-NLS-1$

	public static final String SEARCH_MAX_LENGTH = "searchMaxLength"; //$NON-NLS-1$

	public static final String SEARCH_NON_PROJECTIVE = "searchNonProjective"; //$NON-NLS-1$

	public static final SearchMode DEFAULT_SEARCH_MODE = SearchMode.MATCHES;
	public static final Orientation DEFAULT_SEARCH_ORIENTATION = Orientation.LEFT_TO_RIGHT;
	public static final boolean DEFAULT_SEARCH_CASESENSITIVE = true;
	public static final boolean DEFAULT_OPTIMIZE_SEARCH = false;
	public static final int DEFAULT_SEARCH_RESULT_LIMIT = 0;
	public static final int DEFAULT_SEARCH_MIN_LENGTH = 0;
	public static final int DEFAULT_SEARCH_MAX_LENGTH = 0;
	public static final boolean DEFAULT_SEARCH_NON_PROJECTIVE = false;
}
