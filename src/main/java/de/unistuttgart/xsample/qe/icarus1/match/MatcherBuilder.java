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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import de.unistuttgart.xsample.qe.icarus1.match.Matcher.PrecedenceNode;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatcherBuilder {

	protected final Search search;

	protected Map<TreeNode, Matcher> matcherMap;
	protected Map<Matcher, Matcher> cloneMap;
	protected Map<SearchNode, Integer> idMap;

	protected Map<SearchNode, List<Matcher>> nodeMap;

	protected AtomicInteger idGen;

	protected Matcher rootMatcher;

	protected MatcherLinker linker;

	public MatcherBuilder(Search search) {
		if(search==null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$

		this.search = search;
	}

	protected MatcherLinker getLinker() {
		if(linker==null) {
			linker = new MatcherLinker();
		}

		return linker;
	}

	public Matcher createRootMatcher() {
		matcherMap = new HashMap<>();
		idMap = new HashMap<>();
		idGen = new AtomicInteger();
		nodeMap = new HashMap<>();

		SearchGraph graph = search.getQuery().getSearchGraph();
		graph = SearchUtils.instantiate(graph,
				search.getQuery().getConstraintContext(),
				search.getParameters());
		boolean isDisjunction = graph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION;

		TreeNode tree = new TreeNode();

		// Create tree
		for(SearchNode root : graph.getRootNodes()) {
			tree.addChild(createTree0(tree, root, null));
		}

		// Optimize tree
		for(TreeNode node : tree.getChildren()) {
			optimizeTree(node);
		}

		// Create all plain matchers
		for(int i=0; i<tree.getChildCount(); i++) {
			createMatcher0(null, tree.getChildAt(i), false);
		}

		// Resolve and link precedence edges
		if(graph.getEdges()!=null) {
			for(SearchEdge edge : graph.getEdges()) {
				if(edge.getEdgeType()==EdgeType.PRECEDENCE) {
					resolvePrecedenceEdge(edge);
				}
			}
		}

		getLinker().clear();

		List<TreeNode> negatedRoots = tree.getNegatedChildren();
		List<TreeNode> unnegatedRoots = tree.getUnnegatedChildren();

		// Now link matchers starting from root nodes
		if(unnegatedRoots!=null) {
			rootMatcher = createSimpleRootMatcher(unnegatedRoots, isDisjunction);
		}
		if(negatedRoots!=null) {
			rootMatcher = createProxyRootMatcher(negatedRoots, isDisjunction, rootMatcher);
		}

		rootMatcher.link();

		return rootMatcher;
	}

	protected Matcher createSimpleRootMatcher(List<TreeNode> roots, boolean isDisjunction) {
		if(isDisjunction) {
			Matcher last = null;
			for(TreeNode root : roots) {
				Matcher matcher = matcherMap.get(root);
				if(last!=null) {
					last.setAlternate(matcher);
				}
				last = matcher;

				getLinker().clear();
				linkMatcher0(root);
			}
		} else {
			for(TreeNode root : roots) {
				linkMatcher0(root);
			}
		}

		return matcherMap.get(roots.get(0));
	}

	protected Matcher createProxyRootMatcher(List<TreeNode> negatedRoots,
			boolean isDisjunction, Matcher unnegatedRootMatcher) {
		if(isDisjunction) {
			// Create a proxy for every negated root, linked
			// as alternates
			Matcher[] proxies = new Matcher[negatedRoots.size()];

			for(int i=0; i<proxies.length; i++) {
				getLinker().clear();
				TreeNode exclusion = negatedRoots.get(i);
				linkMatcher0(exclusion);
				Matcher[] exclusions = {
					matcherMap.get(exclusion),
				};

				Matcher proxy = new ProxyRootMatcher();
				proxy.setExclusions(exclusions);
				if(i>0) {
					proxies[i-1].setAlternate(proxy);
				}
				//proxy.setNext(unnegatedRootMatcher);

				proxies[i] = proxy;
			}

			// If there exists at least one other root
			// node that is not negated it will be wrapped
			// in a matcher that holds all such nodes and has
			// no alternates
			if(unnegatedRootMatcher!=null) {
				Matcher lastProxy = proxies[proxies.length-1];
				lastProxy.setAlternate(unnegatedRootMatcher);
			}

			return proxies[0];
		} else {
			// Create a single proxy matcher with all the negated
			// nodes as exclusions
			Matcher proxy = new ProxyRootMatcher();

			proxy.setNext(unnegatedRootMatcher);

			Matcher[] exclusions = new Matcher[negatedRoots.size()];
			for(int i=0; i<exclusions.length; i++) {
				getLinker().clear();
				TreeNode exclusion = negatedRoots.get(i);
				exclusions[i] = matcherMap.get(exclusion);
				linkMatcher0(exclusion);
			}

			proxy.setExclusions(exclusions);

			return proxy;
		}
	}

	protected TreeNode createTree0(TreeNode parent, SearchNode node, SearchEdge head) {
		TreeNode treeNode = new TreeNode(parent);
		treeNode.setSearchNode(node);
		treeNode.setSearchEdge(head);

		int edgeCount = node.getOutgoingEdgeCount();
		for(int i=0; i<edgeCount; i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			// Skip link and precedence edges
			if(edge.getEdgeType()==EdgeType.LINK
					|| edge.getEdgeType()==EdgeType.PRECEDENCE) {
				continue;
			}

			treeNode.addChild(createTree0(treeNode, edge.getTarget(), edge));
		}

		return treeNode;
	}

	protected void optimizeTree(TreeNode tree) {

		// First transform negated disjunctions in child list

		// Fetch disjunctive children
		List<TreeNode> children = tree.getDisjunctiveChildren();
		if(children!=null) {
			// Filter out all unnegated children
			for(Iterator<TreeNode> i = children.iterator(); i.hasNext();) {
				if(!i.next().isNegated()) {
					i.remove();
				}
			}
			// Remaining children are negated disjunctions
			if(!children.isEmpty()) {
				tree.removeChildren(children);
				for(TreeNode dummy : children) {
					// Invert all former grand children in the negated
					// disjunction scope and add them to the current tree
					List<TreeNode> grandChildren = dummy.getChildren();
					invert(grandChildren);
					tree.addChildren(grandChildren);
				}
			}
		}

		// Now process child nodes
		for(int i=0; i<tree.getChildCount(); i++) {
			optimizeTree(tree.getChildAt(i));
		}

		// Special handling for disjunction nodes
		if(tree.isDisjunction()) {
			List<TreeNode> unnegatedChildren = tree.getUnnegatedChildren();

			// If the disjunction contained unnegated members
			// simply add them as a list of alternates
			if(unnegatedChildren!=null) {
				TreeNode leader = linkAlternates(unnegatedChildren);

				tree.removeChildren(unnegatedChildren);
				tree.insertChildAt(0, leader);

				leader.setParent(tree);
			}
		}
	}

	protected void invert(Collection<TreeNode> nodes) {
		for(TreeNode node : nodes) {
			node.setNegated(!node.isNegated());
		}
	}

	protected TreeNode linkAlternates(List<TreeNode> alternates) {
		if(alternates==null || alternates.isEmpty()) {
			return null;
		}

		for(int i=1; i<alternates.size(); i++) {
			alternates.get(i-1).setAlternate(alternates.get(i));
		}

		return alternates.get(0);
	}

	protected Matcher linkMatcher0(TreeNode tree) {

		Matcher matcher = matcherMap.get(tree);

		// Append matcher to pre-order list
		getLinker().link(matcher);
		// Only include non-disjunctive trees, since disjunctive
		// ones link to their children via their options list!
		if(!tree.isDisjunction()) {
			getLinker().add(matcher);
		}

		matcher.setId(getId(matcher.getNode()));

		// Create exclusion list
		List<TreeNode> negatedChildren = tree.getNegatedChildren();
		if(negatedChildren!=null) {
			getLinker().save();

			Matcher[] exclusions = new Matcher[negatedChildren.size()];
			for(int i=0; i<negatedChildren.size(); i++) {
				getLinker().clear();

				exclusions[i] = linkMatcher0(negatedChildren.get(i));
			}
			matcher.setExclusions(exclusions);

			getLinker().load();
		}

		// Link non-disjunctive unnegated children
		List<TreeNode> unnegatedChildren = tree.getUnnegatedChildren();
		if(unnegatedChildren!=null) {
			if(tree.isDisjunction()) {
				// For disjunctions we save the children in a special
				// options list

				Matcher[] options = new Matcher[unnegatedChildren.size()];
				for(int i=0; i<unnegatedChildren.size(); i++) {
					getLinker().save();
					options[i] = linkMatcher0(unnegatedChildren.get(i));
					getLinker().merge();
					getLinker().load();
				}
				matcher.setOptions(options);
			} else {
				// For regular matchers simply perform the default linking
				for(int i=0; i<unnegatedChildren.size(); i++) {
					linkMatcher0(unnegatedChildren.get(i));
				}
			}
		}

		// Finally link disjunctive children
		List<TreeNode> disjunctionChildren = tree.getDisjunctiveChildren();
		if(disjunctionChildren!=null) {
			if(tree.isDisjunction())
				throw new IllegalStateException("Nested disjunction"); //$NON-NLS-1$

			for(int i=0; i<disjunctionChildren.size(); i++) {
				//getLinker().save();

				linkMatcher0(disjunctionChildren.get(i));

				//getLinker().merge();
				//getLinker().load();
			}
		}

		TreeNode alternate = tree.getAlternate();
		if(alternate!=null) {
			// Force clean entry point for alternate tree
			getLinker().save();

			matcher.setAlternate(linkMatcher0(alternate));

			getLinker().merge();
			getLinker().load();
		}

		matcher.setHeight(tree.getHeight());
		matcher.setDescendantCount(tree.getDescendantCount());

		// Special double-linking for disjunctions:
		// The matcher needs to be linked with all its optional
		// children and the next matcher in a possible sequence
		if(tree.isDisjunction()) {
			getLinker().add(matcher);
		}

		return matcher;
	}

	protected Matcher createMatcher0(Matcher parent, TreeNode treeNode,
			boolean exclusionMember) {

		if(matcherMap.containsKey(treeNode))
			throw new IllegalStateException("Duplicate creation attempt for tree"); //$NON-NLS-1$

		SearchNode node = treeNode.getSearchNode();
		SearchEdge edge = treeNode.getSearchEdge();

		exclusionMember |= treeNode.isNegated();

		Matcher matcher;
		if(treeNode.isDisjunction()) {
			matcher = new DisjunctionMatcher(node, edge);
		} else if(edge==null) {
			matcher = new RootMatcher(node);
		} else if(edge.getEdgeType()==EdgeType.TRANSITIVE) {
			matcher = new TransitiveMatcher(node, edge);
		} else {
			matcher = new Matcher(node, edge);
		}

		// Save node-to-matcher mapping
		List<Matcher> list = nodeMap.get(node);
		if(list==null) {
			list = new ArrayList<>();
			nodeMap.put(node, list);
		}
		list.add(matcher);

		// Load constraints
		List<SearchConstraint> constraints = new ArrayList<>();
		if(edge!=null) {
			Collections.addAll(constraints, edge.getConstraints());
		}
		Collections.addAll(constraints, node.getConstraints());
		for(Iterator<SearchConstraint> i = constraints.iterator(); i.hasNext();) {
			if(i.next().isUndefined()) {
				i.remove();
			}
		}
		matcher.setConstraints(constraints.isEmpty() ? null : constraints.toArray(new SearchConstraint[0]));

		matcher.setExclusionMember(exclusionMember);
		matcher.setParent(parent);

		// Save tree to matcher mapping
		matcherMap.put(treeNode, matcher);

		// Build exclusion list if required
		List<TreeNode> negatedChildren = treeNode.getNegatedChildren();
		if(negatedChildren!=null) {
			Matcher[] exclusions = new Matcher[negatedChildren.size()];
			for(int i=0; i<negatedChildren.size(); i++) {
				exclusions[i] = createMatcher0(matcher,
						negatedChildren.get(i), true);
			}

			matcher.setExclusions(exclusions);
			treeNode.removeChildren(negatedChildren);
		}

		// Process child nodes (only unnegated children left)
		for(int i=0; i<treeNode.getChildCount(); i++) {
			createMatcher0(matcher, treeNode.getChildAt(i), exclusionMember);
		}

		if(treeNode.getAlternate()!=null) {
			createMatcher0(parent, treeNode.getAlternate(), exclusionMember);
		}

		return matcher;
	}

	protected void resolvePrecedenceEdge(SearchEdge edge) {
		List<Matcher> sources = nodeMap.get(edge.getSource());
		List<Matcher> targets = nodeMap.get(edge.getTarget());

		if(sources==null || targets==null) {
			// TODO error msg?
			return;
		}

		IntOperator operator = null;
		int offset = -1;

		SearchConstraint[] constraints = edge.getConstraints();
		if(constraints!=null) {
			for(SearchConstraint constraint : constraints) {
				if(!constraint.isUndefined() && "distance".equals(constraint.getToken())) { //$NON-NLS-1$
					operator = IntOperator.fromSymbol(constraint.getOperator().getSymbol());
					offset = (int)constraint.getValue(); //TODO maybe check for string?
				}
			}
		}

		PrecedenceNode[] before = new PrecedenceNode[sources.size()];
		for(int i=0; i<sources.size(); i++) {
			before[i] = new PrecedenceNode(sources.get(i), operator, offset);
		}

		PrecedenceNode[] after = new PrecedenceNode[targets.size()];
		for(int i=0; i<targets.size(); i++) {
			after[i] = new PrecedenceNode(targets.get(i), operator, offset);
		}

		for(Matcher matcher : sources) {
			matcher.setAfter(after);
		}

		for(Matcher matcher : targets) {
			matcher.setBefore(before);
		}
	}

	protected int getId(SearchNode node) {
		Integer id = idMap.get(node);
		if(id==null) {
			id = idGen.getAndIncrement();
			idMap.put(node, id);
		}
		return id;
	}

	public Matcher cloneMatcher(Matcher matcher) {
		if(cloneMap==null) {
			cloneMap = new  HashMap<>();
		} else {
			cloneMap.clear();
		}

		Matcher clone = cloneMatcher0(matcher);

		clone.link();

		return clone;
	}

	protected Matcher cloneMatcher0(Matcher matcher) {
		if(matcher==null) {
			return null;
		}

		Matcher clone = cloneMap.get(matcher);
		if(clone==null) {
			// Create a shallow clone
			clone = matcher.clone();
			// Immediately save reference to clone to prevent
			// duplicates when cloning inner members
			cloneMap.put(matcher, clone);

			// Now do the deep cloning with respect to already cloned members
			clone.setParent(cloneMatcher0(matcher.getParent()));
			clone.setNext(cloneMatcher0(matcher.getNext()));
			clone.setAlternate(cloneMatcher0(matcher.getAlternate()));
			clone.setExclusions(cloneMatchers(matcher.getExclusions()));

			clone.setAfter(clonePrecedenceNodes(matcher.getAfter()));
			clone.setBefore(clonePrecedenceNodes(matcher.getBefore()));

			clone.setOptions(cloneMatchers(matcher.getOptions()));
		}
		return clone;
	}

	protected Matcher[] cloneMatchers(Matcher[] matchers) {
		if(matchers==null) {
			return null;
		}

		int size = matchers.length;
		Matcher[] clones = new Matcher[size];

		for(int i=0; i<size; i++) {
			clones[i] = cloneMatcher0(matchers[i]);
		}

		return clones;
	}

	protected PrecedenceNode[] clonePrecedenceNodes(PrecedenceNode[] nodes) {
		if(nodes==null) {
			return null;
		}

		int size = nodes.length;
		PrecedenceNode[] clones = new PrecedenceNode[size];

		for(int i=0; i<size; i++) {
			PrecedenceNode node = nodes[i];
			clones[i] = new PrecedenceNode(cloneMatcher0(node.getMatcher()), node.getOperator(), node.getOffset());
		}

		return clones;
	}

	protected static class TreeNode implements Cloneable {
		protected SearchEdge searchEdge;
		protected SearchNode searchNode;

		protected TreeNode parent;
		protected TreeNode alternate;

		protected List<TreeNode> children;

		protected int height = -1;
		protected int descendantCount = -1;

		protected boolean negated = false;

		public TreeNode() {
			// no-op
		}

		@Override
		public String toString() {
			return searchNode!=null ? searchNode.toString() : super.toString();
		}

		public TreeNode(TreeNode parent) {
			setParent(parent);
		}

		public TreeNode getAlternate() {
			return alternate;
		}

		public void setAlternate(TreeNode alternate) {
			this.alternate = alternate;
			if(alternate!=null) {
				alternate.setParent(parent);
			}
		}

		public SearchEdge getSearchEdge() {
			return searchEdge;
		}

		public SearchNode getSearchNode() {
			return searchNode;
		}

		public TreeNode getParent() {
			return parent;
		}

		public void setSearchEdge(SearchEdge searchEdge) {
			this.searchEdge = searchEdge;
		}

		public void setSearchNode(SearchNode searchNode) {
			this.searchNode = searchNode;
		}

		public void setParent(TreeNode parent) {
			this.parent = parent;
			if(alternate!=null) {
				alternate.setParent(parent);
			}
		}

		public void addChild(TreeNode child) {
			if(children==null) {
				children = new ArrayList<>();
			}

			children.add(child);
			child.setParent(this);

			height = -1;
			descendantCount = -1;
		}

		public List<TreeNode> getChildren() {
			return children;
		}

		public int getChildCount() {
			return children==null ? 0 : children.size();
		}

		public TreeNode getChildAt(int index) {
			return children==null ? null : children.get(index);
		}

		public int indexInParent() {
			return parent==null ? -1 : parent.indexOfChild(this);
		}

		public void removeChildAt(int index) {
			if(children!=null) {
				TreeNode child = children.remove(index);
				if(child!=null) {
					child.setParent(null);

					height = -1;
					descendantCount = -1;
				}
			}
		}

		public void removeChild(TreeNode child) {
			if(children!=null) {
				children.remove(child);
				child.setParent(null);

				height = -1;
				descendantCount = -1;
			}
		}

		public void removeChildren(Collection<TreeNode> items) {
			if(items==null || children==null) {
				return;
			}

			children.removeAll(items);

			for(TreeNode child : items) {
				child.setParent(null);
			}

			height = -1;
			descendantCount = -1;
		}

		public int indexOfChild(TreeNode child) {
			return children==null ? -1 : children.indexOf(child);
		}

		public void setChild(int index, TreeNode child) {
			children.set(index, child);
			child.setParent(this);

			height = -1;
			descendantCount = -1;
		}

		public void insertChildAt(int index, TreeNode child) {
			children.add(index, child);
			child.setParent(this);

			height = -1;
			descendantCount = -1;
		}

		public void replaceChild(TreeNode oldChild, TreeNode newChild) {
			if(children==null) {
				return;
			}

			int index = children.indexOf(oldChild);
			children.set(index, newChild);
			oldChild.setParent(null);
			newChild.setParent(this);

			height = -1;
			descendantCount = -1;
		}

		public void addChildren(List<TreeNode> items) {
			if(items==null) {
				return;
			}
			if(children==null) {
				children = new ArrayList<>();
			}

			children.addAll(items);
			for(TreeNode child : items) {
				child.setParent(this);
			}

			height = -1;
			descendantCount = -1;
		}

		public boolean isNegated() {
			// FIXME test new handling
			//return negated || searchNode.isNegated() || (searchEdge!=null && searchEdge.isNegated());

			boolean negated = this.negated;
			if(searchEdge!=null && searchEdge.isNegated()) {
				negated = !negated;
			}
			if(searchNode.isNegated()) {
				negated = !negated;
			}

			return negated;
		}

		public void setNegated(boolean negated) {
			this.negated = negated;
		}

		public boolean hasNegatedChild() {
			if(children!=null) {
				for(TreeNode child : children) {
					if(child.isNegated()) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isDisjunction() {
			return searchNode.getNodeType()==NodeType.DISJUNCTION;
		}

		public List<TreeNode> getNegatedChildren() {
			if(children==null) {
				return null;
			}

			List<TreeNode> result = null;

			for(TreeNode child : children) {
				if(child.isNegated() && !child.isDisjunction()) {
					if(result==null) {
						result = new ArrayList<>();
					}
					result.add(child);
				}
			}

			return result;
		}

		public List<TreeNode> getUnnegatedChildren() {
			if(children==null) {
				return null;
			}

			List<TreeNode> result = null;

			for(TreeNode child : children) {
				if(!child.isNegated() && !child.isDisjunction()) {
					if(result==null) {
						result = new ArrayList<>();
					}
					result.add(child);
				}
			}

			return result;
		}

		public List<TreeNode> getDisjunctiveChildren() {
			if(children==null) {
				return null;
			}

			List<TreeNode> result = null;

			for(TreeNode child : children) {
				if(child.isDisjunction()) {
					if(result==null) {
						result = new ArrayList<>();
					}
					result.add(child);
				}
			}

			return result;
		}

		public int getHeight() {
			if(height==-1) {
				int value = 0;
				if(children!=null) {
					if(isDisjunction()) {
						value = Integer.MAX_VALUE;
						for(TreeNode child : children) {
							value = Math.min(value, child.getHeight());
						}
					} else if(!isNegated()) {
						for(TreeNode child : children) {
							value = Math.max(value, child.getHeight());
						}
						value++;
					}
				}
				height = value;
			}

			return height;
		}

		public int getDescendantCount() {
			if(descendantCount==-1) {
				int value = 0;

				if(children!=null) {
					if(isDisjunction()) {
						value = Integer.MAX_VALUE;
						for(TreeNode child : children) {
							value = Math.min(value, child.getDescendantCount());
						}
						//value++;
					} else if(!isNegated()) {
						value = children.size();

						for(TreeNode child : children) {
							value += child.getDescendantCount();
						}
					}
				}

				descendantCount = value;
			}

			return descendantCount;
		}

		@Override
		public TreeNode clone() {
			TreeNode clone = new TreeNode(parent);
			clone.searchNode = searchNode;
			clone.searchEdge = searchEdge;

			if(alternate!=null) {
				clone.alternate = alternate.clone();
			}

			if(children!=null) {
				clone.children = new ArrayList<>(children.size());
				for(TreeNode child : children) {
					clone.children.add(child.clone());
				}
			}

			return clone;
		}
	}

	protected static class MatcherLinker {
		protected Stack<List<Matcher>> stack = new Stack<>();
		protected List<Matcher> buffer;

		public void add(Matcher matcher) {
			if(buffer==null) {
				buffer = newBuffer();
			}
			buffer.add(matcher);
		}

		protected List<Matcher> newBuffer() {
			return new LinkedList<>();
		}

		public void link(Matcher target) {
			if(buffer==null || buffer.isEmpty()) {
				return;
			}

			for(Matcher matcher : buffer) {
				matcher.setNext(target);
			}

			buffer.clear();
		}

		public void clear() {
			if(buffer!=null) {
				buffer.clear();
			}
		}

		public void save() {
			if(buffer==null) {
				buffer = newBuffer();
			}
			stack.push(buffer);
			buffer = null;
		}

		public void load() {
			if(stack.isEmpty())
				throw new IllegalStateException();

			buffer = stack.pop();
		}

		public void merge() {
			if(stack.isEmpty() || buffer==null) {
				return;
			}

			stack.peek().addAll(buffer);
		}
	}
}
