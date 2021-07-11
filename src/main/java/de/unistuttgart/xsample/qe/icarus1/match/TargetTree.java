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

import java.util.Arrays;

import de.unistuttgart.xsample.qe.icarus1.LanguageConstants;
import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.SentenceData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;


/**
 * Rooted tree view on dependency data structures.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TargetTree {

	private int[][] edges;
	private boolean[][] locks;
	private int[] heights;
	private int[] descendantCounts;

	private IntList roots;
	private int rootCount = 0;

	private int[] heads;

	private int size;

	private SentenceData data;

	private int nodePointer = -1;

	// When edgePointer!=-1 then there is a valid edge list
	private int edgePointer = -1;

	private int bufferSize = 200;

	private static final int LIST_START_SIZE = 3;

	public TargetTree() {
		buildBuffer();
	}

	private void buildBuffer() {
		edges = new int[bufferSize][];
		locks = new boolean[bufferSize][];
		heights = new int[bufferSize];
		descendantCounts = new int[bufferSize];
		heads = new int[bufferSize];

		roots = new IntArrayList();
	}

	public void close() {
		edges = null;
		locks = null;
		heights = null;
		descendantCounts = null;
		heads = null;
		roots = null;

		data = null;
		size = 0;
	}
	
	public void reload(SentenceData source, Options options) {
		if(source==null)
			throw new NullPointerException("Invalid source data"); //$NON-NLS-1$
		
		data = source;

		size = fetchSize();
		int head;
		int[] list, tmp;

		if(size<edges.length) {
			// If buffer is sufficient reset all data
			reset();
		} else {
			// Otherwise expand buffer
			bufferSize = Math.max(size, bufferSize*2);
			buildBuffer();
		}

		// reset internal stuff
		for (int i = 0; i < size; i++) {
			descendantCounts[i] = 0;
			heights[i] = 0;

			list = edges[i];
			if (list != null) {
				list[0] = 0;
			}
		}

		// rebuild edge lookup and locks
		for (int i = 0; i < size; i++) {
			head = fetchHead(i);
			if(head == LanguageConstants.DATA_UNDEFINED_VALUE) {
				// ignore dangling heads, nothing we should restrict too much

//				System.out.println("dangling head at index "+(i+1)+" in "+data);
//				data = null;
//				throw new IllegalArgumentException("Data contains undefined head at index: "+i); //$NON-NLS-1$
			} else if (head == LanguageConstants.DATA_HEAD_ROOT) {
				roots.add(i);
			} else {
				list = edges[head];
				if (list == null) {
					// TODO validate initial list size (run corpus and count
					// number of arraycopy calls per data)
					list = new int[LIST_START_SIZE];
					edges[head] = list;
					locks[head] = new boolean[LIST_START_SIZE];
				} else if (list[0] >= list.length - 1) {
					tmp = new int[list.length + list.length];
					locks[head] = new boolean[tmp.length];
					System.arraycopy(list, 0, tmp, 0, list.length);
					list = tmp;
					edges[head] = list;
					tmp = null;
				}

				list[0]++;
				list[list[0]] = i;
				// System.out.printf("%d %s: %d %s\n", i, data.forms[i], head,
				// Arrays.toString(list));
			}

			if(locks[i]==null) {
				locks[i] = new boolean[LIST_START_SIZE];
			}

			heads[i] = head;
		}

		/* FIXED
		 * There are valid situations for not having any structure whatsoever in the supplied data
		 * and therefore we should not expect designated root nodes here.
		 */
