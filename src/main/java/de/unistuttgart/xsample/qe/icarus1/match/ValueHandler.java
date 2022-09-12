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

import de.unistuttgart.xsample.qe.icarus1.LanguageConstants;
import de.unistuttgart.xsample.qe.icarus1.LanguageUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ValueHandler {

	public String valueToLabel(Object value) {
		return String.valueOf(value);
	}

	public Object labelToValue(Object label) {
		return label;
	}

	public Object[] getLabelSet() {
		return null;
	}

	public abstract Class<?> getValueClass();

	public abstract Object getDefaultValue();

	// Calc o1-o2
	public abstract Object substract(Object o1, Object o2);


	public static final ValueHandler stringHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return String.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		}

		@Override
		public Object substract(Object o1, Object o2) {
			throw new UnsupportedOperationException("Cannot substract strings!"); //$NON-NLS-1$
		}
	};

	public static final ValueHandler integerHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Integer.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseIntegerLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((int)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (int)o1-(int)o2;
		}
	};

	public static final ValueHandler longHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Long.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseIntegerLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((int)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (long)o1-(long)o2;
		}
	};

	public static final ValueHandler floatHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Float.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_FLOAT_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseFloatLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((float)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (float)o1-(float)o2;
		}
	};

	public static final ValueHandler doubleHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Double.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
			return LanguageUtils.parseDoubleLabel((String)label);
		}

		@Override
		public String valueToLabel(Object value) {
			return LanguageUtils.getLabel((double)value);
		}

		@Override
		public Object substract(Object o1, Object o2) {
			return (double)o1-(double)o2;
		}
	};

	public static final ValueHandler booleanHandler = new ValueHandler() {

		@Override
		public Class<?> getValueClass() {
			return Boolean.class;
		}

		@Override
		public Object getDefaultValue() {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		@Override
		public Object labelToValue(Object label) {
//			return LanguageUtils.parseBooleanLabel((String)label);
			if(LanguageConstants.DATA_GROUP_LABEL.equals(label)) {
				return LanguageConstants.DATA_GROUP_VALUE;
			} else if(LanguageConstants.DATA_UNDEFINED_LABEL.equals(label)) {
				return LanguageConstants.DATA_UNDEFINED_VALUE;
			} else {
				return Boolean.parseBoolean((String)label);
			}
		}

		@Override
		public String valueToLabel(Object value) {
//			return LanguageUtils.getBooleanLabel((int)value);
			if(value instanceof Integer) {
				return LanguageUtils.getBooleanLabel((int)value);
			} else {
				return Boolean.toString((boolean)value);
			}
		}

		@Override
		public Object[] getLabelSet() {
			return new Object[]{
					LanguageConstants.DATA_UNDEFINED_LABEL,
					valueToLabel(true),
					valueToLabel(false),
			};
		}

		@Override
		public Object substract(Object o1, Object o2) {
			throw new UnsupportedOperationException("Cannot substract boolean values!"); //$NON-NLS-1$
		}
	};
}
