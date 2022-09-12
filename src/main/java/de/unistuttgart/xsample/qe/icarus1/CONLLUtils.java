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

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static de.unistuttgart.xsample.util.XSampleUtils._long;

import de.unistuttgart.xsample.qe.icarus1.CharTableBuffer.Cursor;
import de.unistuttgart.xsample.qe.icarus1.CharTableBuffer.Row;
  
/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CONLLUtils {


	// column	content
	// 1	id
	// 2	form
	// 3	lemma
	// 4	course-grained pos-tag
	// 5	fine-grained pos-tag
	// 6	feats
	// 7	head
	// 8	deprel
	public static final int ID06 = 0;
	public static final int FORM06 = 1;
	public static final int LEMMA06 = 2;
	public static final int CPOS06 = 3;
	public static final int FPOS06 = 4;
	public static final int FEAT06 = 5;
	public static final int HEAD06 = 6;
	public static final int DEPREL06 = 7;

	public static final int ID09 = 0;
	public static final int FORM09 = 1;
	public static final int LEMMA09 = 2;
	public static final int PLEMMA09 = 3;
	public static final int POS09 = 4;
	public static final int PPOS09 = 5;
	public static final int FEAT09 = 6;
	public static final int PFEAT09 = 7;
	public static final int HEAD09 = 8;
	public static final int PHEAD09 = 9;
	public static final int DEPREL09 = 10;
	public static final int PDEPREL09 = 11;
	public static final int FILLPRED09 = 12;
	public static final int PRED09 = 13;

	// APREDs from index 14 on

	// Number of columns of interest (skip APREDs)
	private final static int COL_LIMIT09 = 12;

	private final static int COL_LIMIT06 = 8;

	private static final Object US = "_"; //$NON-NLS-1$
	private static final String DELIMITER = "\\s+"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	public static SentenceData readGold09(CharTableBuffer buffer, int corpusIndex) {
		if(buffer.isEmpty())
			throw new IllegalArgumentException("No rows to read in buffer"); //$NON-NLS-1$

		int size = buffer.getRowCount();
		
		SentenceData data = new SentenceData();

		short[] heads = new short[size];
		String[] forms = new String[size];
		long[] flags = new long[size];

		int index = -1;

		Row row;
		boolean checkIdForIndex = true;

		for(int i=0; i<size; i++) {

			row = buffer.getRow(i);
			if(row.split(DELIMITER, COL_LIMIT09)!=COL_LIMIT09)
				throw new IllegalArgumentException("Incorrect column count in data file, " //$NON-NLS-1$
						+ "are you sure this is the right format for CoNLL 09?"); //$NON-NLS-1$

			forms[i] = getString(row, FORM09, "<empty>"); //$NON-NLS-1$
			Integer head = getInt(row, HEAD06);
			heads[i] = head.shortValue();
			data.setProperty(i, LanguageConstants.HEAD_KEY, head);
			data.setProperty(i, LanguageConstants.LEMMA_KEY, getString(row, LEMMA09, EMPTY));
			data.setProperty(i, LanguageConstants.FEATURES_KEY, getString(row, FEAT09, EMPTY));
			data.setProperty(i, LanguageConstants.POS_KEY, getString(row, POS09, EMPTY));
			data.setProperty(i, LanguageConstants.DEPREL_KEY, getString(row, DEPREL09, EMPTY));
			
			if(index==-1 && checkIdForIndex) {
				Cursor cursor = row.getSplitCursor(ID09);
				int offset = cursor.indexOf('_');
				if(offset>-1 && offset<cursor.length()) {
					index = StringPrimitives.parseInt(cursor, 0, offset-1)-1;
				} else {
					checkIdForIndex = false;
				}
				cursor.recycle();
			}
		}

		if(index==-1) {
			index = corpusIndex;
		}

		fillProjectivityFlags(heads, flags);
		data.setForms(forms);

		for(int i=0; i<size; i++) {
			data.setProperty(i, LanguageConstants.FLAGS_KEY, _long(flags[i]));
		}

		return data;
	}

	public static SentenceData readPredicted09(CharTableBuffer buffer, int corpusIndex) {
		if(buffer.isEmpty())
			throw new IllegalArgumentException("No rows to read in buffer"); //$NON-NLS-1$

		int size = buffer.getRowCount();
		
		SentenceData data = new SentenceData();

		short[] heads = new short[size];
		String[] forms = new String[size];
		long[] flags = new long[size];

		int index = -1;

		Row row;
		boolean checkIdForIndex = true;

		for(int i=0; i<size; i++) {

			row = buffer.getRow(i);
			if(row.split(DELIMITER, COL_LIMIT09)!=COL_LIMIT09)
				throw new IllegalArgumentException("Incorrect column count in data file, " //$NON-NLS-1$
						+ "are you sure this is the right format for CoNLL 09?"); //$NON-NLS-1$

			forms[i] = getString(row, FORM09, "<empty>"); //$NON-NLS-1$
			Integer head = getInt(row, HEAD06);
			heads[i] = head.shortValue();
			data.setProperty(i, LanguageConstants.HEAD_KEY, head);
			data.setProperty(i, LanguageConstants.LEMMA_KEY, getString(row, PLEMMA09, EMPTY));
			data.setProperty(i, LanguageConstants.FEATURES_KEY, getString(row, PFEAT09, EMPTY));
			data.setProperty(i, LanguageConstants.POS_KEY, getString(row, PPOS09, EMPTY));
			data.setProperty(i, LanguageConstants.DEPREL_KEY, getString(row, PDEPREL09, EMPTY));
			
			if(index==-1 && checkIdForIndex) {
				Cursor cursor = row.getSplitCursor(ID09);
				int offset = cursor.indexOf('_');
				if(offset>-1 && offset<cursor.length()) {
					index = StringPrimitives.parseInt(cursor, 0, offset-1)-1;
				} else {
					checkIdForIndex = false;
				}
				cursor.recycle();
			}
		}

		if(index==-1) {
			index = corpusIndex;
		}

		fillProjectivityFlags(heads, flags);
		data.setForms(forms);

		for(int i=0; i<size; i++) {
			data.setProperty(i, LanguageConstants.FLAGS_KEY, _long(flags[i]));
		}

		return data;
	}

	private static Integer getInt(Row row, int index) {
		Cursor cursor = row.getSplitCursor(index);

		int value = LanguageConstants.DATA_UNDEFINED_VALUE;

		if(!StringUtil.equals(cursor, US) && !cursor.isEmpty()) {
			value = StringPrimitives.parseInt(cursor)-1;
		}

		cursor.recycle();

		return _int(value);
	}

	private static String getString(Row row, int index, String def) {
		Cursor cursor = row.getSplitCursor(index);
		String s = EMPTY;
		if(StringUtil.equals(cursor, US) || cursor.isEmpty()) {
			s = def;
		} else {
			s = cursor.toString();
		}

		cursor.recycle();

		return s;
	}

	/**
	 * Head value to mark the root node.
	 */
	public static final short DATA_HEAD_ROOT = -1;

	/**
	 * Algorithm by
	 * <a href="http://ufal.mff.cuni.cz:8080/pub/files/havelka2005.pdf">Havelka 2005</a>
	 *
	 * Naive approach used for now instead!
	 */
	private static void fillProjectivityFlags(short[] heads, long[] flags) {
		for(int i=0; i<heads.length; i++) {
			flags[i] &= ~LanguageConstants.FLAG_PROJECTIVE;
			if(isProjective(i, heads[i], heads)) {
				flags[i] |= LanguageConstants.FLAG_PROJECTIVE;
			}
		}
	}

	private static boolean isProjective(int dep,int head,short[] heads){
		if(head==DATA_HEAD_ROOT)
			return true;
		int min=dep;
		int max;
		if(head<dep){
			min=head;
			max=dep;
		} else {
			max=head;
		}
		for(int i=min+1;i<max;++i){
			int cur=i;
			while(cur!=dep && cur!=head){
				if(cur==DATA_HEAD_ROOT)
					return false;
				cur=heads[cur];
			}
		}
		return true;
	}
}
