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
import de.unistuttgart.xsample.qe.icarus1.match.ConstraintFactory;
import de.unistuttgart.xsample.qe.icarus1.match.SearchOperator;
import de.unistuttgart.xsample.qe.icarus1.match.SharedPropertyRegistry;
import de.unistuttgart.xsample.qe.icarus1.match.ValueHandler;
  
/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractConstraintFactory implements ConstraintFactory {

	private String nameKey, descriptionKey;

	private String token;

	private int type;

	protected static final Object[] DEFAULT_UNDEFINED_VALUESET = {
		LanguageConstants.DATA_UNDEFINED_LABEL
	};

	public AbstractConstraintFactory(String token, int type) {
		this.token = token;
		this.type = type;
	}

	protected static ValueHandler getHandler(Object key) {
		return SharedPropertyRegistry.getHandler(key);
	}

	protected boolean isFlagSet(int flags, int mask) {
		return (flags & mask) == mask;
	}

	/**
	 *
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getConstraintType()
	 */
	@Override
	public int getConstraintType() {
		return type;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getToken() {
		return token;
	}


	@Override
	public Class<?> getValueClass(Object specifier) {
		return getHandler(specifier).getValueClass();
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return getHandler(specifier).getDefaultValue();
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return getHandler(specifier).labelToValue(label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return getHandler(specifier).valueToLabel(value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getSupportedOperators()
	 */
	@Override
	public SearchOperator[] getSupportedOperators() {
		return SearchOperator.values();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getValueSet()
	 */
	@Override
	public Object[] getLabelSet(Object specifier) {
		return DEFAULT_UNDEFINED_VALUESET;
	}

	@Override
	public int getMinInstanceCount() {
		return 0;
	}

	@Override
	public int getMaxInstanceCount() {
		return -1;
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return null;
	}
}
