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
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import de.unistuttgart.xsample.dv.XmpFragment;

/**
 * @author Markus Gärtner
 *
 */
public class FragmentCodec implements LongConsumer {
	
	private static final char SEP = ',';
	private static final String SEP_STRING = String.valueOf(SEP);

	public static String encode(XmpFragment f) {
		if(f.size()==1) {
			return String.valueOf(f.getBeginIndex());
		} 
		
		return String.valueOf(f.getBeginIndex())+"-"+String.valueOf(f.getEndIndex());
	}

	public static String encode(XmpFragment f, long offset) {
		if(f.size()==1) {
			return String.valueOf(f.getBeginIndex()+offset);
		} 
		
		return String.valueOf(f.getBeginIndex()+offset)+"-"+String.valueOf(f.getEndIndex()+offset);
	}

//	public static String encodeEntries(Stream<ExcerptEntry> entries, ToLongFunction<String> offsets) {
//		StringBuilder sb = new StringBuilder(100);
//		LongValue offset = new LongValue();
//		entries.forEachOrdered( entry -> {
//			List<XmpFragment> fragments = entry.getFragments();
//			if(fragments!=null && !fragments.isEmpty()) {
//				for (int i = 0; i < fragments.size(); i++) {
//					if(sb.length()>0) {
//						sb.append(SEP);
//					}
//					sb.append(encode(fragments.get(i), offset.value));
//				}
//			}
//			offset.value += offsets.applyAsLong(entry.getCorpusId());
//		});
//		return sb.toString();
//	}

//	public static String encodeQuotas(Stream<String> parts,
//			Function<String,List<XmpFragment>>ToLongFunction<String> offsets) {
//		StringBuilder sb = new StringBuilder(100);
//		LongValue offset = new LongValue();
//		entries.forEachOrdered( entry -> {
//			List<XmpFragment> fragments = Optional.ofNullable(entry.getQuota())
//					.map(XmpExcerpt::getFragments)
//					.orElse(null);
//			if(fragments!=null && !fragments.isEmpty()) {
//				for (int i = 0; i < fragments.size(); i++) {
//					if(sb.length()>0) {
//						sb.append(SEP);
//					}
//					sb.append(encode(fragments.get(i), offset.value));
//				}
//			}
//			offset.value += offsets.applyAsLong(entry.getCorpusId());
//		});
//		return sb.toString();
//	}

	public static String encodeAll(List<XmpFragment> fragments) {
		if(fragments==null || fragments.isEmpty()) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(fragments.size()*4);
		for (int i = 0; i < fragments.size(); i++) {
			if(i>0) {
				sb.append(SEP);
			}
			sb.append(encode(fragments.get(i)));
		}
		return sb.toString();
	}

	public static String encodeAll(Stream<XmpFragment> fragments) {
		StringBuilder sb = new StringBuilder(100);
		fragments.forEachOrdered(f -> {
			if(sb.length()>0) {
				sb.append(SEP);
			}
			sb.append(encode(f));
		});
		return sb.toString();
	}

	public static String encodeAll(long[] fragments) {
		StringBuilder sb = new StringBuilder(fragments.length*4);
		for (int i = 0; i < fragments.length; i++) {
			if(i>0) {
				sb.append(SEP);
			}
			sb.append(String.valueOf(fragments[i]));
		}
		return sb.toString();
	}

	public static String encodeAll(LongStream fragments) {
		StringBuilder sb = new StringBuilder(100);
		fragments.forEach(fragment -> {
			if(sb.length()>0) {
				sb.append(SEP);
			}
			sb.append(String.valueOf(fragment));
		});
		return sb.toString();
	}

	public static XmpFragment decode(String s) {
		requireNonNull(s);
		XmpFragment f;
		int sep = s.indexOf('-');
		if(sep!=-1) {
			f = XmpFragment.of(Long.parseUnsignedLong(s.substring(0, sep)), 
					Long.parseUnsignedLong(s.substring(sep+1)));
		} else {
			f = XmpFragment.of(Long.parseUnsignedLong(s));
		}
		return f;
	}

	public static List<XmpFragment> decodeAll(String s) {
		return Stream.of(s.split(SEP_STRING))
				.map(FragmentCodec::decode)
				.collect(Collectors.toList());
	}

//	private static final class LongValue {
//		private long value = 0;
//	}

	private final StringBuilder buffer = new StringBuilder();
	
	private void maybeAppendSep() {
		if(buffer.length()>0) {
			buffer.append(SEP);
		}
	}
	
	@Override
	public void accept(long value) {
		append(value);
	}
	
	public FragmentCodec append(long value) {
		maybeAppendSep();
		buffer.append(String.valueOf(value));
		return this;
	}
	
	public FragmentCodec append(long[] values) {
		for(long value : values) {
			append(value);
		}
		return this;
	}
	
	public FragmentCodec append(long[] values, long offset) {
		for(long value : values) {
			append(value + offset);
		}
		return this;
	}
	
	public FragmentCodec append(LongStream values) {
		values.forEach(this);
		return this;
	}
	
	public FragmentCodec append(XmpFragment fragment) {
		maybeAppendSep();
		buffer.append(encode(fragment));
		return this;
	}
	
	public FragmentCodec append(List<XmpFragment> fragments) {
		if(fragments!=null && !fragments.isEmpty()) {
			for(XmpFragment fragment : fragments) {
				append(fragment);
			}
		}
		return this;
	}
	
	public FragmentCodec append(XmpFragment fragment, long offset) {
		maybeAppendSep();
		buffer.append(encode(fragment, offset));
		return this;
	}
	
	public FragmentCodec append(List<XmpFragment> fragments, long offset) {
		if(fragments!=null && !fragments.isEmpty()) {
			for(XmpFragment fragment : fragments) {
				append(fragment, offset);
			}
		}
		return this;
	}
	
	@Override
	public String toString() {
		return buffer.toString();
	}
}
