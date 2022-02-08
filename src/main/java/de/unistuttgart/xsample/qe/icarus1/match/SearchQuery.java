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

import java.util.HashMap;
import java.util.Map;

import de.unistuttgart.xsample.qe.icarus1.UnsupportedFormatException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchQuery implements Cloneable {

	protected SearchGraph graph;
	protected String query;

	protected Map<String, Object> properties;

	protected QueryParser parser;
	protected final ConstraintContext constraintContext;

	public SearchQuery(ConstraintContext constraintContext) {
		if(constraintContext==null)
			throw new NullPointerException("Invalid constraint context"); //$NON-NLS-1$

		this.constraintContext = constraintContext;

		graph = new SearchGraph();
		query = ""; //$NON-NLS-1$
	}

	protected QueryParser createParser() throws Exception {
		return new QueryParser(getConstraintContext(), null);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#parseQueryString(java.lang.String)
	 */
	public void parseQueryString(String query)
			throws UnsupportedFormatException {
		if(query==null)
			throw new NullPointerException("Invalid query"); //$NON-NLS-1$

		if(this.query!=null && this.query.equals(query)) {
			return;
		}

		this.query = query;

		try {
			queryToGraph();
		} catch (Exception e) {
			throw new UnsupportedFormatException(
					"Error while parsing query", e); //$NON-NLS-1$
		}
	}

	protected void graphToQuery() throws Exception {
		if(graph==null) {
			query = null;
			return;
		}

		if(parser==null) {
			parser = createParser();
		}

		query = parser.toQuery(graph, null);
	}

	protected void queryToGraph() throws Exception {
		if(query==null) {
			graph = null;
			return;
		}

		if(parser==null) {
			parser = createParser();
		}

		graph = parser.parseQuery(query, null);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getQueryString()
	 */
	public String getQueryString() {
		return query;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
		}

		properties.put(key, value);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getSearchGraph()
	 */
	public SearchGraph getSearchGraph() {
		return graph;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#setSearchGraph(de.ims.icarus.search_tools.SearchGraph)
	 */
	public void setSearchGraph(SearchGraph graph) throws UnsupportedFormatException {
		if(graph==null)
			throw new NullPointerException("Invalid graph"); //$NON-NLS-1$

		if(this.graph!=null && this.graph.equals(graph)) {
			return;
		}

		this.graph = graph;

		try {
			graphToQuery();
		} catch (Exception e) {
			throw new UnsupportedFormatException(
					"Error while converting graph to query", e); //$NON-NLS-1$
		}
	}

	@Override
	public SearchQuery clone() {
		SearchQuery clone = new SearchQuery(getConstraintContext());
		clone.graph = graph.clone();
		clone.query = query;
		clone.parser = parser;

		if(properties!=null) {
			clone.properties = new HashMap<>(properties);
		}

		return clone;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchQuery#getConstraintContext()
	 */
	public ConstraintContext getConstraintContext() {
		return constraintContext;
	}
}
