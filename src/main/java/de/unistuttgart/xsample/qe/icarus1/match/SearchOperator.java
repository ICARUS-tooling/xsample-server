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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SearchOperator implements Serializable {

	private static final long serialVersionUID = 4318248797354043349L;
	
	private final  String symbol;
	private final String key;
	private final boolean supportNumerical;
	
	/**
	 * @return the symbol
	 */
	public String getSymbol() {
		return symbol;
	}
	
	/**
	 * @return the supportNumerical
	 */
	public boolean isSupportNumerical() {
		return supportNumerical;
	}

	public abstract boolean apply(Object value, Object constraint);

	private SearchOperator(String symbol, String key, boolean supportNumerical) {
		this.symbol = symbol;
		this.key = key;
		this.supportNumerical = supportNumerical;
	}

	private static boolean equals0(Object value, Object constraint) {
		return value==null ? constraint==null : value.equals(constraint);
	}

	private static boolean contains0(Object value, Object constraint) {
		return value==null ? constraint==null : (value.toString()).contains(constraint.toString());
	}

	private static boolean matches0(Object value, Object constraint) {
		if(value==null) {
			return constraint==null;
		}

		Matcher matcher = getMatcher(constraint.toString(), value.toString());
		boolean result = matcher==null ? false : matcher.find();

		if(matcher!=null) {
			recycleMatcher(matcher);
		}

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int compare0(Object value, Object constraint) {
		return ((Comparable)value).compareTo(constraint);
	}


	// Maps strings to their compiled Pattern instance.
	// We use a weak hash map here since we only need the Pattern
	// as long as the respective string is used in some constraint
	private static Map<String, Matcher> matcherCache = Collections.synchronizedMap(
			new WeakHashMap<String, Matcher>());

	private static Matcher getMatcher(String s, String input) {
		if(s==null || s.isEmpty()) {
			return null;
		}

		Matcher matcher = matcherCache.remove(s);
		if(matcher==null) {
			// Do not catch PatternSyntaxException!
			// We want whatever operation the pattern request was originated
			// from to be terminated by the exception.
			matcher = Pattern.compile(s).matcher(input);

			// Do not bother with 'duplicates' since all Pattern
			// compiled from the same string are in fact identical in
			// terms of functionality
//			matcherCache.put(s, matcher);
		} else {
			matcher.reset(input);
		}

		return matcher;
	}

	private static void recycleMatcher(Matcher matcher) {
		if (matcher == null)
			throw new NullPointerException("Invalid matcher"); //$NON-NLS-1$

		matcher.reset();

		matcherCache.put(matcher.pattern().pattern(), matcher);
	}

	public static final SearchOperator EQUALS = new SearchOperator("=", "equals", true) {  //$NON-NLS-1$//$NON-NLS-2$

		private static final long serialVersionUID = -3692306391485959449L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return equals0(value, constraint);
		}
	};

	public static final SearchOperator EQUALS_NOT = new SearchOperator("!=", "equalsNot", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -4730832928170697565L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !equals0(value, constraint);
		}
	};

	public static final SearchOperator MATCHES = new SearchOperator("~", "matches", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -548739311862178925L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return matches0(value, constraint);
		}
	};

	public static final SearchOperator MATCHES_NOT = new SearchOperator("!~", "matchesNot", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -370237882408639045L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !matches0(value, constraint);
		}
	};

	public static final SearchOperator CONTAINS = new SearchOperator("#", "contains", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -8935758538857689576L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return contains0(value, constraint);
		}
	};

	public static final SearchOperator CONTAINS_NOT = new SearchOperator("!#", "containsNot", false) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 2110261744483750112L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return !contains0(value, constraint);
		}
	};

	public static final SearchOperator LESS_THAN = new SearchOperator("<", "lessThan", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -8353909321259706543L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)<0;
		}
	};

	public static final SearchOperator LESS_OR_EQUAL = new SearchOperator("<=", "lessOrEqual", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 6982415206383632031L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)<=0;
		}
	};

	public static final SearchOperator GREATER_THAN = new SearchOperator(">", "greaterThan", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = -3748593349088379755L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)>0;
		}
	};

	public static final SearchOperator GREATER_OR_EQUAL = new SearchOperator(">=", "greaterOrEqual", true) { //$NON-NLS-1$ //$NON-NLS-2$

		private static final long serialVersionUID = 5164052048370243973L;

		/**
		 * @see de.ims.icarus.search_tools.SearchOperator#apply(java.lang.Object, java.lang.Object)
		 */
		@Override
		public boolean apply(Object value, Object constraint) {
			return compare0(value, constraint)>=0;
		}
	};

	private static final SearchOperator[] operators = {
		EQUALS,
		EQUALS_NOT,
		MATCHES,
		MATCHES_NOT,
		CONTAINS,
		CONTAINS_NOT,
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
	};

	private static final SearchOperator[] numericalOperators = {
		EQUALS,
		EQUALS_NOT,
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
	};

	private static final SearchOperator[] booleanOperators = {
		EQUALS,
		EQUALS_NOT,
	};

	private static final SearchOperator[] comparingOperators = {
		LESS_THAN,
		LESS_OR_EQUAL,
		GREATER_THAN,
		GREATER_OR_EQUAL,
	};

	public static SearchOperator[] values() {
		return operators.clone();
	}

	public static SearchOperator[] numerical() {
		return numericalOperators.clone();
	}

	public static SearchOperator[] binary() {
		return booleanOperators.clone();
	}

	public static SearchOperator[] comparing() {
		return comparingOperators.clone();
	}
	
	private static Map<String, SearchOperator> available = new LinkedHashMap<>();
	static {
		for (SearchOperator operator : operators) {
			register(operator);
		}
	}

	public static void register(SearchOperator operator) {
		if(operator==null)
			throw new NullPointerException("Invalid operator"); //$NON-NLS-1$
		if(available.containsKey(operator.getSymbol()))
			throw new IllegalArgumentException("Duplicate operator symbol: "+operator.getSymbol()); //$NON-NLS-1$

		available.put(operator.getSymbol(), operator);
	}

	public static SearchOperator getOperator(String symbol) {
		return available.get(symbol);
	}

	public static Set<String> symbols() {
		return Collections.unmodifiableSet(available.keySet());
	}

	public static Collection<SearchOperator> operators() {
		return Collections.unmodifiableCollection(available.values());
	}
}
