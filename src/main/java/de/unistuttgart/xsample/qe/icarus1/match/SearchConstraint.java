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

import java.io.Serializable;

import de.unistuttgart.xsample.qe.icarus1.LanguageUtils;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchConstraint implements Serializable, Cloneable {

	private static final long serialVersionUID = 8086598627849516305L;

	private String token;

	private Object value;

	private Object specifier;

	private boolean active = true;

	private SearchOperator operator;

	public SearchConstraint(String token, Object value, SearchOperator operator) {
		init();

		setToken(token);
		setValue(value);
		setOperator(operator);
	}

	public SearchConstraint(String token, Object value, SearchOperator operator, Object specifier) {
		this(token, value, operator);

		setSpecifier(specifier);
	}

	public SearchConstraint(SearchConstraint source) {
		init();

		setToken(source.getToken());
		setValue(source.getValue());
		setOperator(source.getOperator());
		setSpecifier(source.getSpecifier());
		setActive(source.isActive());
	}

	protected void init() {
		// for subclasses
	}

	@SuppressWarnings("unused")
	private SearchConstraint() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getValue()
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getOperator()
	 */
	public SearchOperator getOperator() {
		return operator;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	public boolean matches(Object value) {
		return operator.apply(getInstance(value), getConstraint());
	}

	protected Object getConstraint() {
		return value;
	}

	protected boolean equals(Object value, Object constraint) {
		return value.equals(constraint);
	}

	protected boolean contains(Object value, Object constraint) {
		return ((String)value).contains((String)constraint);
	}

	protected boolean matches(Object value, Object constraint) {
		return SearchOperator.MATCHES.apply(value, constraint);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected int compare(Object value, Object constraint) {
		return ((Comparable)value).compareTo(constraint);
	}

	public Object getInstance(Object value) {
		return value;
	}

	@Override
	public SearchConstraint clone() {
		SearchConstraint clonedConstraint = null;
		try {
			clonedConstraint = (SearchConstraint) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
		clonedConstraint.active = false;
		return clonedConstraint;
	}

	@Override
	public String toString() {
		return String.format("[%s: %s%s%s]", getClass().getSimpleName(),  //$NON-NLS-1$
				token, operator.getSymbol(), value);
	}

	public void prepare() {
		// nothing to do here
	}

	public void setValue(Object value) {
		if(value==null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		this.value = value;
	}

	public void setOperator(SearchOperator operator) {
		if(operator==null)
			throw new NullPointerException("Invalid operator"); //$NON-NLS-1$
		this.operator = operator;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isUndefined()
	 */
	public boolean isUndefined() {
		// Considers the specifier, too!
		return LanguageUtils.isUndefined(value) && LanguageUtils.isUndefined(specifier);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getToken()
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		if(token==null)
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		this.token = token;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#setActive(boolean)
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isActive()
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getSpecifier()
	 */
	public Object getSpecifier() {
		return specifier;
	}

	public void setSpecifier(Object specifier) {
		this.specifier = specifier;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getLabel(java.lang.Object)
	 */
	public Object getLabel(Object value) {
		return value;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isMultiplexing()
	 */
	public boolean isMultiplexing() {
		return false;
	}
	

	public static class CaseInsensitiveConstraint extends SearchConstraint {
	
		private static final long serialVersionUID = -7648734660494017554L;
	
		protected Object lowercaseValue;
	
		public CaseInsensitiveConstraint(String token, Object value,
				SearchOperator operator) {
			super(token, value, operator);
		}
	
		public CaseInsensitiveConstraint(String token, Object value,
				SearchOperator operator, Object specifier) {
			super(token, value, operator, specifier);
		}
	
		@Override
		protected Object getConstraint() {
			return lowercaseValue;
		}
	
		@Override
		public void setValue(Object value) {
			super.setValue(value);
			lowercaseValue = String.valueOf(value).toLowerCase();
		}
	
		public Object getLowercaseValue() {
			return lowercaseValue;
		}
	
		/**
		 * @see de.ims.icarus.search_tools.standard.DefaultConstraint#clone()
		 */
		@Override
		public CaseInsensitiveConstraint clone() {
			return (CaseInsensitiveConstraint) super.clone();
		}
	}
}
