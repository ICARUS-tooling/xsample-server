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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchNode {
	
	private static final AtomicInteger idCounter = new AtomicInteger();
	
	private String id = "node_"+idCounter.getAndIncrement(); //$NON-NLS-1$
	
	private NodeType nodeType = NodeType.GENERAL;
	
	private SearchConstraint[] constraints;
	
	private List<SearchEdge> incomingEdges;
	
	private List<SearchEdge> outgoingEdges;
	
	private boolean negated;
	
	private int height = -1;
	
	private int descendantCount = -1;
	
	private int childCount = -1;

	public SearchNode() {
		// no-op
	}
	
	public SearchNode(SearchNode node) {
		setId(node.getId());
		setNegated(node.isNegated());

		// TODO maintain source value of edge
		for(int i=0; i<node.getIncomingEdgeCount(); i++) {
			addEdge(node.getIncomingEdgeAt(i), true);
		}

		// TODO maintain source value of edge
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			addEdge(node.getOutgoingEdgeAt(i), false);
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getConstraints()
	 */
	public SearchConstraint[] getConstraints() {
		return constraints;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#isNegated()
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getOutgoingEdgeCount()
	 */
	public int getOutgoingEdgeCount() {
		return outgoingEdges==null ? 0 : outgoingEdges.size();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getOutgoingEdgeAt(int)
	 */
	public SearchEdge getOutgoingEdgeAt(int index) {
		return outgoingEdges.get(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getIncomingEdgeCount()
	 */
	public int getIncomingEdgeCount() {
		return incomingEdges==null ? 0 : incomingEdges.size();
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getIncomingEdgeAt(int)
	 */
	public SearchEdge getIncomingEdgeAt(int index) {
		return incomingEdges.get(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getHeight()
	 */
	public int getHeight() {
		
		if(height==-1) {
			int value = 0;
			
			if(outgoingEdges!=null) {
				for(SearchEdge edge : outgoingEdges) {
					value = Math.max(value, edge.getTarget().getHeight());
				}
			}
			
			height = value + 1;
		}
		
		return height;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getDescendantCount()
	 */
	public int getDescendantCount() {
		
		if(descendantCount==-1) {
			int value = 0;
			
			if(outgoingEdges!=null) {
				value = outgoingEdges.size();
				for(SearchEdge edge : outgoingEdges) {
					value += edge.getTarget().getDescendantCount();
				}
			}
			descendantCount = value;
		}
		
		return descendantCount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setConstraints(SearchConstraint[] constraints) {
		this.constraints = constraints;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	/**
	 * 
	 * @see de.ims.icarus.search_tools.SearchNode#getNodeType()
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
		
		childCount = -1;
	}

	public void addEdge(SearchEdge edge, boolean incoming) {
		if(edge==null)
			throw new NullPointerException("Invalid edge"); //$NON-NLS-1$
		
		if(incoming) {
			if(incomingEdges==null) {
				incomingEdges = new ArrayList<>();
			}
			incomingEdges.add(edge);
		} else {
			if(outgoingEdges==null) {
				outgoingEdges = new ArrayList<>();
			}
			outgoingEdges.add(edge);
		}
		
		height = -1;
		descendantCount = -1;
		childCount = -1;
	}
	
	public void addEdges(Collection<SearchEdge> newEdges, boolean incoming) {
		if(newEdges==null)
			throw new NullPointerException("Invalid outgoingEdges"); //$NON-NLS-1$
		if(newEdges.isEmpty()) {
			return;
		}
		
		if(incoming) {
			if(incomingEdges==null) {
				incomingEdges = new ArrayList<>();
			}
			incomingEdges.addAll(newEdges);
		} else {
			if(outgoingEdges==null) {
				outgoingEdges = new ArrayList<>();
			}
			outgoingEdges.addAll(outgoingEdges);
		}
		
		height = -1;
		descendantCount = -1;
		childCount = -1;
	}
	
	public void sortEdges(Comparator<SearchEdge> comparator) {
		if(comparator==null)
			throw new NullPointerException("Invalid comparator"); //$NON-NLS-1$
		
		Collections.sort(outgoingEdges, comparator);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchNode#getChildCount()
	 */
	public int getChildCount() {
		
		if(childCount==-1) {
			if(nodeType==NodeType.LEAF || negated 
					|| outgoingEdges==null || outgoingEdges.isEmpty()) {
				childCount = 0;
			} else if(nodeType==NodeType.DISJUNCTION) {
				childCount = 1;

				for(SearchEdge edge : outgoingEdges) {
					if(edge.getTarget().isNegated()) {
						childCount = 0;
						break;
					}
				}
			} else {
				childCount = 0;
				for(SearchEdge edge : outgoingEdges) {
					SearchNode node = edge.getTarget();
					if(node.getNodeType()==NodeType.DISJUNCTION) {
						childCount += node.getChildCount();
					} else {
						childCount++;
					}
				}
			}
		}
		
		return childCount;
	}
}
