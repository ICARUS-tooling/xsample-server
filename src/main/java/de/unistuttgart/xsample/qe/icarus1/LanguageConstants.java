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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class LanguageConstants {
	// flags
	public static final int FLAG_PROJECTIVE = (1 << 0);

	/**
	 * Head value to mark the root node.
	 */
	public static final short DATA_HEAD_ROOT = -1;

	public static final String DATA_ROOT_LABEL = "<root>"; //$NON-NLS-1$

	public static final String DATA_UNDEFINED_LABEL = "?"; //$NON-NLS-1$

	public static final String DATA_GROUP_LABEL = "<*>"; //$NON-NLS-1$

	public static final String DATA_LEFT_LABEL = "<<"; //$NON-NLS-1$

	public static final String DATA_RIGHT_LABEL = ">>"; //$NON-NLS-1$

	public static final int DATA_LEFT_VALUE = -1;

	public static final int DATA_RIGHT_VALUE = 1;

	public static final int DATA_GROUP_VALUE = -3;

	public static final int DATA_UNDEFINED_VALUE = -2;

	public static final float DATA_UNDEFINED_FLOAT_VALUE = Float.NEGATIVE_INFINITY;

	public static final double DATA_UNDEFINED_DOUBLE_VALUE = Double.NEGATIVE_INFINITY;

	public static final int DATA_YES_VALUE = 0;

	public static final int DATA_NO_VALUE = -1;

	// PROPERTY KEYS

	// General properties
	public static final String ID_KEY = "id"; //$NON-NLS-1$
	public static final String SIZE_KEY = "size"; //$NON-NLS-1$
	public static final String LENGTH_KEY = "length"; //$NON-NLS-1$
	public static final String INDEX_KEY = "index"; //$NON-NLS-1$
	public static final String GENDER_KEY = "gender"; //$NON-NLS-1$
	public static final String NUMBER_KEY = "number"; //$NON-NLS-1$


	// Edge Properties
	public static final String DIRECTION_KEY = "dir"; //$NON-NLS-1$
	public static final String DISTANCE_KEY = "dist"; //$NON-NLS-1$
	public static final String TRANSITIVE_KEY = "trans"; //$NON-NLS-1$
	public static final String EDGE_KEY = "edge"; //$NON-NLS-1$
	public static final String ROOT_KEY = "root"; //$NON-NLS-1$

	// Word Properties
	public static final String FORM_KEY = "form"; //$NON-NLS-1$
	public static final String POS_KEY = "pos"; //$NON-NLS-1$
	public static final String LEMMA_KEY = "lemma"; //$NON-NLS-1$
	public static final String FEATURES_KEY = "features"; //$NON-NLS-1$
	public static final String DEPREL_KEY = "deprel"; //$NON-NLS-1$
	public static final String HEAD_KEY = "head"; //$NON-NLS-1$
	public static final String FLAGS_KEY = "flags"; //$NON-NLS-1$
	public static final String SPEAKER_KEY = "speaker"; //$NON-NLS-1$
	public static final String SPEAKER_FEATURES_KEY = "speaker_features"; //$NON-NLS-1$
	public static final String ENTITY_KEY = "entity"; //$NON-NLS-1$
	public static final String TAG_KEY = "tag"; //$NON-NLS-1$
	public static final String PARSE_KEY = "parse"; //$NON-NLS-1$
	public static final String FRAMESET_KEY = "frameset"; //$NON-NLS-1$
	public static final String SENSE_KEY = "sense"; //$NON-NLS-1$
}
