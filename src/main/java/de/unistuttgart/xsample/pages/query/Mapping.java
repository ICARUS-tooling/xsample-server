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
/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Models an alignment between primary segmentation units and some arbitrary
 * sort of underlying segments.
 * <p>
 * <i>Source</i> in the documentation or parameter names of this class refers
 * to the native segmentation of the annotation data that is used for mapping purposes.
 * On the other hand <i>target</i> refers to the basic segmentation of the corpus'
 * primary data, such as "pages" for PDF resources.
 * <p>
 * Currently this class only models a one-way mapping from source to target
 * indices with no efficient mechanism for reverse lookups.
 * 
 * @author Markus Gärtner
 *
 */
public class Mapping implements Serializable {
	
	private static final long serialVersionUID = 2777158458785145698L;

	private final long[] spans;
	private final long sourceOffset, targetOffset;
	
	private Mapping(Builder builder) { 
		spans = builder.getSpans();
		sourceOffset = builder.getSourceOffset();
		targetOffset = builder.getTargetOffset();
	}

	public long size() { return spans.length>>>1; }
	
	/** Translates raw source index into ready to use array index of the spans buffer */
	private int translateSource(long sourceIndex) {
		int translatedSourceIndex = strictToInt(sourceIndex - sourceOffset);
		return translatedSourceIndex<<1;
	}
	
	public long targetBegin(long sourceIndex) { return targetOffset + spans[translateSource(sourceIndex)]; }
	public long targetEnd(long sourceIndex) { return targetOffset + spans[translateSource(sourceIndex)+1]; }
	
	public static Builder builder() { return new Builder(); }
	
	public static class Builder {
		
		private long[] spans;
		private int size = 0;
		private long sourceOffset = 0;
		private long targetOffset = 0;
		
		private Builder() {
			spans = new long[1000];
			Arrays.fill(spans, -1);
		}
		
		public Builder sourceOffset(long sourceOffset) {
			checkArgument("Source offset must not be negative", sourceOffset>=0);
			this.sourceOffset = sourceOffset;
			return this;
		}
		
		public Builder targetOffset(long targetOffset) {
			checkArgument("Target offset must not be negative", targetOffset>=0);
			this.targetOffset = targetOffset;
			return this;
		}
		
		/**
		 * Adds a new mapping to this builder.
		 * Computes and returns the source index that was used for the mapping.
		 * Note that the returned source index is <b>not</b> modified by the
		 * (potentially not yet) set {@link #sourceOffset(long) source offset}!
		 * Target offsets will be adjusted by the current {@link #targetOffset(long) target offset}.
		 */
		public void addMapping(long sourceIndex, long targetBegin, long tragetEnd) {
			int translatedSourceIndex = strictToInt(sourceIndex - sourceOffset);
			
			int index = translatedSourceIndex<<1;
			if(spans.length<index+1) {
				int oldSize = spans.length;
				int newSize = oldSize<<1;
				if(newSize<0) {
					//TODO need to check again exact max allowed array size
					newSize = Integer.MAX_VALUE-8;
				}
				spans = Arrays.copyOf(spans, newSize);
				Arrays.fill(spans, oldSize, newSize, -1);
			}
			spans[index] = targetBegin - targetOffset;
			spans[index+1] = tragetEnd - targetOffset;
		}
		
		private long getSourceOffset() { return sourceOffset; }
		private long getTargetOffset() { return targetOffset; }	
		
		private long[] getSpans() {
			if(size==spans.length>>>1) {
				return spans;
			}
			return Arrays.copyOf(spans, size<<1);
		}
		
		public Mapping build() { return new Mapping(this); }
	}
}
