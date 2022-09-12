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
import de.unistuttgart.xsample.qe.icarus1.match.SharedPropertyRegistry;
import de.unistuttgart.xsample.qe.icarus1.match.TargetTree;

/**
 * @author Markus Gärtner
 * @version $Id: WordPropertyConstraintFactory.java 389 2015-04-23 10:19:15Z mcgaerty $
 *
 */
public class SentencePropertyConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "sentenceProperty"; //$NON-NLS-1$

	public SentencePropertyConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE);
	}

	/**
	 * @see de.ims.icarus.search_tools.standard.AbstractConstraintFactory#getSupportedSpecifiers()
	 */
	@Override
	public Object[] getSupportedSpecifiers() {
		return SharedPropertyRegistry.getSpecifiers(SharedPropertyRegistry.SENTENCE_LEVEL);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		if(options.get(SearchParameters.SEARCH_CASESENSITIVE, SearchParameters.DEFAULT_SEARCH_CASESENSITIVE))
			return new SentencePropertyConstraint(value, operator, specifier);
		else
			return new SentencePropertyIConstraint(value, operator, specifier);
	}

	private static class SentencePropertyConstraint extends SearchConstraint {

		private static final long serialVersionUID = 426884206361332385L;

		public SentencePropertyConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getSource().getProperty(getKey());
		}

		@Override
		public SearchConstraint clone() {
			return new SentencePropertyConstraint(getValue(), getOperator(), getSpecifier());
		}
	}

	private static class SentencePropertyIConstraint extends SearchConstraint.CaseInsensitiveConstraint {

		private static final long serialVersionUID = -6552729430469568381L;

		public SentencePropertyIConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		public String getKey() {
			return getSpecifier().toString();
		}

		@Override
		public Object getInstance(Object value) {
			Object p = ((TargetTree)value).getSource().getProperty(getKey());
			return p==null ? null : p.toString().toLowerCase();
		}

		@Override
		public SentencePropertyIConstraint clone() {
			return new SentencePropertyIConstraint(getValue(), getOperator(), getSpecifier());
		}
	}
}
