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

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchEdge {
	
	private static final AtomicInteger idCounter = new AtomicInteger();
	
	private String id = "edge_"+idCounter.getAndIncrement(); //$NON-NLS-1$

	private SearchConstraint[] constraints = {};
	
	private SearchNode source;

	private SearchNode target;
	
	private boolean negated = false;
	
	private EdgeType edgeType = EdgeType.DOMINANCE;
	
	public SearchEdge(SearchEdge edge) {
		setSource(edge.getSource());
		setTarget(edge.getTarget());
		setConstraints(edge.getConstraints());
		setEdgeType(edge.getEdgeType());
		setNegated(edge.isNegated());
		setId(edge.getId());
	}

	public SearchEdge(SearchNode source, SearchNode target) {
		setSource(source);
		setTarget(target);
	}

	public SearchEdge(SearchNode source, SearchNode target, SearchConstraint[] constraints) {
		setSource(source);
		setTarget(target);
		setConstraints(constraints);
	}
	
	public SearchEdge() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getConstraints()
	 */
	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#isNegated()
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getEdgeType()
	 */
	public EdgeType getEdgeType() {
		return edgeType;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getSource()
	 */
	public SearchNode getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchEdge#getTarget()
	 */
	public SearchNode getTarget() {
		return target;
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
	}

	public void setSource(SearchNode source) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		this.source = source;
	}

	public void setTarget(SearchNode target) {
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		this.target = target;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public void setEdgeType(EdgeType edgeType) {
		if(edgeType==null)
			throw new NullPointerException("Invalid edgeType"); //$NON-NLS-1$
		this.edgeType = edgeType;
	}

	public String getId() {
		if(id==null || id.isEmpty())
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
