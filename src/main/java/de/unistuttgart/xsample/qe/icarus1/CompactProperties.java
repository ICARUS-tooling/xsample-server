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

import static de.unistuttgart.xsample.util.XSampleUtils._double;
import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CompactProperties implements Cloneable, Serializable {

	private static final long serialVersionUID = -492641053997637443L;

	private static final char ASSIGNMENT_CHAR = ':';
	private static final char SEPARATOR_CHAR = ';';

	protected Object table;

	protected static final int ARRAY_SIZE_LIMIT = 8;


	@SuppressWarnings("unchecked")
	public Object get(String key) {
		requireNonNull(key);

		if(table==null)
			return null;

		if(table instanceof Object[]) {
			Object[] table = (Object[]) this.table;
			for(int i = 0; i<table.length-1; i+=2)
				if(table[i]!=null && key.equals(table[i]))
					return table[i+1];

			return null;
		} else {
			return ((Map<String, Object>) table).get(key);
		}
	}

	public int size() {
		if(table==null) {
			return 0;
		} else if(table instanceof Object[]) {
			return ((Object[])table).length;
		} else {
			return ((Map<?, ?>)table).size();
		}
	}

	protected void grow() {
		Map<String, Object> map = new LinkedHashMap<>();
		Object[] table = (Object[]) this.table;

		for(int i=1; i<table.length; i+=2)
			if(table[i-1]!=null && table[i]!=null)
				map.put((String)table[i-1], table[i]);

		this.table = map;
	}

	@SuppressWarnings("unchecked")
	protected void shrink() {
		Map<String,Object> map = (Map<String, Object>) this.table;
		Object[] table = new Object[map.size()*2];
		int index = 0;
		for(Entry<String, Object> entry : map.entrySet()) {
			table[index++] = entry.getKey();
			table[index++] = entry.getValue();
		}

		this.table = table;
	}

	protected void clear() {
		table = null;
	}

	@SuppressWarnings("unchecked")
	public void put(String key, Object value) {
		requireNonNull(key);

		// nothing to do here
		if(value==null && table==null)
			return;

		if(table==null) {
			// INITIAL mode
			Object[] table = new Object[4];
			table[0] = key;
			table[1] = value;

			this.table = table;
		} else if(table instanceof Object[]) {
			// ARRAY mode and array is set
			Object[] table = (Object[]) this.table;
			int emptyIndex = -1;

			// try to insert
			for(int i=0; i<table.length-1; i+=2) {
				if(table[i]==null) {
					emptyIndex = i;
				} else if(key.equals(table[i])) {
					table[i+1] = value;
					if(value==null)
						table[i] = null;
					return;
				}
			}

			// key not present
			if(emptyIndex!=-1) {
				// empty slot available
				table[emptyIndex] = key;
				table[emptyIndex+1] = value;
			} else if(value!=null) { // only bother for non-null mappings
				// no empty slot found -> need to expand
				int size = table.length;
				Object[] newTable = new Object[size+2];
				System.arraycopy(table, 0, newTable, 0, size);
				newTable[size] = key;
				newTable[size+1] = value;
				this.table = newTable;

				if(++size > ARRAY_SIZE_LIMIT) {
					grow();
				}
			}

		} else {
			// TABLE mode
			Map<String,Object> table = (Map<String, Object>)this.table;

			if(value==null)
				table.remove(key);
			else
				table.put(key, value);

			if(table.size()<ARRAY_SIZE_LIMIT) {
				shrink();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> asMap() {
		Map<String, Object> map;

		if(table==null) {
			map = null;
		} else if(table instanceof Object[]) {
			map = new LinkedHashMap<>();
			Object[] table = (Object[]) this.table;
			for(int i=1; i<table.length; i+=2) {
				if(table[i-1]!=null && table[i]!=null) {
					map.put((String)table[i-1], table[i]);
				}
			}
		} else {
			map = new LinkedHashMap<>((Map<String, Object>)this.table);
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompactProperties clone() {
		CompactProperties clone = null;
		try {
			clone = (CompactProperties) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}

		if(table instanceof Object[]) {
			clone.table = ((Object[])table).clone();
		} else if(table!=null) {
			clone.table = new LinkedHashMap<>((Map<String, Object>) clone.table);
		}

		return clone;
	}

	@Override
	public String toString() {
		if(table==null) {
			return ""; //$NON-NLS-1$
		}

		StringBuilder sb = new StringBuilder();
		appendTo(sb);

		return sb.toString();
	}

	public void appendTo(StringBuilder sb) {

		if(table instanceof Object[]) {
			Object[] items = (Object[]) table;
			int maxI = items.length-1;
			for(int i=0; i<maxI; i+=2) {
				if(items[i]==null || items[i+1]==null) {
					continue;
				}
				sb.append(items[i]).append(ASSIGNMENT_CHAR)
					.append(items[i+1]).append(SEPARATOR_CHAR);
			}
		} else if(table!=null) {
			Map<?, ?> map = (Map<?, ?>) table;
			for(Entry<?, ?> entry : map.entrySet()) {
				if(entry.getValue()==null) {
					continue;
				}
				sb.append(entry.getKey()).append(ASSIGNMENT_CHAR)
					.append(entry.getValue()).append(SEPARATOR_CHAR);
			}
		}
	}

	public static CompactProperties parse(Splitable s) {
		return parse(s, 0);
	}

	public static CompactProperties parse(Splitable s, int from) {
		if(s==null || s.isEmpty()) {
			return null;
		}

		CompactProperties properties = new CompactProperties();
		int maxIndex = s.length()-1;
		int startIndex = from;
		while(startIndex<maxIndex) {
			int offset0 = s.indexOf(ASSIGNMENT_CHAR, startIndex);
			if(offset0==-1)
				throw new NullPointerException("Invalid properties source string: "+s); //$NON-NLS-1$
			int endIndex = s.indexOf(SEPARATOR_CHAR, offset0);
			if(endIndex==-1) {
				endIndex = s.length();
			}

			Splitable sKey = s.subSequence(startIndex, offset0);
			Splitable sValue = s.subSequence(offset0+1, endIndex);

			properties.put(sKey.toString(), toValue(sValue));

			sKey.recycle();
			sValue.recycle();

			startIndex = endIndex+1;
		}

		return properties;
	}

	public static CompactProperties parse(String s) {
		return parse(s, 0);
	}

	public static CompactProperties parse(String s, int from) {
		if(s==null || s.isEmpty()) {
			return null;
		}

		CompactProperties properties = new CompactProperties();
		int maxIndex = s.length()-1;
		int startIndex = from;
		while(startIndex<maxIndex) {
			int offset0 = s.indexOf(ASSIGNMENT_CHAR, startIndex);
			if(offset0==-1)
				throw new NullPointerException("Invalid properties source string: "+s); //$NON-NLS-1$
			int endIndex = s.indexOf(SEPARATOR_CHAR, offset0);
			if(endIndex==-1) {
				endIndex = s.length();
			}

			String sKey = s.substring(startIndex, offset0);
			String sValue = s.substring(offset0+1, endIndex);

			properties.put(sKey.toString(), toValue(sValue));

			startIndex = endIndex+1;
		}

		return properties;
	}

	private static Object toValue(Splitable s) {
		try {
			return _int(StringPrimitives.parseInt(s));
		} catch(NumberFormatException e) {
			// ignore
		}
		try {
			return _double(StringPrimitives.parseDouble(s));
		} catch(NumberFormatException e) {
			// ignore
		}

		return s.toString();
	}

	private static Object toValue(String s) {
		try {
			return _int(StringPrimitives.parseInt(s));
		} catch(NumberFormatException e) {
			// ignore
		}
		try {
			return _double(StringPrimitives.parseDouble(s));
		} catch(NumberFormatException e) {
			// ignore
		}

		return s;
	}

	public static CompactProperties subset(CompactProperties source, int index) {
		if(source==null) {
			return null;
		}
		CompactProperties properties = new CompactProperties();

		String suffix = '_'+String.valueOf(index);

		Object table = source.table;

		if(table instanceof Object[]) {
			Object[] items = (Object[]) table;
			int maxI = items.length-1;
			for(int i=0; i<maxI; i+=2) {
				if(items[i]==null || items[i+1]==null) {
					continue;
				}
				String key = (String) items[i];
				if(key.endsWith(suffix)) {
					key = key.substring(0, key.length()-suffix.length());
					properties.put(key, items[i+1]);
				}
			}
		} else if(table!=null) {
			Map<?, ?> map = (Map<?, ?>) table;
			for(Entry<?, ?> entry : map.entrySet()) {
				if(entry.getValue()==null) {
					continue;
				}

				String key = (String) entry.getKey();
				if(key.endsWith(suffix)) {
					key = key.substring(0, key.length()-suffix.length());
					properties.put(key, entry.getValue());
				}
			}
		}

		return properties;
	}

	public static CompactProperties subset(CompactProperties source,
			int index0, int index1) {
		if(source==null) {
			return null;
		}
		CompactProperties properties = new CompactProperties();

		Map<String, Object> map = source.asMap();

		for(int i=index0; i<=index1; i++) {
			String suffix = '_'+String.valueOf(i);
			Iterator<Entry<String, Object>> it = map.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, Object> entry = it.next();
				String key = entry.getKey();

				if(entry.getValue()==null) {
					it.remove();
					continue;
				}

				if(key.endsWith(suffix)) {
					key = key.substring(0, key.length()-suffix.length());
					properties.put(key, entry.getValue());
					it.remove();
				}
			}
		}

		return properties;
	}

//	public static void countKeys(CompactProperties properties,
//			Counter counter) {
//
//		if(counter==null)
//			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
//
//		if(properties==null || properties.size()==0) {
//			return;
//		}
//
//		Object table = properties.table;
//
//		if(table instanceof Object[]) {
//			Object[] items = (Object[]) table;
//			int maxI = items.length-1;
//			for(int i=0; i<maxI; i+=2) {
//				if(items[i]==null || items[i+1]==null) {
//					continue;
//				}
//				counter.increment(getRawKey((String) items[i]));
//			}
//		} else if(table!=null) {
//			Map<?, ?> map = (Map<?, ?>) table;
//			for(Entry<?, ?> entry : map.entrySet()) {
//				if(entry.getValue()==null) {
//					continue;
//				}
//
//				counter.increment(getRawKey((String) entry.getKey()));
//			}
//		}
//	}

	public static void collectKeys(CompactProperties properties,
			Collection<String> target) {
		if(target==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$

		if(properties==null || properties.size()==0) {
			return;
		}

		Object table = properties.table;

		if(table instanceof Object[]) {
			Object[] items = (Object[]) table;
			int maxI = items.length-1;
			for(int i=0; i<maxI; i+=2) {
				if(items[i]==null || items[i+1]==null) {
					continue;
				}
				target.add(getRawKey((String) items[i]));
			}
		} else if(table!=null) {
			Map<?, ?> map = (Map<?, ?>) table;
			for(Entry<?, ?> entry : map.entrySet()) {
				if(entry.getValue()==null) {
					continue;
				}

				target.add(getRawKey((String) entry.getKey()));
			}
		}
	}

	private static String getRawKey(String key) {
		int idx = key.lastIndexOf('_');
		if(idx==-1) {
			return key;
		}

		int len = key.length();
		for(int i=idx+1; i<len; i++) {
			if(!Character.isDigit(key.charAt(i))) {
				return key;
			}
		}

		return key.substring(0, idx);
	}
}
