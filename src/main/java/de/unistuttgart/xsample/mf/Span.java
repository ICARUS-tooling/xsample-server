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
/**
 * 
 */
package de.unistuttgart.xsample.mf;

import static de.unistuttgart.xsample.util.XSampleUtils._long;
import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.checkState;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.unistuttgart.xsample.util.SelfValidating;

public class Span implements Serializable, SelfValidating {

	private static final long serialVersionUID = -5616064267813758508L;
	
	@Expose
	@SerializedName(XsampleManifest.TYPE)
	private final String _type = XsampleManifest.NS+"span";

	@Expose
	@SerializedName(XsampleManifest.NS+"begin")
	@Nullable
	private Long begin;

	@Expose
	@SerializedName(XsampleManifest.NS+"end")
	@Nullable
	private Long end;

	@Expose
	@SerializedName(XsampleManifest.NS+"spanType")
	private SpanType spanType;
	
	public long getBegin() { return begin==null ? -1 : begin.longValue(); }
	public long getEnd() { return end==null ? -1 : end.longValue(); }
	public SpanType getSpanType() { return spanType; }
	
	@Override
	public void validate() {
		checkState("Missing 'span-type' field", spanType!=null);
		checkState("Need at least 1 of 'begin' or 'end' fields", 
				begin!=null || end!=null);
	}

	public static Span.Builder builder() { return new Builder(); }

	public static class Builder extends BuilderBase<Span> {
		
		private Builder() { /* no-op */ }
	
		@Override
		protected Span makeInstance() { return new Span(); }
		
		public Span.Builder begin(long begin) {
			checkArgument("Begin cannot be negative", begin>=0);
			checkState("Begin already set", instance.begin==null);
			instance.begin = _long(begin);
			return this;
		}
		
		public Span.Builder end(long end) {
			checkArgument("End cannot be negative", end>=0);
			checkState("End already set", instance.end==null);
			instance.end = _long(end);
			return this;
		}
		
		public Span.Builder spanType(SpanType spanType) {
			requireNonNull(spanType);
			checkState("Span type already set", instance.spanType==null);
			instance.spanType = spanType;
			return this;
		}
	}		
}