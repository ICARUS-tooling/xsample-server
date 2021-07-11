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
package de.unistuttgart.xsample.qe.icarus1;

import static de.unistuttgart.xsample.util.XSampleUtils._int;

import java.util.Map;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SentenceData implements Cloneable {

	private static final long serialVersionUID = 6042849879442946254L;

	protected CompactProperties properties;

	protected int index = -1;

	protected Map<Key, Object> indexedProperties;

	protected String[] forms;

	public SentenceData(String...forms) {
		if(forms==null)
			throw new NullPointerException("Invalid forms array"); //$NON-NLS-1$

		this.forms = forms;
	}

	public SentenceData(SentenceData source) {
		this.forms = IntStream.range(0, source.length())
				.mapToObj(source::getForm)
				.toArray(String[]::new);
	}

	public SentenceData() {
		//no-op
	}

	public void setForms(String[] forms) {
		if(this.forms!=null)
			throw new IllegalStateException("Form tokens already set"); //$NON-NLS-1$

		this.forms = forms;
	}

	public CompactProperties getProperties() {
		if(properties==null) {
			properties = new CompactProperties();
		}

		return properties;
	}

	public Object getProperty(String key) {
		switch (key) {
		case LanguageConstants.SIZE_KEY:
			return _int(length());

		case LanguageConstants.INDEX_KEY:
			return _int(getIndex());

		default:
			return properties==null ? null : properties.get(key);
		}
	}

	public void setProperty(String key, Object value) {
		getProperties().put(key, value);
	}

	public void setProperties(CompactProperties properties) {
		this.properties = properties;
	}

	/**
	 * @see de.ims.icarus.ui.text.TextItem#getText()
	 */
	public String getText() {
		return String.join(" ", forms);
	}

	@Override
	public String toString() {
		return getText();
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getIndex()
	 */
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @see de.ims.icarus.language.SentenceData#getForm(int)
	 */
	public String getForm(int index) {
		return forms==null ? null : forms[index];
	}

	private static final Key sharedKey = new Key();

	protected final Object getIndexedProperty(int index, String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		if(indexedProperties==null) {
			return null;
		}

		//FIXME maybe synchronize?

		sharedKey.index = index;
		sharedKey.key = key;

		try {
			return indexedProperties.get(sharedKey);
		} finally {
			sharedKey.index = -1;
			sharedKey.key = null;
		}
	}

	public void setProperty(int index, String key, Object value) {
		Key newKey = new Key(key, index);

		if(indexedProperties==null) {
			indexedProperties = new Object2ObjectOpenHashMap<>();
		}

		indexedProperties.put(newKey, value);
	}

	/**
	 * @see de.ims.icarus.language.coref.DefaultCoreferenceData#getProperty(int, java.lang.String)
	 */
	public Object getProperty(int index, String key) {
		switch (key) {
		case LanguageConstants.INDEX_KEY:
			return _int(index);
			
		case LanguageConstants.FORM_KEY:
			return forms[index];

		case LanguageConstants.SIZE_KEY:
		case LanguageConstants.LENGTH_KEY:
			return _int(getForm(index).length());

		default:
			return getIndexedProperty(index, key);
		}
	}

	public int getHead(int index) {
		Number n = (Number) getProperty(index, LanguageConstants.HEAD_KEY);
		return n==null ? LanguageConstants.DATA_HEAD_ROOT : n.intValue();
	}

	public String getPos(int index) {
		return (String) getProperty(index, LanguageConstants.POS_KEY);
	}

	public String getLemma(int index) {
		return (String) getProperty(index, LanguageConstants.LEMMA_KEY);
	}

	public String getFeatures(int index) {
		return (String) getProperty(index, LanguageConstants.FEATURES_KEY);
	}

	public String getRelation(int index) {
		return (String) getProperty(index, LanguageConstants.DEPREL_KEY);
	}

	public boolean isFlagSet(int index, long flag) {
		Number flags = (Number) getProperty(index, LanguageConstants.FLAGS_KEY);

		return flags!=null && (flags.longValue() & flag) == flag;
	}

	public long getFlags(int index) {
		Number flags = (Number) getProperty(index, LanguageConstants.FLAGS_KEY);
		return flags==null ? 0L : flags.longValue();
	}

	public boolean isEmpty() {
		return length()==0;
	}

	public int length() {
		return forms==null ? 0 : forms.length;
	}

	@Override
	public SentenceData clone() {
		try {
			SentenceData other = (SentenceData) super.clone();
			if(properties!=null) {
				other.properties = properties.clone();
			}
			if(indexedProperties!=null) {
				other.indexedProperties = new Object2ObjectOpenHashMap<>(indexedProperties);
			}
			return other;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
	}

	protected static class Key implements Cloneable {
		public String key;
		public int index;

		public Key() {
			// no-op
		}

		public Key(String key, int index) {
			this.key = key;
			this.index = index;
		}

		public Key(Key source) {
			key = source.key;
			index = source.index;
		}

		@Override
		public Key clone() {
			return new Key(this);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return key.hashCode() * (1+index);
		}
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Key) {
				Key other = (Key) obj;
				return index==other.index && key.equals(other.key);
			}
			return false;
		}
		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Key:"+key+"["+index+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
}
