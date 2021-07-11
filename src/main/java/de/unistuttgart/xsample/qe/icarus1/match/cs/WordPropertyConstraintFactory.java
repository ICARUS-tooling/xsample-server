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
import de.unistuttgart.xsample.qe.icarus1.SentenceData;
import de.unistuttgart.xsample.qe.icarus1.match.SearchConstraint;
import de.unistuttgart.xsample.qe.icarus1.match.SearchOperator;
import de.unistuttgart.xsample.qe.icarus1.match.SearchParameters;
import de.unistuttgart.xsample.qe.icarus1.match.SharedPropertyRegistry;
import de.unistuttgart.xsample.qe.icarus1.match.TargetTree;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class WordPropertyConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "wordProperty"; //$NON-NLS-1$

	public WordPropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE);
	}

	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractConstraintFactory#getSupportedSpecifiers()
	 */
	@Override
	public Object[] getSupportedSpecifiers() {
		return SharedPropertyRegistry.getSpecifiers(SharedPropertyRegistry.WORD_LEVEL,
				SharedPropertyRegistry.INCLUDE_COMPATIBLE_TYPES | SharedPropertyRegistry.INCLUDE_GENERAL_LEVEL,
				SentenceData.class);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SearchParameters.SEARCH_CASESENSITIVE, SearchParameters.DEFAULT_SEARCH_CASESENSITIVE))
			return new WordPropertyConstraint(value, operator, specifier);
		else
			return new WordPropertyIConstraint(value, operator, specifier);
	}

	private static class WordPropertyConstraint extends SearchConstraint {

		private static final long serialVersionUID = -2520716674648713610L;

		public WordPropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getProperty(getKey());
		}

		@Override
		public SearchConstraint clone() {
			return new WordPropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class WordPropertyIConstraint extends SearchConstraint.CaseInsensitiveConstraint {

		private static final long serialVersionUID = -9209332012676323076L;

		public WordPropertyIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			Object p = ((TargetTree)value).getProperty(getKey());
			return p==null ? null : p.toString().toLowerCase();
		}

		@Override
		public WordPropertyIConstraint clone() {
			return new WordPropertyIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
