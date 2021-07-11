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

import de.unistuttgart.xsample.qe.icarus1.LanguageConstants;
import de.unistuttgart.xsample.qe.icarus1.LanguageUtils;
import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.match.SearchConstraint;
import de.unistuttgart.xsample.qe.icarus1.match.SearchOperator;
import de.unistuttgart.xsample.qe.icarus1.match.TargetTree;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RelativeWordPositionConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "section"; //$NON-NLS-1$

	public RelativeWordPositionConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE);
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return Integer.class;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return SearchOperator.numerical();
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseIntegerLabel((String) label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getLabel((int)value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new RelativeWordPositionConstraint(value, operator);
	}

	private static class RelativeWordPositionConstraint extends SearchConstraint {

		private static final long serialVersionUID = -4951579070208503138L;

		public RelativeWordPositionConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			TargetTree tree = (TargetTree)value;
//			System.out.println((int) ((tree.getNodeIndex()+1)/(double)tree.size() * 100.0));
			return (int) ((1+tree.getNodeIndex())/(double)tree.size() * 100.0);
		}

		@Override
		public SearchConstraint clone() {
			return new RelativeWordPositionConstraint(getValue(), getOperator());
		}
	}
}
