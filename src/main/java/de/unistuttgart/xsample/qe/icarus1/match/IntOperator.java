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
public enum IntOperator {

	EQUALS {
		@Override
		public boolean apply(int value, int constraint) {
			return value==constraint;
		}
	},

	EQUALS_NOT {
		@Override
		public boolean apply(int value, int constraint) {
			return value!=constraint;
		}
	},

	LESS_THAN {
		@Override
		public boolean apply(int value, int constraint) {
			return value<constraint;
		}
	},

	LESS_OR_EQUALS {
		@Override
		public boolean apply(int value, int constraint) {
			return value<=constraint;
		}
	},

	GREATER_THAN {
		@Override
		public boolean apply(int value, int constraint) {
			return value>constraint;
		}
	},

	GREATER_OR_EQUALS {
		@Override
		public boolean apply(int value, int constraint) {
			return value>=constraint;
		}
	},

	;

	public abstract boolean apply(int value, int constraint);

	public static IntOperator fromSymbol(String s) {
		switch (s) {
		case "=": return EQUALS; //$NON-NLS-1$
		case "!=": return EQUALS_NOT; //$NON-NLS-1$
		case ">": return GREATER_THAN; //$NON-NLS-1$
		case ">=": return GREATER_OR_EQUALS; //$NON-NLS-1$
		case "<": return LESS_THAN; //$NON-NLS-1$
		case "<=": return LESS_OR_EQUALS; //$NON-NLS-1$

		default:
			return null;
		}
	}
}
