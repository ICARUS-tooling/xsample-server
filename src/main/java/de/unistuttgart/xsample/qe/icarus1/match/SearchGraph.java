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
public class SearchGraph implements Cloneable {
	
	public static final int OPERATOR_CONJUNCTION = 1;
	public static final int OPERATOR_DISJUNCTION = 2;

	private int rootOperator = OPERATOR_CONJUNCTION;

	private SearchNode[] nodes;

	private SearchEdge[] edges;

	private SearchNode[] rootNodes;

	public SearchGraph() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchGraph#getNodes()
	 */
	public SearchNode[] getNodes() {
		return nodes;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchGraph#getEdges()
	 */
	public SearchEdge[] getEdges() {
		return edges;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchGraph#getRootNodes()
	 */
	public SearchNode[] getRootNodes() {
		return rootNodes;
	}

	public void setNodes(SearchNode[] nodes) {
		this.nodes = nodes;
	}

	public void setEdges(SearchEdge[] edges) {
		this.edges = edges;
	}

	public void setRootNodes(SearchNode[] rootNodes) {
		this.rootNodes = rootNodes;
	}

	/**
	 * Returns the operator to be applied in case that more than
	 * one independent sub-graph is contained within this {@code SearchGraph}.
	 * <p>
	 * Note that in the case of disjunction and groupings in different
	 * sub-graphs a mapping between them is required to aggregate the instances
	 * in the result. 
	 */
	public int getRootOperator() {
		return rootOperator;
	}

	public void setRootOperator(int rootOperator) {
		this.rootOperator = rootOperator;
	}

	@Override
	public SearchGraph clone() {
		SearchGraph graph = new SearchGraph();
		graph.rootOperator = rootOperator;
		graph.nodes = nodes==null ? null : nodes.clone();
		graph.edges = edges==null ? null : edges.clone();
		graph.rootNodes = rootNodes==null ? null : rootNodes.clone();
		return graph;
	}
}