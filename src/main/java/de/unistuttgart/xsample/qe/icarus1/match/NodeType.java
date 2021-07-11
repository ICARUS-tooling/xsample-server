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

import java.text.ParseException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum NodeType {

	/**
	 * Node without incoming edges and at least one outgoing edge
	 */
	ROOT("root"), //$NON-NLS-1$

	/**
	 * Node without outgoing edges and exactly one incoming edge
	 */
	LEAF("leaf"), //$NON-NLS-1$

	/**
	 * A node without restrictions
	 */
	GENERAL("general"), //$NON-NLS-1$

	/**
	 * Marks a node that serves as branching point within a disjunction
	 */
	DISJUNCTION("disjunction"), //$NON-NLS-1$

	/**
	 * A node that is neither a leaf nor a root.
	 */
	INTERMEDIATE("intermediate"), //$NON-NLS-1$

	/**
	 * A node that is not a root.
	 */
	NON_ROOT("non_root"), //$NON-NLS-1$

	/**
	 * A node that is not a leaf.
	 */
	PARENT("parent"); //$NON-NLS-1$

	private NodeType(String token) {
		this.token = token;
	}

	private String token;

	public String getToken() {
		return token;
	}

	public static NodeType parseNodeType(String s) throws ParseException {
		if(s==null || s.isEmpty())
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$

		s = s.toLowerCase();

		for(NodeType type : values()) {
			if(type.token.startsWith(s)) {
				return type;
			}
		}

		throw new ParseException("Unknown node type string: "+s, 0); //$NON-NLS-1$
	}
}
