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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class RootMatcher extends Matcher {

	public RootMatcher(SearchNode node) {
		super(node, null);

		exclusionMember = node.isNegated();
	}

	@Override
	public boolean matches() {

		int nodeCount = targetTree.size();

		boolean matched = false;

//		int minIndex = getMinIndex();
//		int maxIndex = getMaxIndex();

		indexIterator.setMax(nodeCount-1);

//		if(minIndex<=maxIndex) {
			while(indexIterator.hasNext()) {
				targetTree.viewNode(indexIterator.next());

				// Honor locked nodes that are allocated to other matchers!
				if(targetTree.isNodeLocked()) {
					continue;
				}

				// Check for precedence constraints
//				if(targetTree.getNodeIndex()<minIndex
//						|| targetTree.getNodeIndex()>maxIndex) {
//					continue;
//				}
				if(!isLegalIndex(targetTree.getNodeIndex())) {
					continue;
				}

				// Check for type constraints
				if(!matchesType()) {
					continue;
				}

				// Check for structural constraints
				if(targetTree.getDescendantCount()<descendantCount
						|| targetTree.getHeight()<height) {
					continue;
				}

				// Check for required number of children
				if(targetTree.getEdgeCount()<childCount) {
					continue;
				}

				// Check if the current node is a potential match
				if(!matchesConstraints()) {
					continue;
				}

				// Lock allocation
				allocate();

				// Search for child matchers that serve as exclusions
				if(!matchesExclusions()) {
					// Delegate further search to the next matcher
					// or otherwise commit current match
					matched |= matchesNext();
				}

				// Release lock
				deallocate();

				// Stop search if only one successful hit is required.
				// This is the case when either a non-exhaustive search
				// takes place or the matcher is a part of a sub-tree
				// serving as exclusion
				if(matched && (exclusionMember || !exhaustive)) {
					break;
				}
			}
//		}



		if(matched && !exclusionMember && previous==null && searchMode!=SearchMode.INDEPENDENT_HITS) {
//			commit();
		} else if(!matched || exhaustive) {
			// If unsuccessful and part of a disjunction let the
			// alternate matcher have a try.
			if(alternate!=null) {
				alternate.matches();
			}
		}

		return matched;
	}
}
