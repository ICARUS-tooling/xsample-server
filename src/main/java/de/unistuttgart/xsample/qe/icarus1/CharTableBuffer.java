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
package de.unistuttgart.xsample.qe.icarus1;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CharTableBuffer {

//	public static void main(String[] args) throws Exception {
//
//		File file = new File("D:/Workspaces/Default/Icarus/data/treebanks/CoNLL2009-ST-English-development.txt"); //$NON-NLS-1$
//
//		Reader reader = new BufferedReader(new FileReader(file));
//
//		CharTableBuffer buffer = new CharTableBuffer(100, 200);
//
//		buffer.startReading(reader);
//
//		buffer.next();
//
//		for(int i=0; i<buffer.getRowCount(); i++) {
//			Row row = buffer.getRow(i);
//			System.out.println(row);
//
//			row.split('\t');
//
//			for(int j=0; j<row.getSplitCount(); j++) {
//				Cursor c = row.getSplitCursor(j);
//				System.out.println(c);
//				c.recycle();
//			}
//
//			if(i>0)
//				break;
//		}
//
//		buffer.close();
//	}

	// Data storage
	private Row[] rows;
	private int columns;
	private int height = 0;

	// Data source
	private Reader reader;
	private char[] buffer;
	private boolean ignoreLF = false;

	private RowFilter rowFilter;

	private int blockBegin, blockEnd = 0;

	// Cursor cursorCache
	private Stack<Cursor> cursorCache = new Stack<>();
	private Map<String, Matcher> regexCache;

	public CharTableBuffer() {
		this(50, 100);
	}

	public CharTableBuffer(int rows, int columns) {

		this.columns = columns;

		this.rows = new Row[rows];
	}

	public String getErrorMessage(String prefix) {
		return String.format(
				"%s - error in block:\n==== starting at line %d\n%s\n==== ending at line %d", //$NON-NLS-1$
				prefix, getBlockBegin(), toString(), getBlockEnd());
	}

	public void startReading(Reader reader) throws IOException {
		if (reader == null)
			throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

		reset();

		this.reader = reader;

		if(buffer==null) {
			buffer = new char[8000];
		}

		ignoreLF = false;
	}

	public boolean next() throws IOException {
		if(reader==null)
			throw new IllegalStateException("No reader initialized"); //$NON-NLS-1$

		int lines = 0;
		height = 0;

		//TODO read empty lines till first content or end of stream?

		line_loop: for(;;) {
			Row row = readLine0();

			if(row==null) {
				// End of stream
				truncate();
				break line_loop;
			} else if(rowFilter==null) {
				lines++;
				// If no custom row handling is defined use the first empty row as delimiter
				if(row.length()==0) {
					truncate();
					break line_loop;
				}
			} else {
				lines++;
				// Allow custom filter to decide
				switch (rowFilter.getRowAction(row)) {
				case END_OF_TABLE:
					truncate();
					break line_loop;

				case IGNORE:
					truncate();
					break;

				default:
					break;
				}
			}
		}

		if(lines>0) {
			blockBegin = blockEnd+1;
			blockEnd = blockBegin+lines-1;
		}

		return height>0;
	}

	private void truncate() {
		if(height==0)
			throw new IllegalStateException();

		height--;
	}


	/**
	 * @return the rowFilter
	 */
	public RowFilter getRowFilter() {
		return rowFilter;
	}

	/**
	 * @param rowFilter the rowFilter to set
	 */
	public void setRowFilter(RowFilter rowFilter) {
		this.rowFilter = rowFilter;
	}


	private static final char CR = '\r';
	private static final char LF = '\n';

	/**
	 * Reads characters from the underlying reader until the end of the stream
	 * or a linebreak occurs. Returns the length of that line.
	 */
	private Row readLine0() throws IOException {
		int nextChar = 0;

		boolean eos = false;

		char_loop : for(;;) {
			int c = reader.read();

			switch (c) {
			case -1:
				eos = true;
				break char_loop;

			case CR:
				ignoreLF = true;
				break char_loop;

			case LF:
				if(!ignoreLF)
					break char_loop;
				break;

			default:
				if(nextChar>=buffer.length) {
					buffer = Arrays.copyOf(buffer, nextChar*2+1);
				}
				buffer[nextChar++] = (char) c;
				ignoreLF = false;
				break;
			}
		}

		Row row = nextRow();

		row.set(buffer, 0, nextChar);

		return row.isEmpty() && eos ? null : row;
	}

	public void reset() throws IOException {
		if(reader!=null) {
			reader.close();
			reader = null;
		}

		ignoreLF = false;
	}

	public void close() throws IOException {
		reset();
		buffer = null;

		if(rows!=null) {
			for(Row row : rows) {
				if(row!=null) {
					row.close();
				}
			}
			rows = null;
		}

		for(Cursor cursor : cursorCache) {
			cursor.closeSplits();
		}
	}

	private Row nextRow() {
		int index = height;

		if(index>=rows.length) {
//			System.out.println("Expanding row buffer to "+(index*2+1));
			rows = Arrays.copyOf(rows, index*2+1);
		}

		Row row = rows[index];
		if(row==null) {
//			System.out.println("creating new row for index "+index);
			row = rows[index] = new Row(index, columns);
		}

		height++;

		return row;
	}

	public Row getRow(int index) {
		if(index>=height)
			throw new IndexOutOfBoundsException();

		return rows[index];
	}

	public int getRowCount() {
		return height;
	}

	public boolean isEmpty() {
		return height==0;
	}

	private Cursor getCursor0(int row, int index0, int index1) {
		Cursor cursor = null;

		// Check cursorCache
		if(!cursorCache.isEmpty()) {
			cursor = cursorCache.pop();
		}

		// Create new cursor only if required
		if(cursor==null) {
//			System.out.printf("creating new cursor: row=%d from=%d to=%d\n",row, index0, index1);
			cursor = new Cursor();
		} else {
			cursor.resetSplits();
		}

		// Now set scope
		cursor.setRow(row);
		cursor.setIndex0(index0);
		cursor.setIndex1(index1);
		cursor.resetHash();

		return cursor;
	}

	private void recycleCursor0(Cursor cursor) {
		if (cursor == null)
			throw new NullPointerException("Invalid cursor"); //$NON-NLS-1$
		cursorCache.push(cursor);
	}

	private Matcher getMatcher0(String regex, CharSequence input) {
		if (regex == null)
			throw new NullPointerException("Invalid regex"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		Matcher m = regexCache.remove(regex);

		if(m==null) {
//			System.out.println("Compiling pattern: "+regex);
			m = Pattern.compile(regex).matcher(input);

//			regexCache.put(regex, m);
		} else {
			m.reset(input);
		}

		return m;
	}

	private void recycleMatcher0(Matcher matcher) {
		if (matcher == null)
			throw new NullPointerException("Invalid matcher"); //$NON-NLS-1$

		if(regexCache==null) {
			regexCache = new HashMap<>();
		}

		matcher.reset();
		regexCache.put(matcher.pattern().pattern(), matcher);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if(isEmpty()) {
			sb.append("<empty buffer>"); //$NON-NLS-1$
		} else {
			for(int i=0; i<getRowCount(); i++) {
				if(i>0) {
					sb.append('\n');
				}
				sb.append(getRow(i).toString());
			}
		}

		return sb.toString();
	}

	/**
	 * @return the blockBegin
	 */
	public int getBlockBegin() {
		return blockBegin;
	}

	/**
	 * @return the blockEnd
	 */
	public int getBlockEnd() {
		return blockEnd;
	}

	public class Cursor extends Splitable {

		private int row, index0, index1;

		/**
		 * @see java.lang.CharSequence#length()
		 */
		@Override
		public int length() {
			return index1-index0+1;
		}

		/**
		 * @see java.lang.CharSequence#charAt(int)
		 */
		@Override
		public char charAt(int index) {
			if(index>index1-index0)
				throw new IndexOutOfBoundsException();

			return getRow(row).charAt(index0+index);
		}

		private void setRow(int row) {
			this.row = row;
		}

		private void setIndex0(int index0) {
			this.index0 = index0;
		}

		private void setIndex1(int index1) {
			this.index1 = index1;
		}

		@Override
		public void recycle() {
			row = index0 = index1 = -1;

			recycleCursor0(this);
		}

		/**
		 * @see de.ims.icarus.util.strings.AbstractString#subSequence(int, int)
		 */
		@Override
		public Cursor subSequence(int start, int end) {
			return getCursor0(row, index0+start, index0+end-1);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getCachedMatcher(java.lang.String)
		 */
		@Override
		protected Matcher getCachedMatcher(String regex) {
			return getMatcher0(regex, this);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#recycleMatcher(java.util.regex.Matcher)
		 */
		@Override
		protected void recycleMatcher(Matcher matcher) {
			recycleMatcher0(matcher);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getSplitCursor(int)
		 */
		@Override
		public Cursor getSplitCursor(int index) {
			return (Cursor) super.getSplitCursor(index);
		}
	}

	public class Row extends Splitable {

		// Row pointer
		private final int rowIndex;

		// Data storage
		private char[] buffer;
		private int width = 0;

		private Row(int rowIndex, int size) {
			this.rowIndex = rowIndex;
			buffer = new char[size];
		}

		public int getRowIndex() {
			return rowIndex;
		}

		private void reset() {
			width = 0;
			resetSplits();
			resetHash();
		}

		private void close() {
			reset();
			buffer = null;
			closeSplits();
		}

		private void ensureCapacity(int capacity) {
			if(capacity>=buffer.length) {
				capacity = 2*capacity+1;
//				System.out.println("expanding capacity of row "+rowIndex+" to "+capacity);
				buffer = Arrays.copyOf(buffer, capacity);
			}
		}

//		private void append(CharSequence s) {
//			int l = s.length();
//			ensureCapacity(width+l);
//
//			for(int i=0; i<l;i++) {
//				buffer[width++] = s.charAt(i);
//			}
//
//			resetHash();
//		}

//		private void append(char[] c, int offset, int len) {
//			ensureCapacity(width+len);
//
//			System.arraycopy(c, offset, buffer, width, len);
//			width += len;
//
//			resetHash();
//		}

		private void set(char[] c, int offset, int len) {
			ensureCapacity(len);

			System.arraycopy(c, offset, buffer, 0, len);
			width = len;

			resetHash();
		}

		/**
		 * @see java.lang.CharSequence#length()
		 */
		@Override
		public int length() {
			return width;
		}

		/**
		 * @see java.lang.CharSequence#charAt(int)
		 */
		@Override
		public char charAt(int index) {
			if(index>=width)
				throw new IndexOutOfBoundsException();

			return buffer[index];
		}

		/**
		 * @see de.ims.icarus.util.strings.AbstractString#subSequence(int, int)
		 */
		@Override
		public Cursor subSequence(int start, int end) {
			return getCursor0(rowIndex, start, end-1);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getCachedMatcher(java.lang.String)
		 */
		@Override
		protected Matcher getCachedMatcher(String regex) {
			return getMatcher0(regex, this);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#recycleMatcher(java.util.regex.Matcher)
		 */
		@Override
		protected void recycleMatcher(Matcher matcher) {
			recycleMatcher0(matcher);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#getSplitCursor(int)
		 */
		@Override
		public Cursor getSplitCursor(int index) {
			return (Cursor) super.getSplitCursor(index);
		}

		/**
		 * @see de.ims.icarus.util.strings.Splitable#recycle()
		 */
		@Override
		public void recycle() {
			// no-op
		}
	}

	public enum RowAction {
		IGNORE,
		VALID,
		END_OF_TABLE;
	}

	public interface RowFilter {
		RowAction getRowAction(Row row);
	}
}
