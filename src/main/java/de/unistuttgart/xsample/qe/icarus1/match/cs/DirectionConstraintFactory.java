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
public class DirectionConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "direction"; //$NON-NLS-1$

	public DirectionConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new DirectionConstraint(value, operator);
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return null;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				SearchOperator.EQUALS,
		};
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseDirectionLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getDirectionLabel((int)value);
	}

	@Override
	public Object[] getLabelSet(Object specifier) {
		return new Object[]{
				LanguageConstants.DATA_UNDEFINED_LABEL,
				LanguageConstants.DATA_LEFT_LABEL,
				LanguageConstants.DATA_RIGHT_LABEL,
		};
	}

	private static class DirectionConstraint extends SearchConstraint {

		private static final long serialVersionUID = 8874429868140453623L;

		public DirectionConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getLabel(Object value) {
			return LanguageUtils.getDirectionLabel((int)value);
		}

		@Override
		public Object getInstance(Object value) {
			return ((TargetTree)value).getDirection();
		}

		@Override
		public SearchConstraint clone() {
			return new DirectionConstraint(getValue(), getOperator());
		}
	}
}
