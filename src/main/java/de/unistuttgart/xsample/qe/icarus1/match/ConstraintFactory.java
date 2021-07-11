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

import de.unistuttgart.xsample.qe.icarus1.Options;

/**
 * Describes and
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ConstraintFactory {

	public static final int EDGE_CONSTRAINT_TYPE = 1;
	public static final int NODE_CONSTRAINT_TYPE = 2;

	SearchConstraint createConstraint(Object value, SearchOperator operator, Object specifier, Options options);

	SearchOperator[] getSupportedOperators();

	String getToken();

	/**
	 * Returns the class of supported values. This is a hint for editors
	 * or other user interface elements on what kind of component should
	 * be used to present the constraint. If the return value is {@code null}
	 * than only the values returned by {@link #getValueSet()} are considered
	 * legal!
	 */
	Class<?> getValueClass(Object specifier);

	/**
	 * Returns the value to be used as constraint in the case that
	 * no user input was made.
	 */
	Object getDefaultValue(Object specifier);

	/**
	 * Returns a collection of possible values that should be displayed to the
	 * user when editing the constraint. If {@link #getValueClass()} returns
	 * {@code null} these values are considered to be the only legal collection
	 * of possible values!
	 */
	Object[] getLabelSet(Object specifier);

	/**
	 * Transforms or parses the given {@code label} into a value
	 * suitable for {@code SearchConstraint} objects created by this factory.
	 */
	Object labelToValue(Object label, Object specifier);

	/**
	 * Transforms the given {@code value} into a {@code label} object
	 * that can be used for interface elements presented to the user.
	 */
	Object valueToLabel(Object value, Object specifier);

	int getConstraintType();

	// TODO add mechanics to create multiple instances of constraint and to
	// obtain min and max allowed instance count

	/**
	 * Returns the minimum required count of constraint instances
	 * created by this factory. A value of {@code -1} allows the
	 * user interface to make that decision.
	 */
	int getMinInstanceCount();

	/**
	 * Returns the maximum allowed count of constraint instances
	 * created by this factory. A value of {@code -1} deactivates
	 * the upper limit and allows the user interface to handle the
	 * decision. Note that aside from that reserved return value all
	 * values that are less than the current minimum as obtained from
	 * {@link #getMinInstanceCount()} will cause exceptions.
	 */
	int getMaxInstanceCount();

	Object[] getSupportedSpecifiers();
}