//		if(roots.isEmpty())
//			throw new IllegalArgumentException("Structure is not a tree - no root defined"); //$NON-NLS-1$

		// refresh descendants counter and depth
		for(int root : roots) {
			prepareDescendants0(root);
		}
	}

	private void prepareDescendants0(int index) {
		int[] list = edges[index];

		// System.out.printf("preparing %d\n", index);

		if (list != null && list[0] != 0) {
			int idx;
			int value = list[0];
			int depth = 0;
			for (int i = 1; i <= list[0]; i++) {
				idx = list[i];
				prepareDescendants0(idx);
				value += descendantCounts[idx];
				depth = Math.max(depth, heights[idx]);
			}
			descendantCounts[index] = value;
			heights[index] = depth + 1;
		} else {
			descendantCounts[index] = 0;
			heights[index] = 1; // MARK 1
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getNodeIndex()
	 */
	public int getNodeIndex() {
		return edgePointer==-1 ? nodePointer : -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getEdgeIndex()
	 */
	public int getEdgeIndex() {
		return edgePointer;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getEdgeCount()
	 */
	public int getEdgeCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		int[] list = edges[nodePointer];

		return list==null ? 0 : list[0];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewEdge(int)
	 */
	public void viewEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		int[] list = edges[nodePointer];

		if(list==null || index<0 || index>=list[0])
			throw new IndexOutOfBoundsException("Edge index out of bounds: "+index); //$NON-NLS-1$

		edgePointer = index;
	}

	/**
	 *
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewEdge(int, int)
	 */
	public void viewEdge(int nodeIndex, int edgeIndex) {
		if(nodeIndex<0 || nodeIndex>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+nodeIndex); //$NON-NLS-1$

		int[] list = edges[nodeIndex];

		if(list==null || edgeIndex<0 || edgeIndex>=list[0])
			throw new IndexOutOfBoundsException("Edge index out of bounds: "+edgeIndex); //$NON-NLS-1$

		nodePointer = nodeIndex;
		edgePointer = edgeIndex;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getSourceIndex()
	 */
	public int getSourceIndex() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return nodePointer;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getTargetIndex()
	 */
	public int getTargetIndex() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return edges[nodePointer][1+edgePointer];
	}

	public boolean isRoot() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return heads[nodePointer]==LanguageConstants.DATA_HEAD_ROOT;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getParentIndex()
	 */
	public int getParentIndex() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return heads[nodePointer];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewNode(int)
	 */
	public void viewNode(int index) {
		if(index<0 || index>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+index); //$NON-NLS-1$

		nodePointer = index;
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewChild(int)
	 */
	public void viewChild(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		int[] list = edges[nodePointer];

		if(list==null || index<0 || index>=list[0])
			throw new IndexOutOfBoundsException("Child index out of bounds: "+index); //$NON-NLS-1$

		nodePointer = list[1+index];
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getChildIndexAt(int, int)
	 */
	public int getChildIndexAt(int nodeIndex, int index) {
		if(nodeIndex<0 || nodeIndex>=size)
			throw new IndexOutOfBoundsException("Node index out of bounds: "+nodeIndex); //$NON-NLS-1$

		int[] list = edges[nodeIndex];

		if(list==null || index<0 || index>=list[0])
			throw new IndexOutOfBoundsException("Child index out of bounds: "+index); //$NON-NLS-1$

		return list[1+index];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewParent()
	 */
	public void viewParent() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$
		if(heads[nodePointer]==-1)
			throw new IllegalStateException("Current node is the root node"); //$NON-NLS-1$

		nodePointer = heads[nodePointer];
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewTarget()
	 */
	public void viewTarget() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		nodePointer = edges[nodePointer][1+edgePointer];
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#viewSource()
	 */
	public void viewSource() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		// nodePointer is already set to the source of the current edge!
		edgePointer = -1;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getHeight()
	 */
	public int getHeight() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return heights[nodePointer];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#getDescendantCount()
	 */
	public int getDescendantCount() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return descendantCounts[nodePointer];
	}

	// LOCKING METHODS

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockNode()
	 */
	public void lockNode() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		locks[nodePointer][0] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockEdge()
	 */
	public void lockEdge() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		locks[nodePointer][1+edgePointer] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockEdge(int)
	 */
	public void lockEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		locks[nodePointer][1+index] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockEdge(int, int)
	 */
	public void lockEdge(int nodeIndex, int index) {
		locks[nodeIndex][1+index] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#lockNode(int)
	 */
	public void lockNode(int index) {
		locks[index][0] = true;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockNode()
	 */
	public void unlockNode() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		unlockNode(nodePointer);
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockEdge()
	 */
	public void unlockEdge() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		locks[nodePointer][1+edgePointer] = false;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockEdge(int)
	 */
	public void unlockEdge(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		locks[nodePointer][1+index] = false;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockEdge(int, int)
	 */
	public void unlockEdge(int nodeIndex, int index) {
		locks[nodeIndex][1+index] = false;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockNode(int)
	 */
	public void unlockNode(int index) {
		locks[index][0] = false;
		int[] list = edges[index];

		// Unlock all edges for this node!
		if(list!=null) {
			boolean[] lock = locks[index];
			for(int i=1; i<=list[0]; i++) {
				lock[i] = false;
			}
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockChildren(int)
	 */
	public void unlockChildren(int index) {
		int[] list = edges[index];

		if(list!=null) {
			for(int i=1; i<=list[0]; i++) {
				unlockNode(list[i]);
			}
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isNodeLocked()
	 */
	public boolean isNodeLocked() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return locks[nodePointer][0];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isEdgeLocked()
	 */
	public boolean isEdgeLocked() {
		if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return locks[nodePointer][1+edgePointer];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isNodeLocked(int)
	 */
	public boolean isNodeLocked(int index) {
		return locks[index][0];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isEdgeLocked(int)
	 */
	public boolean isEdgeLocked(int index) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return locks[nodePointer][1+index];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#isEdgeLocked(int, int)
	 */
	public boolean isEdgeLocked(int nodeIndex, int index) {
		return locks[nodeIndex][1+index];
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#unlockAll()
	 */
	public void unlockAll() {
		for(int i=0; i<size; i++) {
			int[] list = edges[i];
			if(list!=null) {
				Arrays.fill(locks[i], 0, list[0], false);
			}
		}
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#size()
	 */
	public int size() {
		return size;
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.TargetTree#reset()
	 */
	public void reset() {
		nodePointer = -1;
		edgePointer = -1;
		roots.clear();

		unlockAll();
	}

	public SentenceData getSource() {
		return data;
	}

	private boolean supports(Object data) {
		return data instanceof SentenceData;
	}

	private int fetchSize() {
		return data.length();
	}

	private int fetchHead(int index) {
		return data.getHead(index);
	}



	// NODE METHODS

	public String getForm() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getForm(nodePointer);
	}

	public String getPos() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getPos(nodePointer);
	}

	public String getLemma() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getLemma(nodePointer);
	}

	/**
	 * Returns an always non-null array of feature expressions
	 */
	public String getFeatures() {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.getFeatures(nodePointer);
	}

	// EDGE METHODS

	public String getRelation() {
		/*if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$*/
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		return data.getRelation(nodePointer);
	}

	public int getDistance() {
		/*if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$*/
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		int head = heads[nodePointer];

		return head==LanguageConstants.DATA_HEAD_ROOT ?
				LanguageConstants.DATA_UNDEFINED_VALUE : Math.abs(head-nodePointer);
	}

	public int getDirection() {
		/*if(edgePointer==-1)
			throw new IllegalStateException("Current scope is not on an edge"); //$NON-NLS-1$*/
		if(nodePointer==-1)
			throw new IllegalStateException("Scope on edge but node pointer cleared"); //$NON-NLS-1$

		int head = heads[nodePointer];

		if(head==LanguageConstants.DATA_HEAD_ROOT) {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}

		return nodePointer<head ?
				LanguageConstants.DATA_LEFT_VALUE : LanguageConstants.DATA_RIGHT_VALUE;
	}

	// GENERAL METHODS

	public boolean isFlagSet(long flag) {
		if(nodePointer==-1)
			throw new IllegalStateException("Current scope is not on a node"); //$NON-NLS-1$

		return data.isFlagSet(nodePointer, flag);
	}

	public Object getProperty(String key) {
		return getSource().getProperty(nodePointer, key);
	}

	// LOCKING METHODS

}
