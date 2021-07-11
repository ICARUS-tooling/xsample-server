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
public class FeaturesConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "features"; //$NON-NLS-1$

	public FeaturesConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SearchParameters.SEARCH_CASESENSITIVE, SearchParameters.DEFAULT_SEARCH_CASESENSITIVE))
			return new FeaturesConstraint(value, operator);
		else
			return new FeaturesCIConstraint(value, operator);
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[] {
			SearchOperator.EQUALS,
			SearchOperator.EQUALS_NOT,
			SearchOperator.CONTAINS,
			SearchOperator.CONTAINS_NOT,
			SearchOperator.MATCHES,
			SearchOperator.MATCHES_NOT,
		};
	}

	private static class FeaturesConstraint extends SearchConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public FeaturesConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getFeatures();
		}

		@Override
		public SearchConstraint clone() {
			return new FeaturesConstraint(getValue(), getOperator());
		}
	}

	private static class FeaturesCIConstraint extends SearchConstraint.CaseInsensitiveConstraint {

		private static final long serialVersionUID = -3346450454270312183L;

		public FeaturesCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getFeatures().toLowerCase();
		}

		@Override
		public FeaturesCIConstraint clone() {
			return new FeaturesCIConstraint(getValue(), getOperator());
		}
	}
}
