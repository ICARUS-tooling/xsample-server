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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LanguageUtils  {

	private LanguageUtils() {
		// no-op
	}

	public static String combine(SentenceData data) {
		StringBuilder sb = new StringBuilder(data.length()*4);
		for(int i=0; i<data.length(); i++) {
			if(i>0) {
				sb.append(" "); //$NON-NLS-1$
			}
			sb.append(data.getForm(i));
		}

		return sb.toString();
	}

	public static String[] getForms(SentenceData data) {
		String[] result = new String[data.length()];

		for(int i=0; i<data.length(); i++) {
			result[i] = data.getForm(i);
		}

		return result;
	}

	public static boolean isRoot(int value) {
		return value==LanguageConstants.DATA_HEAD_ROOT;
	}

	public static boolean isRoot(String value) {
		return LanguageConstants.DATA_ROOT_LABEL.equals(value);
	}

	public static boolean isUndefined(int value) {
		return value==LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	public static boolean isUndefined(Object value) {
		return value==null || "".equals(value) //$NON-NLS-1$
				|| value.equals(LanguageConstants.DATA_UNDEFINED_LABEL)
				|| value.equals(LanguageConstants.DATA_UNDEFINED_VALUE)
				|| value.equals(LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE)
				|| value.equals(LanguageConstants.DATA_UNDEFINED_FLOAT_VALUE);
	}

	public static boolean isUndefined(String value) {
		return value==null || value.isEmpty()
				|| value.equals(LanguageConstants.DATA_UNDEFINED_LABEL);
	}

	public static String getBooleanLabel(int value) {
		switch (value) {
		case LanguageConstants.DATA_GROUP_VALUE:
			return LanguageConstants.DATA_GROUP_LABEL;
		case LanguageConstants.DATA_UNDEFINED_VALUE:
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		case LanguageConstants.DATA_YES_VALUE:
			return String.valueOf(true);
		case LanguageConstants.DATA_NO_VALUE:
			return String.valueOf(false);
		}

		throw new IllegalArgumentException("Unknown value: "+value); //$NON-NLS-1$
	}

	public static int parseBooleanLabel(String label) {
		if(LanguageConstants.DATA_GROUP_LABEL.equals(label))
			return LanguageConstants.DATA_GROUP_VALUE;
		else if(LanguageConstants.DATA_UNDEFINED_LABEL.equals(label))
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		else if(Boolean.parseBoolean(label))
			return LanguageConstants.DATA_YES_VALUE;
		else
			return LanguageConstants.DATA_NO_VALUE;
	}

	public static int getBooleanValue(boolean value) {
		return value ? LanguageConstants.DATA_YES_VALUE : LanguageConstants.DATA_NO_VALUE;
	}

	public static String getHeadLabel(int head) {
		switch (head) {
		case LanguageConstants.DATA_HEAD_ROOT:
			return LanguageConstants.DATA_ROOT_LABEL;
		case LanguageConstants.DATA_UNDEFINED_VALUE:
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		case LanguageConstants.DATA_GROUP_VALUE:
			return LanguageConstants.DATA_GROUP_LABEL;
		default:
			return String.valueOf(head + 1);
		}
	}

	public static String getLabel(int value) {
		switch (value) {
		case LanguageConstants.DATA_UNDEFINED_VALUE:
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		case LanguageConstants.DATA_GROUP_VALUE:
			return LanguageConstants.DATA_GROUP_LABEL;
		default:
			return String.valueOf(value);
		}
	}

	public static String getLabel(float value) {
		if(value==LanguageConstants.DATA_UNDEFINED_FLOAT_VALUE) {
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		} else {
			return String.valueOf(value);
		}
	}

	public static String getLabel(double value) {
		if(value==LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE) {
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		} else {
			return String.valueOf(value);
		}
	}

	public static String getDirectionLabel(int value) {
		switch (value) {
		case LanguageConstants.DATA_UNDEFINED_VALUE:
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		case LanguageConstants.DATA_GROUP_VALUE:
			return LanguageConstants.DATA_GROUP_LABEL;
		case LanguageConstants.DATA_LEFT_VALUE:
			return LanguageConstants.DATA_LEFT_LABEL;
		case LanguageConstants.DATA_RIGHT_VALUE:
			return LanguageConstants.DATA_RIGHT_LABEL;
		}

		return null;
	}

	public static int parseHeadLabel(String head) {
		head = head.trim();
		if (LanguageConstants.DATA_ROOT_LABEL.equals(head))
			return LanguageConstants.DATA_HEAD_ROOT;
		else if (LanguageConstants.DATA_UNDEFINED_LABEL.equals(head))
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		else if (LanguageConstants.DATA_GROUP_LABEL.equals(head))
			return LanguageConstants.DATA_GROUP_VALUE;
		else
			return Integer.parseInt(head) - 1;
	}

	public static int parseIntegerLabel(String value) {
		value = value.trim();
		if (value.isEmpty() || LanguageConstants.DATA_UNDEFINED_LABEL.equals(value))
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		else if (LanguageConstants.DATA_GROUP_LABEL.equals(value))
			return LanguageConstants.DATA_GROUP_VALUE;
		else
			return Integer.parseInt(value);
	}

	public static float parseFloatLabel(String value) {
		value = value.trim();
		if (value.isEmpty() || LanguageConstants.DATA_UNDEFINED_LABEL.equals(value))
			return LanguageConstants.DATA_UNDEFINED_FLOAT_VALUE;
		else if (LanguageConstants.DATA_GROUP_LABEL.equals(value))
			return LanguageConstants.DATA_GROUP_VALUE;
		else
			return Float.parseFloat(value);
	}

	public static double parseDoubleLabel(String value) {
		value = value.trim();
		if (value.isEmpty() || LanguageConstants.DATA_UNDEFINED_LABEL.equals(value))
			return LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE;
		else if (LanguageConstants.DATA_GROUP_LABEL.equals(value))
			return LanguageConstants.DATA_GROUP_VALUE;
		else
			return Double.parseDouble(value);
	}

	public static int parseDirectionLabel(String direction) {
		direction = direction.trim();
		if (LanguageConstants.DATA_GROUP_LABEL.equals(direction))
			return LanguageConstants.DATA_GROUP_VALUE;
		else if (LanguageConstants.DATA_LEFT_LABEL.equals(direction) || "left".equals(direction)) //$NON-NLS-1$
			return LanguageConstants.DATA_LEFT_VALUE;
		else if (LanguageConstants.DATA_RIGHT_LABEL.equals(direction) || "right".equals(direction)) //$NON-NLS-1$
			return LanguageConstants.DATA_RIGHT_VALUE;
		else
			return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	public static String normalizeLabel(String value) {
		if(value==null)
			return LanguageConstants.DATA_UNDEFINED_LABEL;

		value = value.trim();
		if (value.isEmpty())
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		else
			return value;
	}

	public static boolean isProjectiveSentence(short[] heads){
		for(int i=0;i<heads.length;++i){
			if(!isProjective(i, heads[i], heads))
				return false;
		}
		return true;
	}

	public static boolean isProjective(int index,short heads[]){
		return isProjective(index,heads[index],heads);
	}

	public static boolean isProjective(int dep,int head,short[] heads){
		if(head==LanguageConstants.DATA_HEAD_ROOT)
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
				if(cur==LanguageConstants.DATA_HEAD_ROOT)
					return false;
				cur=heads[cur];
			}
		}
		return true;
	}
	
	public static final SentenceData dummySentenceData = new SentenceData("This", "is", "a", "test"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
}
