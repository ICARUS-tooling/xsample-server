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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unistuttgart.xsample.qe.icarus1.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchUtils {

	private SearchUtils() {
		// no-op
	}

	public static boolean isExhaustiveSearch(Search search) {
		return search.getParameters().get(SearchParameters.SEARCH_MODE, SearchParameters.DEFAULT_SEARCH_MODE).isExhaustive();
	}

	public static boolean isLeftToRightSearch(Search search) {
		return search.getParameters().get(SearchParameters.SEARCH_ORIENTATION, SearchParameters.DEFAULT_SEARCH_ORIENTATION)==Orientation.LEFT_TO_RIGHT;
	}

	public static boolean isOptimizedSearch(Search search) {
		return search.getParameters().getBoolean(SearchParameters.OPTIMIZE_SEARCH, SearchParameters.DEFAULT_OPTIMIZE_SEARCH);
	}

	public static boolean isCaseSensitiveSearch(Search search) {
		return search.getParameters().getBoolean(SearchParameters.SEARCH_CASESENSITIVE, SearchParameters.DEFAULT_SEARCH_CASESENSITIVE);
	}

	public static Object getDefaultSpecifier(ConstraintFactory factory) {
		Object[] specifiers = factory.getSupportedSpecifiers();
		return (specifiers!=null && specifiers.length>0) ?
				specifiers[0] : null;
	}

	/**
	 * Returns an exact copy of the given {@code SearchGraph} with
	 * all its constraints contained within nodes and edges instantiated
	 * using the {@code ConstraintFactory} implementations provided
	 * by the specified context. In addition the provided options are passed
	 * to the factories so one can create a copy of an existing search graph
	 * with new settings.
	 * <p>
	 * It is recommended that {@link SearchFactory} implementations make use
	 * of this method when creating the actual {@link Search} object so they
	 * can be sure that all constraints are properly instantiated and not
	 * plain instances of {@link SearchConstraint}.
	 */
	public static SearchGraph instantiate(SearchGraph graph, ConstraintContext context, Options options) {
		if(graph==null)
			throw new NullPointerException("Graph is null"); //$NON-NLS-1$
		if(context==null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		Map<SearchNode, SearchNode> cloneMap = new HashMap<>();

		List<SearchNode> nodes = new ArrayList<>();
		List<SearchEdge> edges = new ArrayList<>();
		List<SearchNode> roots = new ArrayList<>();

		for(SearchNode node : graph.getNodes()) {
			SearchNode clone = new SearchNode();
			clone.setId(node.getId());
			clone.setNegated(node.isNegated());
			clone.setNodeType(node.getNodeType());
			clone.setConstraints(instantiate(node.getConstraints(), context, options));

			cloneMap.put(node, clone);
			nodes.add(clone);
		}

		for(SearchEdge edge : graph.getEdges()) {
			SearchEdge clone = new SearchEdge();
			clone.setId(edge.getId());
			clone.setNegated(edge.isNegated());
			clone.setEdgeType(edge.getEdgeType());
			clone.setConstraints(instantiate(edge.getConstraints(), context, options));

			edges.add(clone);

			SearchNode target = cloneMap.get(edge.getTarget());
			SearchNode source = cloneMap.get(edge.getSource());

			clone.setSource(source);
			clone.setTarget(target);

			source.addEdge(clone, false);
			target.addEdge(clone, true);
		}

		for(SearchNode root : graph.getRootNodes()) {
			roots.add(cloneMap.get(root));
		}

		SearchGraph result = new SearchGraph();
		result.setRootOperator(graph.getRootOperator());

		result.setRootNodes(roots.toArray(new SearchNode[0]));
		result.setNodes(nodes.toArray(new SearchNode[0]));
		result.setEdges(edges.toArray(new SearchEdge[0]));

		return result;
	}

	private static SearchConstraint[] instantiate(SearchConstraint[] constraints,
			ConstraintContext context, Options options) {
		if(constraints==null) {
			return null;
		}

		if(options==null) {
			options = Options.emptyOptions;
		}

		List<SearchConstraint> result = new ArrayList<>();

		for(SearchConstraint constraint : constraints) {
			if(constraint==null || constraint.isUndefined() || !constraint.isActive()) {
				continue;
			}

			ConstraintFactory factory = context.getFactory(constraint.getToken());
			result.add(factory.createConstraint(
					constraint.getValue(), constraint.getOperator(),
					constraint.getSpecifier(), options));
		}

		return toArray(result);
	}

	public static boolean asBoolean(Object value) {
		if(value instanceof Boolean) {
			return (boolean)value;
		} else {
			return Boolean.parseBoolean(value.toString());
		}
	}

	public static int asInteger(Object value) {
		if(value instanceof Integer) {
			return (int)value;
		} else {
			return Integer.parseInt(value.toString());
		}
	}

	public static double asDouble(Object value) {
		if(value instanceof Double) {
			return (double)value;
		} else {
			return Double.parseDouble(value.toString());
		}
	}

	public static long asLong(Object value) {
		if(value instanceof Long) {
			return (long)value;
		} else {
			return Long.parseLong(value.toString());
		}
	}

	public static List<SearchConstraint> cloneConstraints(List<SearchConstraint> constraints) {
		if(constraints==null) {
			return null;
		}

		List<SearchConstraint> result = new ArrayList<>();

		for(SearchConstraint constraint : constraints) {
			result.add(constraint.clone());
		}

		return result;
	}

	public static SearchConstraint[] toArray(Collection<SearchConstraint> constraints) {
		return constraints==null ? null : constraints.toArray(
				new SearchConstraint[constraints.size()]);
	}

	public static int getMinInstanceCount(ConstraintFactory factory) {
		int value = factory.getMinInstanceCount();
		return value==-1 ? 1 : value;
	}

	public static int getMaxInstanceCount(ConstraintFactory factory) {
		int value = factory.getMaxInstanceCount();
		return value==-1 ? 9 : value;
	}

	public static SearchConstraint[] createSearchConstraints(List<ConstraintFactory> factories) {
		List<SearchConstraint> constraints = new ArrayList<>();

		for(ConstraintFactory factory : factories) {
			int min = getMinInstanceCount(factory);
			int max = factory.getMaxInstanceCount();
			if(max!=-1 && max<min)
				throw new IllegalArgumentException("Max instance count of factory is too small: "+factory.getClass()); //$NON-NLS-1$

			SearchOperator operator = factory.getSupportedOperators()[0];

			for(int i=0; i<min; i++) {
				constraints.add(new SearchConstraint(
						factory.getToken(), factory.getDefaultValue(null), operator));
			}
		}

		return toArray(constraints);
	}

	public interface Visitor {
		void visit(SearchNode node);

		void visit(SearchEdge edge);
	}

	public static void traverse(SearchGraph graph, Visitor visitor) {
		if(graph==null)
			throw new NullPointerException("Invalid graph"); //$NON-NLS-1$
		if(visitor==null)
			throw new NullPointerException("Invalid visitor"); //$NON-NLS-1$

		Set<Object> visited = new HashSet<>();

		for(SearchNode root : graph.getRootNodes()) {
			traverse(root, visitor, visited);
		}
	}

	public static void traverse(SearchNode node, Visitor visitor) {
		if(node==null)
			throw new NullPointerException("Invalid node"); //$NON-NLS-1$
		if(visitor==null)
			throw new NullPointerException("Invalid visitor"); //$NON-NLS-1$

		Set<Object> visited = new HashSet<>();

		traverse(node, visitor, visited);
	}

	private static void traverse(SearchNode node, Visitor visitor, Set<Object> visited) {
		if(visited.contains(node)) {
			return;
		}

		visited.add(node);
		visitor.visit(node);

		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);

			visitor.visit(edge);
			traverse(edge.getTarget(), visitor, visited);
		}
	}

	public static EnumSet<EdgeType> regularEdges = EnumSet.of(EdgeType.DOMINANCE, EdgeType.TRANSITIVE);
	public static EnumSet<EdgeType> allEdges = EnumSet.of(EdgeType.DOMINANCE, EdgeType.values());
	public static EnumSet<EdgeType> utilityEdges = EnumSet.of(EdgeType.LINK, EdgeType.PRECEDENCE);


	public static List<SearchNode> getChildNodes(SearchNode node) {
		return getChildNodes(node, null);
	}

	public static List<SearchNode> getChildNodes(SearchNode node, EnumSet<EdgeType> allowedEdges) {
		List<SearchNode> children = new ArrayList<>();

		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(allowedEdges==null || allowedEdges.contains(edge.getEdgeType())) {
				children.add(edge.getTarget());
			}
		}

		return children;
	}

	public static SearchConstraint[] cloneSimple(SearchConstraint[] constraints) {
		if(constraints==null) {
			return null;
		}
		int size = constraints.length;
		SearchConstraint[] result = new SearchConstraint[size];

		for(int i=0; i<size; i++) {
			SearchConstraint constraint = constraints[i];
			result[i] = new SearchConstraint(constraint.getToken(),
					constraint.getValue(), constraint.getOperator(), constraint.getSpecifier());
		}

		return result;
	}

	public static SearchConstraint[] cloneConstraints(SearchConstraint[] source) {
		if(source==null) {
			return null;
		}

		int size = source.length;
		SearchConstraint[] newConstraints = new SearchConstraint[size];
		for(int i=0; i<size; i++) {
			newConstraints[i] = source[i].clone();
		}
		return newConstraints;
	}

	public static boolean isEmpty(SearchGraph graph) {
		return graph==null || graph.getRootNodes()==null || graph.getRootNodes().length==0;
	}

	private static void collectInactive0(List<SearchConstraint> buffer, SearchConstraint[] constraints) {
		if(constraints==null) {
			return;
		}

		for(SearchConstraint constraint : constraints) {
			if(!constraint.isActive() && !constraint.isUndefined()) {
				buffer.add(constraint);
			}
		}
	}

	public static List<SearchConstraint> collectInactive(SearchGraph graph) {
		if(isEmpty(graph)) {
			return Collections.emptyList();
		}

		final List<SearchConstraint> result = new ArrayList<>();

		Visitor visitor = new Visitor() {

			@Override
			public void visit(SearchEdge edge) {
				collectInactive0(result, edge.getConstraints());
			}

			@Override
			public void visit(SearchNode node) {
				collectInactive0(result, node.getConstraints());
			}
		};

		traverse(graph, visitor);

		return result;
	}

	public static boolean isUndefined(SearchConstraint[] constraints) {
		if(constraints==null) {
			return true;
		}

		for(SearchConstraint constraint : constraints) {
			if(!constraint.isUndefined()) {
				return false;
			}
		}

		return true;
	}

	public static boolean isUndefined(SearchEdge edge) {
		if(edge==null) {
			return true;
		}

		return isUndefined(edge.getConstraints());
	}

	public static boolean isUndefined(SearchNode node) {
		if(node==null) {
			return true;
		}

		return isUndefined(node.getConstraints());
	}

	public static boolean searchIsReady(Search search) {
		if(search==null)
			throw new NullPointerException("Invalid search"); //$NON-NLS-1$

		if(search.getTarget()==null) {
			return false;
		}
		if(search.getQuery()==null) {
			return false;
		}
		if(isEmpty(search.getQuery().getSearchGraph())) {
			return false;
		}

		return !search.isRunning() && !search.isDone();
	}

	public static final Comparator<SearchNode> nodeIdSorter = new Comparator<SearchNode>() {

		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			return o1.getId().compareTo(o2.getId());
		}

	};

	public static final Comparator<SearchEdge> edgeIdSorter = new Comparator<SearchEdge>() {

		@Override
		public int compare(SearchEdge o1, SearchEdge o2) {
			return o1.getId().compareTo(o2.getId());
		}

	};

	public static final Comparator<SearchConstraint> constraintSorter = new Comparator<SearchConstraint>() {

		@Override
		public int compare(SearchConstraint o1, SearchConstraint o2) {
			return o1.getToken().compareTo(o2.getToken());
		}
	};

	public static final Comparator<ConstraintFactory> factoryTokenSorter = new Comparator<ConstraintFactory>() {

		@Override
		public int compare(ConstraintFactory o1, ConstraintFactory o2) {
			return o1.getToken().compareTo(o2.getToken());
		}
	};

	public static final Comparator<ConstraintFactory> factoryNameSorter = new Comparator<ConstraintFactory>() {

		@Override
		public int compare(ConstraintFactory o1, ConstraintFactory o2) {
			return o1.getToken().compareTo(o2.getToken());
		}
	};
}
