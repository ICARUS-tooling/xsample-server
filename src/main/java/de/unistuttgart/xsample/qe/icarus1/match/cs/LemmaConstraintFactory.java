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
package de.unistuttgart.xsample.qe.icarus1.match.cs;

import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.match.SearchConstraint;
import de.unistuttgart.xsample.qe.icarus1.match.SearchOperator;
import de.unistuttgart.xsample.qe.icarus1.match.SearchParameters;
import de.unistuttgart.xsample.qe.icarus1.match.TargetTree;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LemmaConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "lemma"; //$NON-NLS-1$

	public LemmaConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SearchParameters.SEARCH_CASESENSITIVE, SearchParameters.DEFAULT_SEARCH_CASESENSITIVE))
			return new LemmaConstraint(value, operator);
		else
			return new LemmaCIConstraint(value, operator);
	}

	private static class LemmaConstraint extends SearchConstraint {

		private static final long serialVersionUID = -2816057046153547371L;

		public LemmaConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getLemma();
		}

		@Override
		public SearchConstraint clone() {
			return new LemmaConstraint(getValue(), getOperator());
		}
	}

	private static class LemmaCIConstraint extends SearchConstraint.CaseInsensitiveConstraint {

		private static final long serialVersionUID = -8582367322352411091L;

		public LemmaCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getLemma().toLowerCase();
		}

		@Override
		public LemmaCIConstraint clone() {
			return new LemmaCIConstraint(getValue(), getOperator());
		}
	}
}
