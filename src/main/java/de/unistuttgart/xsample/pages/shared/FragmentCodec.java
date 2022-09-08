/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import de.unistuttgart.xsample.dv.XmpFragment;

/**
 * @author Markus Gärtner
 *
 */
public class FragmentCodec {
	
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
