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

import java.text.ParseException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum EdgeType {

	/**
	 * Marks the edge as an empty connection that only serves
	 * as a link between two nodes and does not carry any
	 * constraints.
	 */
	LINK("link"), //$NON-NLS-1$

	/**
	 * Marks the edge as active part of some functional relation.
	 * Enables all kinds of constraints on the edge to be taken
	 * into account when used in a matching process.
	 */
	DOMINANCE("dominance"), //$NON-NLS-1$
	
	/**
	 * Transitive closure. Assigned edge matches any directed path
	 * between two nodes that are matched by the source and target
	 * {@code SearchNode} of this edge. Constraints set for this edge
	 * might be used for the initial matching check but subsequent
	 * expansions should ignore them.
	 * <p>
	 * This edge type cannot be used together with negation!
	 */
	TRANSITIVE("transitive"), //$NON-NLS-1$
	
	/**
	 * Matching against total order of source and target {@code SearchNode}.
	 * Note that the concrete order is data specific and may vary. Usually
	 * it is given by the initial order of word tokens within a sentence
	 * that are represented by nodes in the graph.
	 */
	PRECEDENCE("precedence"); //$NON-NLS-1$
	
	private EdgeType(String token) {
		this.token = token;
	}
	
	private String token;
	
	public String getToken() {
		return token;
	}

	public static EdgeType parseEdgeType(String s) throws ParseException {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$
		
		s = s.toLowerCase();
		
		for(EdgeType type : values()) {
			if(type.token.startsWith(s)) {
				return type;
			}
		}
		
		throw new ParseException("Unknown edge type string: "+s, 0); //$NON-NLS-1$
	}
}
