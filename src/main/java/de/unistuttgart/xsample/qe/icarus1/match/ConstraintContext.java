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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unistuttgart.xsample.qe.icarus1.LanguageConstants;
import de.unistuttgart.xsample.qe.icarus1.match.cs.FormConstraintFactory;


/**
 * Holds all available tokens, aliases and {@code ConstraintFactory}
 * object associated with a certain {@code ContentType}. Note that
 * all methods that modify the content of a context are <i>non-destructive</i>
 * i.e. only {@code add} new factories, tokens or aliases but never
 * {@code remove} them!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintContext {

	private Set<String> tokens = new LinkedHashSet<>();
	private Set<String> requiredTokens = new HashSet<>();
	private Map<String, String> aliases = new HashMap<>();
	private Map<String, Object> factories = new HashMap<>();

	private List<ConstraintFactory> nodeFactoryCache;
	private List<ConstraintFactory> edgeFactoryCache;

	public void registerFactory(String token, Object factory) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		
		addToken(token);

		token = token.toLowerCase();

		if(factories.containsKey(token))
			throw new IllegalArgumentException("Duplicate factory for token: "+token+" in context"); //$NON-NLS-1$ //$NON-NLS-2$

		if(factory instanceof ConstraintFactory
				|| factory instanceof String
				|| factory instanceof Class
				/*|| factory instanceof ClassProxy*/) {
			factories.put(token, factory);
		} else
			throw new NullPointerException("Invalid factory: "+factory.getClass()+" for token '"+token+"' in context"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		nodeFactoryCache = null;
		edgeFactoryCache = null;
	}

	public void addToken(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$

		token = token.toLowerCase();

		if(tokens.contains(token))
			throw new IllegalArgumentException("Token '"+token+"' already registered to context "); //$NON-NLS-1$ //$NON-NLS-2$

		tokens.add(token);
	}

	public void addRequiredToken(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$

		token = token.toLowerCase();

		requiredTokens.add(token);
	}

	public void addAlias(String alias, String token) {
		if(alias==null || alias.isEmpty())
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$

		alias = alias.toLowerCase();

		if(aliases.containsKey(alias))
			throw new IllegalArgumentException("Alias '"+alias+"' already registered to context"); //$NON-NLS-1$ //$NON-NLS-2$

		aliases.put(alias, token);
	}

	public boolean isRegistered(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$

		token = token.toLowerCase();

		return factories.containsKey(token);
	}

	public boolean isRequired(String token) {
		return requiredTokens.contains(token.toLowerCase());
	}

	public ConstraintFactory getFactory(String token) {
		if(token==null || token.isEmpty())
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$

		token = token.toLowerCase();

		Object factory = factories.get(token);
		if(factory!=null && !(factory instanceof ConstraintFactory)) {
			try {
				if(factory instanceof String) {
					factory = Class.forName((String)factory);
				}
				if(factory instanceof Class) {
					factory = ((Class<?>)factory).newInstance();
				}/* else if(factory instanceof ClassProxy) {
					factory = ((ClassProxy)factory).loadObjectUnsafe();
				}*/

				// Refresh mapping
				factories.put(token, factory);
			} catch(Exception e) {
				factories.remove(token);
				factory = null;
				throw new IllegalStateException("Failed to instantiate constraint factory for token: "+token, e);
			}
		}

		return (ConstraintFactory) factory;
	}

	public Set<String> getTokens() {
		return Collections.unmodifiableSet(tokens);
	}

	public Set<String> getLegalTokens() {
		Set<String> result = new LinkedHashSet<>(tokens);
		result.addAll(aliases.keySet());

		return result;
	}

	public Set<String> getAliases() {
		return Collections.unmodifiableSet(aliases.keySet());
	}

	public String getToken(String alias) {
		if(alias==null || alias.isEmpty())
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$

		alias = alias.toLowerCase();

		return aliases.get(alias);
	}

	/**
	 * Finds a token that either directly is a completion of the
	 * given {@code fragment} string or has an alias that could
	 * serve as a completion. A string is considered to be a completion
	 * of the input {@code fragment} if its {@link String#startsWith(String)}
	 * method returns {@code true} with the {@code fragment} as argument.
	 * <p>
	 * This search is case-insensitive. If no token could be found
	 * {@code null} will be returned.
	 */
	public String completeToken(String fragment) {
		if(fragment==null || fragment.isEmpty())
			throw new NullPointerException("Invalid fragment"); //$NON-NLS-1$

		fragment = fragment.toLowerCase();

		// Try main tokens first
		for(String token : tokens) {
			if(token.startsWith(fragment)) {
				return token;
			}
		}

		// Now try aliases
		for(Entry<String, String> entry : aliases.entrySet()) {
			if(entry.getKey().startsWith(fragment)) {
				return entry.getValue();
			}
		}

		return null;
	}

	public List<ConstraintFactory> getFactories() {
		List<ConstraintFactory> result = new ArrayList<>(tokens.size());

		for(String token : tokens) {
			result.add(getFactory(token));
		}

		return result;
	}

	public List<ConstraintFactory> getNodeFactories() {
		if(nodeFactoryCache==null) {
			nodeFactoryCache = new ArrayList<>();

			for(String token : tokens) {
				ConstraintFactory factory = getFactory(token);
				if(factory!=null && factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
					nodeFactoryCache.add(factory);
				}
			}

			nodeFactoryCache = Collections.unmodifiableList(nodeFactoryCache);
		}

		return nodeFactoryCache;
	}

	public List<ConstraintFactory> getEdgeFactories() {
		if(edgeFactoryCache==null) {
			edgeFactoryCache = new ArrayList<>();

			for(String token : tokens) {
				ConstraintFactory factory = getFactory(token);
				if(factory!=null && factory.getConstraintType()==ConstraintFactory.EDGE_CONSTRAINT_TYPE) {
					edgeFactoryCache.add(factory);
				}
			}

			edgeFactoryCache = Collections.unmodifiableList(edgeFactoryCache);
		}

		return edgeFactoryCache;
	}

	public void addAll(ConstraintContext other) {
		if (other == null)
			throw new NullPointerException("Invalid other");  //$NON-NLS-1$

		tokens.addAll(other.tokens);
		requiredTokens.addAll(other.requiredTokens);
		aliases.putAll(other.aliases);
		for(Entry<String, Object> entry : other.factories.entrySet()) {
			if(factories.containsKey(entry.getKey()))
				throw new IllegalStateException("Constraint already registered in context: "+entry.getKey()); //$NON-NLS-1$ //$NON-NLS-2$

			factories.put(entry.getKey(), entry.getValue());
		}

		nodeFactoryCache = null;
		edgeFactoryCache = null;
	}
	
	public static ConstraintContext defaultContext() {
		ConstraintContext context = new ConstraintContext();
		
		context.registerFactory(LanguageConstants.FORM_KEY, FormConstraintFactory.class);
		//TODO
		
		return context;
	}
}