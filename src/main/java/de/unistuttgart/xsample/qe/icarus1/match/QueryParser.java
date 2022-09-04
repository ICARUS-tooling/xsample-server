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

import static de.unistuttgart.xsample.util.XSampleUtils._int;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.unistuttgart.xsample.qe.icarus1.CompactProperties;
import de.unistuttgart.xsample.qe.icarus1.LanguageConstants;
import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.StringUtil;
import de.unistuttgart.xsample.qe.icarus1.UnsupportedFormatException;


/**
 *
 *
 *
 * Query-semi-EBNF:
 *
 * digit			=	"0" to "9" ;<br>
 * number 			=	[ "-" ], digit, { digit } ;<br>
 * letter			=	"A" to "Z" ;<br>
 * space			=	all whitespace characters<br>
 * symbol			=	all special symbols ;<br>
 * character		=	letter | digit | symbol<br>
 * identifier		=	letter , { letter | "_" } ;<br>
 * char_sequence 	=	character , { character } ;<br>
 * text				=	"'" , char_sequence , "'"
 * 						| '"' , char_sequence , '"' ;<br>
 * operator			=	"=" | "!=" | "=~" | "!~" | "=#" | "!#" | "&gt;" | "&ge;" | "&lt;" | "&le;"
 * grouping			=	"&lt;*&gt;"<br>
 *
 * assignment		=	identifier, [ space ], operator, [ space], (text | grouping);
 * node				=	"[", [ identifier ] { ",",  identifier } { node }"]" ;
 *
 * query			=	[ "!" ], [ space ]
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class QueryParser {

	public static final String EXPAND_TOKENS_OPTION = "expandTokens"; //$NON-NLS-1$

	public static final String NODE_NAME_PATTERN_OPTION = "nodeNamePattern"; //$NON-NLS-1$
	public static final String EDGE_NAME_PATTERN_OPTION = "edgeNamePattern"; //$NON-NLS-1$
	public static final String ID_OPTION = "id"; //$NON-NLS-1$
	public static final String EDGETYPE_OPTION = "edgeType"; //$NON-NLS-1$
	public static final String NODETYPE_OPTION = "nodeType"; //$NON-NLS-1$

	public static final String DEFAULT_NODE_NAME_PATTERN = "node_%d"; //$NON-NLS-1$
	public static final String DEFAULT_EDGE_NAME_PATTERN = "edge_%d"; //$NON-NLS-1$

	// Quotation marks symbol
	protected static final char QUOTATIONMARK = '"';
	protected static final char SINGLE_QUOTATIONMARK = '\'';

	// Escaping character
	protected static final char ESCAPE = '\\';

	protected static final char SPACE = ' ';

	protected static final char UNDERSCORE = '_';

	protected static final char EQUALITY_SIGN = '=';

	// Square brackets (used as wrappers for a node definition)
	protected static final char SQUAREBRAKET_OPENING = '[';
	protected static final char SQUAREBRAKET_CLOSING = ']';

	// Brackets (used as wrappers for a collection of meta-constraints or properties)
	protected static final char BRAKET_OPENING = '(';
	protected static final char BRAKET_CLOSING = ')';

	// Curly brackets (used as wrappers for defining disjunctions)
	protected static final char CURLYBRAKET_OPENING = '{';
	protected static final char CURLYBRAKET_CLOSING = '}';

	protected static final char NEGATION_SIGN = '!';

	// General enumeration delimiter (comma)
	protected static final char COMMA = ',';

	protected static final char COLON = ':';

	protected static final char SPECIFIER_DELIMITER = '$';

	protected String query;

	protected int index;

	protected StringBuilder buffer = new StringBuilder(100);

	// Optional context, used to complete token fragments
	protected final ConstraintContext context;
	protected final Options options;

	protected final NodeStack nodeStack = new NodeStack();

	public QueryParser(Options options) {
		this(null, options);
	}

	public QueryParser(ConstraintContext context, Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}

		this.options = options;
		this.context = context;
	}

	public ConstraintContext getConstraintContext() {
		return context;
	}

	protected Object getProperty(String key, SearchNode node, Object defaultValue) {
		Map<String, Object> properties = nodeStack.getProperties(node);
		Object value = properties==null ? null : properties.get(key);
		value = value==null ? options.get(key) : value;
		return value==null ? defaultValue : value;
	}

	/**
	 * Moves the parse pointer until a non-whitespace
	 * character is encountered.
	 */
	protected void skipWS() {
		while(Character.isWhitespace(current())) {
			if(!hasNext()) {
				break;
			}
			next();
		}
	}

	/**
	 * Checks whether the end of the query string is reached
	 */
	protected boolean isEOS() {
		return index>=query.length();
	}

	/**
	 * Checks whether there are unread characters after the
	 * current pointer position
	 */
	protected boolean hasNext() {
		return index < query.length()-1;
	}

	/**
	 * Moves the pointer one step and returns the character
	 * at the new location
	 */
	protected char next() {
		index++;
		return current();
	}
	protected char tryNext() {
		index++;
		return !isEOS() ? current() : '\0';
	}

	/**
	 * Returns the character at the current pointer position
	 */
	protected char current() {
		return query.charAt(index);
	}

	protected char getAndStep() {
		char c = current();
		next();
		return c;
	}

	/**
	 * Moves the pointer back to the first position in the
	 * input string and resets the node stack
	 */
	protected void reset() {
		index = 0;
		nodeStack.reset();
		buffer.setLength(0);
		query = null;
	}

	public SearchGraph parseQuery(String query, Options options) throws ParseException {
		if(query==null)
			throw new NullPointerException("Invalid query"); //$NON-NLS-1$

		if(query.trim().isEmpty()) {
			return null;
		}

		if(options==null) {
			options = Options.emptyOptions;
		}

		reset();
		this.query = query.trim();

		skipWS();

		// Check for global properties
		if(current()==BRAKET_OPENING) {
			parseProperties();
			skipWS();
		}

		int rootOperator = SearchGraph.OPERATOR_CONJUNCTION;
		int disjunctionStart = index;

		// Check for disjunctive root operator
		if(current()==CURLYBRAKET_OPENING) {
			rootOperator = SearchGraph.OPERATOR_DISJUNCTION;
			nodeStack.setRootOperator(rootOperator);
			next();
		}

		boolean closed = rootOperator!=SearchGraph.OPERATOR_DISJUNCTION;

		// Now parse in all the root nodes
		while(!isEOS()) {
			skipWS();

			char c = current();

			if(c==SQUAREBRAKET_OPENING) {
				parseNode();
			} else if(c==CURLYBRAKET_CLOSING && rootOperator==SearchGraph.OPERATOR_DISJUNCTION) {
				closed = true;
				break;
			} else
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$
		}

		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed disjunction at index "+disjunctionStart), index); //$NON-NLS-1$

		nodeStack.close();

		Map<SearchNode, Map<String, Object>> pendingProperties = new HashMap<>();
		Map<String, Object> idMap = new HashMap<>();
		List<SearchEdge> edges = nodeStack.getEdges();
		List<SearchNode> nodes = nodeStack.getNodes();

		// Collect explicitly defined node ids
		for(SearchNode node : nodes) {
			Map<String, Object> properties = nodeStack.getProperties(node);
			String id = properties==null ? null : (String)properties.get(ID_OPTION);
			if(properties!=null && id!=null && !id.isEmpty()) {
				if(idMap.containsKey(id))
					throw new ParseException("Duplicate static node id: "+id, index); //$NON-NLS-1$

				node.setId(id);
				idMap.put(id, node);
				properties.remove(ID_OPTION);
			}
		}

		// Set edge ids
		String edgeNamePattern = options.get(EDGE_NAME_PATTERN_OPTION, DEFAULT_EDGE_NAME_PATTERN);
		int edgeIndex = 0;
		for(SearchEdge edge : edges) {
			String id = String.format(edgeNamePattern, _int(edgeIndex));
			if(idMap.containsKey(id))
				throw new ParseException("Duplicate edge id: "+id, index); //$NON-NLS-1$

			edge.setId(id);
			idMap.put(id, edge);
			edgeIndex++;
		}

		// Set remaining node ids and store pending meta-properties
		int nodeIndex = 0;
		for(SearchNode node : nodes) {
			String namePattern = (String) getProperty(NODE_NAME_PATTERN_OPTION, node, DEFAULT_NODE_NAME_PATTERN);

			if(idMap.containsKey(namePattern))
				throw new ParseException("Duplicate static id: "+namePattern, index); //$NON-NLS-1$

			// Generate and set id
			String id;
			while(idMap.containsKey((id=String.format(namePattern, _int(nodeIndex))))) {
				nodeIndex++;
			}
			idMap.put(id, node);
			nodeIndex++;

			Map<String, Object> properties = nodeStack.getProperties(node);
			if(properties==null || properties.isEmpty()) {
				continue;
			}

			// Parse and apply types
			if(properties.containsKey(NODETYPE_OPTION)) {
				node.setNodeType(NodeType.parseNodeType(
						(String)properties.get(NODETYPE_OPTION)));
				properties.remove(NODETYPE_OPTION);
			}
			if(properties.containsKey(EDGETYPE_OPTION)) {
				SearchEdge edge = nodeStack.getFrame(node).getEdge();
				edge.setEdgeType(EdgeType.parseEdgeType(
						(String)properties.get(EDGETYPE_OPTION)));
				properties.remove(EDGETYPE_OPTION);
			}

			pendingProperties.put(node, properties);
		}

		// Process remaining properties
		Set<String> links = new HashSet<>();
		for(SearchNode node : pendingProperties.keySet()) {
			Map<String, Object> properties = pendingProperties.get(node);
			for(Map.Entry<String, Object> entry : properties.entrySet()) {
				Order order = null;
				try {
					String s = (String)entry.getValue();
					int idx = s.indexOf(';');
					order = Order.parseOrder(s.substring(0, idx));
				} catch(Exception e) {
					// ignore
				}

				if(order!=null && order!=Order.UNDEFINED) {
					Object idRef = idMap.get(entry.getKey());
					if(idRef==null || !(idRef instanceof SearchNode))
						throw new ParseException("Unknown order target reference: "+entry.getKey(), index); //$NON-NLS-1$

					SearchNode target = (SearchNode)idRef;
					SearchEdge edge = null;

					if(order==Order.AFTER) {
						edge = new SearchEdge(node, target);
					} else if(order==Order.BEFORE) {
						edge = new SearchEdge(target, node);
					}

					// TODO ensure that there is no precedence edge between nodes in disjoint disjunctive sub-trees

					if(edge!=null) {
						edge.setEdgeType(EdgeType.PRECEDENCE);
						String link = edge.getSource().getId()+"_"+edge.getTarget().getId(); //$NON-NLS-1$

						if(links.contains(link))
							throw new ParseException("Duplicate link: "+link, index); //$NON-NLS-1$


						String[] parts = ((String)entry.getValue()).split(";"); //$NON-NLS-1$
						if(parts.length>1) {
							SearchConstraint[] constraints = new SearchConstraint[parts.length-1];
							for(int i=1; i<parts.length; i++) {
								constraints[i-1] = parseDistanceConstraint(parts[i]);
							}
							edge.setConstraints(constraints);
						}

						node.addEdge(edge, node==edge.getTarget());
						target.addEdge(edge, target==edge.getTarget());
						edges.add(edge);
						continue;
					}
				}

				throw new ParseException("Unknown property assignment: '"+entry.getKey()+"="+entry.getValue()+"'", index); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		// TODO ensure all generated ids are well formed (only alpha-numeric characters and underscores)!!


		// Collect root nodes and return graph
		List<SearchNode> roots = SearchUtils.getChildNodes(nodeStack.getVirtualRoot());

		SearchGraph graph = new SearchGraph();
		graph.setRootOperator(nodeStack.getRootOperator());
		graph.setEdges(edges.toArray(new SearchEdge[0]));
		graph.setNodes(nodes.toArray(new SearchNode[0]));
		graph.setRootNodes(roots.toArray(new SearchNode[0]));

		reset();

		return graph;
	}

	protected SearchConstraint parseDistanceConstraint(String s) {
		if(s.startsWith(LanguageConstants.DISTANCE_KEY)) {
			s = s.substring(LanguageConstants.DISTANCE_KEY.length());

			for(int i=0; i<s.length(); i++) {
				if(Character.isDigit(s.charAt(i))) {
					SearchOperator operator = SearchOperator.getOperator(s.substring(0, i));
					Integer value = Integer.getInteger(s.substring(i));

					return new SearchConstraint(LanguageConstants.DISTANCE_KEY, value, operator);
				}
			}
		}

		return null;
	}

	protected String parseUnquotedText() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'unquoted-text'-content"), index); //$NON-NLS-1$

		buffer.setLength(0);

		while(!isEOS()) {
			char c = current();

			if(isLegalId(c)) {
				buffer.append(c);
			} else {
				break;
			}

			if(!hasNext()) {
				break;
			}

			next();
		}

		if(buffer.length()==0)
			throw new ParseException(errorMessage(
					"Unexpected character at index "+index+" - expected alpha-numeric character [a-zA-Z0-9]"), index); //$NON-NLS-1$ //$NON-NLS-2$

		return buffer.toString();
	}

	protected String parseQuotedText() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'quoted-text'-content"), index); //$NON-NLS-1$

		char delimiter = getAndStep();

		if(delimiter!=QUOTATIONMARK && delimiter!=SINGLE_QUOTATIONMARK)
			throw new ParseException(errorMessage(
					"Illegal delimiter character for quoted text"), index); //$NON-NLS-1$

		int delimiterIndex = index;
		boolean escape = false;
		boolean closed = true;

		buffer.setLength(0);

		while(!isEOS()) {
			char c = current();

			if(c==delimiter) {
				closed = true;
				break;
			} else if(escape) {
				// Only escape delimiters, this prevents the need
				// of "escape-flooding" in regex-pattern
				if(c!=delimiter) {
					buffer.append(ESCAPE);
				}
				buffer.append(c);
				escape = false;
			} else if(c==ESCAPE) {
				escape = true;
			} else {
				buffer.append(c);
			}

			if(!hasNext()) {
				break;
			}

			next();
		}

		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed delimiter '"+delimiter+"' at index "+delimiterIndex), index); //$NON-NLS-1$ //$NON-NLS-2$

		tryNext();

		return buffer.toString();
	}

	protected String parseText() throws ParseException {
		if(current()==QUOTATIONMARK || current()==SINGLE_QUOTATIONMARK) {
			return parseQuotedText();
		}
		return parseUnquotedText();
	}

	protected String parseId() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'identifier'-content"), index); //$NON-NLS-1$

		buffer.setLength(0);

		while(!isEOS()) {
			char c = current();

			if(isLegalId(c)) {
				buffer.append(c);
			} else {
				break;
			}

			if(!hasNext()) {
				break;
			}

			next();
		}

		if(buffer.length()==0)
			throw new ParseException(errorMessage(
					"Unexpected non-letter character at index "+index+" - expected letter character [a-zA-Z_]"), index); //$NON-NLS-1$ //$NON-NLS-2$

		return buffer.toString();
	}

	protected Identifier parseIdentifier() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'identifier'-content"), index); //$NON-NLS-1$

		String token = parseId();
		String specifier = null;

		if(current()==SPECIFIER_DELIMITER) {
			next();
			specifier = parseText();
		}

		return new Identifier(token, specifier);
	}

	protected void parseProperty() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'property'-content"), index); //$NON-NLS-1$

		// Parse key
		String key = parseId();
		skipWS();

		// Ensure existence of equality sign
		if(getAndStep()!=EQUALITY_SIGN)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected equality sign '='"), index); //$NON-NLS-1$ //$NON-NLS-2$
		skipWS();

		// Parse value
		Object value = parseText();

		nodeStack.pushProperty(key, value);
	}

	protected SearchOperator parseSearchOperator() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'search-operator'-content"), index); //$NON-NLS-1$

		String s = ""; //$NON-NLS-1$
		while(!isEOS()) {
			char c = current();

			if(isLegalId(c)	|| c==QUOTATIONMARK || c==SINGLE_QUOTATIONMARK) {
				break;
			}

			s += current();

			if(!hasNext()) {
				break;
			}

			next();

			// Max length of any operator is 3 (GROUPING <*>)
			if(s.length()>=3) {
				break;
			}
		}

		SearchOperator operator = null;

		for(SearchOperator op : SearchOperator.values()) {
			if(op.getSymbol().equals(s)) {
				operator = op;
				break;
			}
		}

		if(operator==null)
			throw new ParseException(errorMessage(
					"Illegal search-operator prefix"), index); //$NON-NLS-1$

		return operator;
	}

	protected void parseConstraint() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'constraint'-content"), index); //$NON-NLS-1$

		Identifier identifier = parseIdentifier();
		Object specifier = identifier.getSpecifier();

		// Process token
		String fragment = identifier.getToken().toLowerCase();
		String token = fragment;
		if(options.get(EXPAND_TOKENS_OPTION, Boolean.TRUE).booleanValue() && context!=null) {
			token = context.completeToken(fragment);
		}
		if(token==null || (context!=null && !context.isRegistered(token)))
			throw new ParseException(errorMessage(
					"Unrecognized constraint token fragment '"+fragment+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$
		skipWS();

		token = identifier.getToken();

		// Parse operator
		SearchOperator operator = parseSearchOperator();
		if(context!=null) {
			ConstraintFactory factory = context.getFactory(token);
			if(!java.util.Arrays.asList(factory.getSupportedOperators()).contains(operator))
				throw new ParseException(errorMessage(
						"Unsupported operator '"+operator.getSymbol()+"' for token '"+token+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		skipWS();

		// Parse value
		Object value = parseValue(operator.isSupportNumerical());
//		Object value = parseText();

		if(context!=null) {
			ConstraintFactory factory = context.getFactory(token);
			value = factory.labelToValue(value, specifier);
		}

		nodeStack.pushConstraint(new SearchConstraint(token, value, operator, specifier));
	}
	
	private static boolean isNumberBegin(char c) {
		return c=='+' || c=='-' || Character.isDigit(c);
	}
	
	private static boolean isNumberFragment(char c) {
		return c=='.' || Character.isDigit(c);
	}
	
	protected Object parseValue(boolean isNumberAllowed) throws ParseException {
		if(isNumberAllowed && isNumberBegin(current())) {
			buffer.setLength(0);

			while(!isEOS()) {
				char c = current();

				if(isNumberFragment(c)) {
					buffer.append(c);
				} else {
					break;
				}

				if(!hasNext()) {
					break;
				}

				next();
			}

			if(buffer.length()==0)
				throw new ParseException(errorMessage(
						"Unexpected character at index "+index+" - expected alpha-numeric character [a-zA-Z0-9] or part of a floating point number"), index); //$NON-NLS-1$ //$NON-NLS-2$

			String value = buffer.toString();
			try {
				return Double.valueOf(value);
			} catch(NumberFormatException e) {
				// ignore
			}
			try {
				return Integer.valueOf(value);
			} catch(NumberFormatException e) {
				// ignore
			}
			
			// Not a valid number, treat it as a regular string
			return value;
		}
		
		return parseText();
	}

	protected void parseProperties() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'property-collection'-content"), index); //$NON-NLS-1$

		if(current()!=BRAKET_OPENING)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected opening bracket '('"), index); //$NON-NLS-1$ //$NON-NLS-2$
		int collectionStart = index;
		next();
		boolean closed = false;

		while(!isEOS()) {
			skipWS();

			char c = current();

			if(c==COMMA) {
				tryNext();
			} else if(c==BRAKET_CLOSING) {
				closed = true;
			} else if(isLegalId(c)) {
				parseProperty();
			} else if(c!=COLON)
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$

			if(closed) {
				break;
			}
		}

		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed properties collection at index "+collectionStart), index); //$NON-NLS-1$

		tryNext();
	}

	protected void parseDisjunction() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'node-collection'-content"), index); //$NON-NLS-1$

		if(current()!=CURLYBRAKET_OPENING)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected opening curly bracket '{'"), index); //$NON-NLS-1$ //$NON-NLS-2$
		int disjunctionStart = index;
		boolean closed = false;

		nodeStack.openNode();
		nodeStack.getCurrentNode().setNodeType(NodeType.DISJUNCTION);
		next();

		// Apply negation if specified
		if(current()==NEGATION_SIGN) {
			nodeStack.getCurrentNode().setNegated(true);
			next();
		}

		int optionCount = 0;

		while(!isEOS()) {
			skipWS();

			char c = current();

			if(c==COMMA) {
				tryNext();
			} else if(c==CURLYBRAKET_CLOSING) {
				closed = true;
			} else if(c==SQUAREBRAKET_OPENING) {
				parseNode();
				optionCount++;
			} else
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$

			if(closed) {
				break;
			}
		}

		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed disjunction definition at index "+disjunctionStart), index); //$NON-NLS-1$
		if(optionCount<2)
			throw new ParseException(errorMessage(
					"Missing disjunction member nodes - expected 2 or more, got "+optionCount), index); //$NON-NLS-1$

		nodeStack.closeNode();

		tryNext();
	}

	protected void parseNode() throws ParseException {
		if(isEOS())
			throw new ParseException(errorMessage(
					"Unexpected end of query string - expected 'node'-content"), index); //$NON-NLS-1$

		if(current()!=SQUAREBRAKET_OPENING)
			throw new ParseException(errorMessage(
					"Illegal character at index "+index+" - expected opening square bracket '['"), index); //$NON-NLS-1$ //$NON-NLS-2$

		// Remember start of node definition
		int nodeStart = index;
		boolean closed = false;

		nodeStack.openNode();
		next();

		// Apply negation if specified
		if(current()==NEGATION_SIGN) {
			nodeStack.getCurrentNode().setNegated(true);
			next();
		}

		while(!isEOS()) {
			skipWS();

			char c = current();

			if(c==COMMA) {
				tryNext();
			} else if(c==SQUAREBRAKET_OPENING) {
				parseNode();
			} else if(c==SQUAREBRAKET_CLOSING) {
				closed = true;
			} else if(c==CURLYBRAKET_OPENING) {
				parseDisjunction();
			} else if(c==BRAKET_OPENING) {
				parseProperties();
			} else if(isLegalId(c)) {
				parseConstraint();
			} else
				throw new ParseException(errorMessage(
						"Illegal character at index "+index), index); //$NON-NLS-1$

			if(closed) {
				break;
			}
		}

		if(!closed)
			throw new ParseException(errorMessage(
					"Unclosed node definition at index "+nodeStart), index); //$NON-NLS-1$

		nodeStack.closeNode();

		tryNext();
	}

	protected String errorMessage(String msg) {
		StringBuilder sb = new StringBuilder(query.length()*2);
		sb.append(msg).append(":\n\n"); //$NON-NLS-1$
		// Make output query fit one line and preserve total length
		sb.append(query.replaceAll("\r\n|\r|\n", " ")).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append(String.format("%-"+index+"s", "")).append("^"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		return sb.toString();
	}

	public String toQuery(SearchGraph graph, Options options) throws UnsupportedFormatException {
		if(graph==null)
			throw new NullPointerException("Invalid graph"); //$NON-NLS-1$
		if(SearchUtils.isEmpty(graph))
			throw new IllegalArgumentException("Empty graph"); //$NON-NLS-1$

//		if(options==null) {
//			options = Options.emptyOptions;
//		}

		reset();

		buffer.setLength(0);

		Set<SearchNode> idSet = new HashSet<>();
		for(SearchEdge edge : graph.getEdges()) {
			if(edge.getEdgeType()==EdgeType.PRECEDENCE) {
				idSet.add(edge.getTarget());
			}
		}

		Map<SearchNode, String> assignedIds = new HashMap<>();

		boolean isDisjuntive = graph.getRootOperator()==SearchGraph.OPERATOR_DISJUNCTION;

		if(isDisjuntive) {
			buffer.append(CURLYBRAKET_OPENING);
		}

		SearchNode[] roots = graph.getRootNodes();
		for(int i=0; i<roots.length; i++) {
			if(i>0) {
				buffer.append(SPACE);
			}

			appendNode(roots[i], null, idSet, assignedIds);
		}

		if(isDisjuntive) {
			buffer.append(CURLYBRAKET_CLOSING);
		}

		String result = buffer.toString();

		reset();

		return result;
	}

	protected String getId(SearchNode node, Map<SearchNode, String> assignedIds) {
		String id = assignedIds.get(node);
		if(id==null) {
			id = node.getId();
		}

		if(id==null || id.isEmpty() || "<undefined>".equals(id)) { //$NON-NLS-1$
			Set<String> ids = new HashSet<>(assignedIds.values());
			int count=0;

			do {
				id = "node"+count; //$NON-NLS-1$
				count++;
			} while(ids.contains(id));

			assignedIds.put(node, id);
		}
		return id;
	}

	protected String escapeEdgeConstraints(Order order, SearchConstraint[] constraints) {
		if(constraints==null || constraints.length==0) {
			return order.getToken();
		}

		StringBuilder sb = new StringBuilder().append('"').append(order.getToken());

		for(SearchConstraint constraint : constraints) {
			sb.append(';').append(constraint.getToken()).append(constraint.getOperator().getSymbol()).append(constraint.getValue());
		}

		sb.append('"');

		return sb.toString();
	}

	protected void appendNode(SearchNode node, SearchEdge head, Set<SearchNode> idSet, Map<SearchNode, String> assignedIds) {
		buffer.append(SQUAREBRAKET_OPENING);

		if(node.isNegated()) {
			buffer.append(NEGATION_SIGN).append(SPACE);
		}

		// Collect properties and meta-constraints
		CompactProperties properties = new CompactProperties();
		if(idSet.contains(node)) {
			properties.put(ID_OPTION, getId(node, assignedIds));
		}
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.PRECEDENCE) {
				String targetId = getId(edge.getTarget(), assignedIds);
				properties.put(targetId, escapeEdgeConstraints(Order.AFTER, edge.getConstraints()));
			}
		}
		if(node.getNodeType()!=NodeType.GENERAL) {
			properties.put(NODETYPE_OPTION, node.getNodeType().getToken());
		}
		if(head!=null && head.getEdgeType()!=EdgeType.DOMINANCE) {
			properties.put(EDGETYPE_OPTION, head.getEdgeType().getToken());
		}

		// Append properties
		Map<String, Object> propertiesMap = properties.asMap();
		if(propertiesMap!=null && !propertiesMap.isEmpty()) {
			boolean empty = true;
			buffer.append(BRAKET_OPENING);

			for(Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
				if(!empty) {
					buffer.append(COMMA).append(SPACE);
				}

				empty = false;

				buffer.append(entry.getKey());
				buffer.append(EQUALITY_SIGN);
				appendText(entry.getValue().toString());
			}

			buffer.append(BRAKET_CLOSING);
			buffer.append(SPACE);
		}

		// Append constraints
		boolean leadingComma = false;
		if(head!=null) {
			leadingComma = appendConstraints(head.getConstraints(), leadingComma);
		}
		leadingComma = appendConstraints(node.getConstraints(), leadingComma);
		if(leadingComma) {
			buffer.append(SPACE);
		}

		// Append sub-nodes
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);

			if(edge.getEdgeType()==EdgeType.PRECEDENCE
					|| edge.getEdgeType()==EdgeType.LINK) {
				continue;
			}

			SearchNode target = edge.getTarget();

			if(target.getNodeType()==NodeType.DISJUNCTION) {
				appendDisjunction(target, idSet, assignedIds);
			} else {
				appendNode(target, edge, idSet, assignedIds);
			}
		}

		StringUtil.trim(buffer);

		buffer.append(SQUAREBRAKET_CLOSING);
	}

	protected void appendDisjunction(SearchNode node, Set<SearchNode> idSet, Map<SearchNode, String> assignedIds) {
		buffer.append(CURLYBRAKET_OPENING);
		if(node.isNegated()) {
			buffer.append(NEGATION_SIGN).append(SPACE);
		}

		int nodeCount = 0;
		for(int i=0; i<node.getOutgoingEdgeCount(); i++) {
			SearchEdge edge = node.getOutgoingEdgeAt(i);
			if(edge.getEdgeType()==EdgeType.PRECEDENCE
					|| edge.getEdgeType()==EdgeType.LINK) {
				continue;
			}

			appendNode(edge.getTarget(), edge, idSet, assignedIds);
			nodeCount++;
		}

		if(nodeCount<2)
			throw new IllegalArgumentException("Missing disjunction node members - expected 2 or more, got "+nodeCount); //$NON-NLS-1$

		buffer.append(CURLYBRAKET_CLOSING);
	}

	protected boolean appendConstraints(SearchConstraint[] constraints, boolean leadingComma) {
		if(constraints==null || constraints.length==0) {
			return false;
		}

		int definedCount = 0;

		for(int i=0; i<constraints.length; i++) {
			SearchConstraint constraint = constraints[i];
			if(constraint.isUndefined()) {
				continue;
			}

			if(leadingComma || definedCount>0) {
				buffer.append(COMMA).append(SPACE);
			}

			buffer.append(constraint.getToken());

			String specifier = toString(constraint.getSpecifier());
			if(specifier!=null) {
				buffer.append(SPECIFIER_DELIMITER);
				appendText(specifier);
			}

			buffer.append(constraint.getOperator().getSymbol());

			Object value = constraint.getValue();
			String label;
			if(context!=null) {
				ConstraintFactory factory = context.getFactory(constraint.getToken());
				label = String.valueOf(factory.valueToLabel(value, specifier));
			} else {
				label = String.valueOf(value);
			}

			appendText(label);
			definedCount++;
		}

		return definedCount>0;
	}

	protected String toString(Object obj) {
		return obj==null ? null : obj.toString();
	}

	protected boolean requiresQuote(String s) {
		if(s==null || s.isEmpty()) {
			return false;
		}

		for(int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if(!isLegalId(c)) {
				return true;
			}
		}

		return false;
	}

	protected void appendText(String s) {
		if(requiresQuote(s)) {
			char delimiter = s.indexOf(QUOTATIONMARK)!=-1 ? SINGLE_QUOTATIONMARK : QUOTATIONMARK;
			s = s.replace(String.valueOf(delimiter), "\\"+delimiter); //$NON-NLS-1$

			buffer.append(delimiter).append(s).append(delimiter);
		} else {
			buffer.append(s);
		}
	}

	protected boolean isLegalId(char c) {
		return c==UNDERSCORE || Character.isLetter(c) || Character.isDigit(c);
	}

	protected static class Identifier {
		private String token;
		private String specifier;

		public Identifier() {
			// no-op
		}

		public Identifier(String token, String specifier) {
			this.token = token;
			this.specifier = specifier;
		}

		public String getToken() {
			return token;
		}

		public String getSpecifier() {
			return specifier;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public void setSpecifier(String specifier) {
			this.specifier = specifier;
		}
	}

	protected class NodeStack {
		private SearchNode virtualRoot = new SearchNode();

		private Stack<NodeStackFrame> stack = new Stack<>();

		private List<SearchNode> nodes = new ArrayList<>();
		private List<SearchEdge> edges = new ArrayList<>();

		private Map<SearchNode, NodeStackFrame> frameMap = new HashMap<>();

		// Lookup-table for all meta-data encountered so far
		private Map<SearchNode, Map<String, Object>> nodeProperties = new HashMap<>();

		public void pushConstraint(SearchConstraint constraint) throws ParseException {
			if(constraint==null)
				throw new NullPointerException("Invalid constraint"); //$NON-NLS-1$

			getFrame().addConstraint(constraint);
		}

		public void pushProperty(String key, Object value) throws ParseException {
			if(key==null)
				throw new NullPointerException("Invalid key"); //$NON-NLS-1$
			if(value==null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			if(stack.isEmpty()) {
				// Root meta-data
				Map<String, Object> properties = nodeProperties.get(virtualRoot);
				if(properties==null) {
					properties = new HashMap<>();
					nodeProperties.put(virtualRoot, properties);
				}

				if(properties.containsKey(key))
					throw new ParseException(errorMessage(
							"Duplicate property: "+key), index); //$NON-NLS-1$

				properties.put(key, value);
			} else {
				// Node-level meta-data
				getFrame().setProperty(key, value);
			}
		}

		private NodeStackFrame getFrame() throws ParseException {
			if(stack.isEmpty())
				throw new ParseException(errorMessage(
						"No node-frame available on the parse stack"), index); //$NON-NLS-1$

			return stack.peek();
		}

		public SearchNode getCurrentNode() throws ParseException {
			return getFrame().getNode();
		}

		public SearchEdge getCurrentEdge() throws ParseException {
			return getFrame().getEdge();
		}

		public NodeStackFrame getFrame(SearchNode node) {
			return frameMap.get(node);
		}

		public List<SearchNode> getNodes() {
			return nodes;
		}

		public List<SearchEdge> getEdges() {
			return edges;
		}

		public SearchNode getVirtualRoot() {
			return virtualRoot;
		}

		public Map<String, Object> getProperties(SearchNode node) {
			return nodeProperties.get(node);
		}

		/**
		 * Defines the operator used on the root level
		 * <p>
		 * Possible values are
		 * <ul>
		 * <li>{@value SearchGraph#OPERATOR_DISJUNCTION}</li>
		 * <li>{@value SearchGraph#OPERATOR_CONJUNCTION}</li>
		 * </ul>
		 */
		public void setRootOperator(int operator) {
			virtualRoot.setNodeType(operator==SearchGraph.OPERATOR_DISJUNCTION ?
					NodeType.DISJUNCTION : NodeType.GENERAL);
		}

		public int getRootOperator() {
			return virtualRoot.getNodeType()==NodeType.DISJUNCTION ?
					SearchGraph.OPERATOR_DISJUNCTION : SearchGraph.OPERATOR_CONJUNCTION;
		}

		public SearchNode closeNode() throws ParseException {
			if(stack.isEmpty()) {
				return null;
			}

			NodeStackFrame frame = stack.pop();
			frame.close();

			Map<String, Object> properties = frame.getProperties();
			if(properties!=null) {
				nodeProperties.put(frame.getNode(), properties);
			}

			return frame.getNode();
		}

		public void openNode() throws ParseException {
			NodeStackFrame frame;

			if(stack.isEmpty()) {
				// Only link the new node to the root, do NOT
				// apply a link backwards!
				frame = new NodeStackFrame();
				SearchEdge dummyEdge = new SearchEdge(
						virtualRoot, frame.getNode());
				virtualRoot.addEdge(dummyEdge, false);
			} else {
				// The new frame handles the linking to an existing
				// parent, so no need to worry about linking here
				SearchNode parent = getFrame().getNode();
				frame = new NodeStackFrame(parent);
				edges.add(frame.getEdge());
			}

			stack.push(frame);
			nodes.add(frame.getNode());
			frameMap.put(frame.getNode(), frame);
		}

		public void close() throws ParseException {
			if(!stack.isEmpty())
				throw new ParseException(errorMessage(
						stack.size()+" unclosed node definitions"), index); //$NON-NLS-1$
		}

		public void reset() {
			virtualRoot = new SearchNode();
			stack.clear();
			nodeProperties.clear();
			nodes.clear();
			edges.clear();
		}
	}

	private class NodeStackFrame {
		List<SearchConstraint> nodeConstraints;
		List<SearchConstraint> edgeConstraints;
		SearchNode node;
		SearchEdge edge;

		CompactProperties properties;

		NodeStackFrame() {
			this(null);
		}

		NodeStackFrame(SearchNode parent) {
			node = new SearchNode();
			nodeConstraints = new LinkedList<>();

			if (parent!=null) {
				edge = new SearchEdge();
				edgeConstraints = new LinkedList<>();

				edge.setSource(parent);
				edge.setTarget(node);

				parent.addEdge(edge, false);
				node.addEdge(edge, true);
			}
		}

		void setProperty(String key, Object value) throws ParseException {
			if(properties==null) {
				properties = new CompactProperties();
			}

			if(properties.get(key)!=null)
				throw new ParseException(errorMessage(
						"Duplicate property: "+key), index); //$NON-NLS-1$

			properties.put(key, value);
		}

		Map<String, Object> getProperties() {
			return properties==null ? null : properties.asMap();
		}

		SearchNode getNode() {
			return node;
		}

		SearchEdge getEdge() {
			return edge;
		}

		@SuppressWarnings("unused")
		private boolean containsConstraint(List<? extends SearchConstraint> constraints, SearchConstraint constraint) {
			for(SearchConstraint target : constraints) {
				if(target.getToken().equals(constraint.getToken())) {
					return true;
				}
			}

			return false;
		}

		void addConstraint(SearchConstraint constraint) throws ParseException {
			ConstraintFactory factory = getConstraintContext().getFactory(constraint.getToken());
			if(factory.getConstraintType()==ConstraintFactory.NODE_CONSTRAINT_TYPE) {
				if(nodeConstraints==null)
					throw new ParseException(errorMessage(
							"Unexpected node-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$

//				if(containsConstraint(nodeConstraints, constraint))
//					throw new ParseException(errorMessage(
//							"Duplicate node-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$

				nodeConstraints.add(constraint);
			} else {
				if(edgeConstraints==null)
					throw new ParseException(errorMessage(
							"Unexpected edge-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$

//				if(containsConstraint(nodeConstraints, constraint))
//					throw new ParseException(errorMessage(
//							"Duplicate edge-constraint '"+constraint.getToken()+"'"), index); //$NON-NLS-1$ //$NON-NLS-2$

				edgeConstraints.add(constraint);
			}
		}

		void close() throws ParseException {
			if(nodeConstraints!=null && !nodeConstraints.isEmpty()) {
				node.setConstraints(nodeConstraints.toArray(new SearchConstraint[0]));
			}

			if(edgeConstraints!=null && !edgeConstraints.isEmpty()) {
				edge.setConstraints(edgeConstraints.toArray(new SearchConstraint[0]));
			}
		}
	}
}
