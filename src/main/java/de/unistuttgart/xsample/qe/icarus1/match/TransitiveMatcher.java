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

import java.util.Stack;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TransitiveMatcher extends Matcher {

	protected Stack<IndexIterator> iteratorCache = new Stack<>();

	protected boolean matched = false;

	public TransitiveMatcher(SearchNode node, SearchEdge edge) {
		super(node, edge);
	}

	@Override
	protected void innerClose() {
		iteratorCache.clear();
	}

	protected IndexIterator newIterator() {
		IndexIterator iterator = iteratorCache.isEmpty() ? null : iteratorCache.pop();

		if(iterator==null) {
			iterator = indexIterator.clone();
		}

		return iterator;
	}

	protected void recycleIterator(IndexIterator iterator) {
		iteratorCache.push(iterator);
	}

	@Override
	public boolean matches() {
		int parentAllocation = parent.getAllocation();

		//FIXME switch to the isLegalIndex(int) method  and traverse space instead of premature restriction
		int minIndex = getMinIndex();
		int maxIndex = getMaxIndex();

		matched = false;

		if(minIndex<=maxIndex) {
			search(parentAllocation, minIndex, maxIndex);
		}

		// Return scope to parent node
		targetTree.viewNode(parentAllocation);

		// If unsuccessful and part of a disjunction let the
		// alternate matcher have a try.
		if(!matched && alternate!=null) {
			matched = alternate.matches();
		}

		return matched;
	}

	protected void search(int index, int minIndex, int maxIndex) {

		targetTree.viewNode(index);
		indexIterator.setMax(targetTree.getEdgeCount()-1);

		// Early return in case of unfruitful path
		if(!indexIterator.hasNext()) {
			return;
		}

		while(indexIterator.hasNext()) {
			targetTree.viewNode(index);
			targetTree.viewChild(indexIterator.next());

			// Check for precedence constraints
			if(targetTree.getNodeIndex()<minIndex
					|| targetTree.getNodeIndex()>maxIndex) {
				continue;
			}

			// Honor locked nodes that are allocated to other matchers!
			if(targetTree.isNodeLocked()) {
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

			if(isDone()) {
				return;
			}
		}

		targetTree.lockNode(index);

		// Continue recursive
		targetTree.viewNode(index);
		IndexIterator iterator = newIterator();
		iterator.setMax(targetTree.getEdgeCount()-1);
		while(iterator.hasNext()) {
			search(targetTree.getChildIndexAt(index, iterator.next()), minIndex, maxIndex);

			if(isDone()) {
				break;
			}
		}

		targetTree.unlockNode(index);

		recycleIterator(iterator);
	}

	protected boolean isDone() {
		return matched && (exclusionMember || !exhaustive);
	}
}
