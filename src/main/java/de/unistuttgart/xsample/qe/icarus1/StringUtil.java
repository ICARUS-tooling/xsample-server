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

import java.awt.Component;
import java.awt.FontMetrics;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class StringUtil {

	public static final String TEXT_WILDCARD = "[...]"; //$NON-NLS-1$

	private StringUtil() {
		// no-op
	}

	// EQUALITY

	public static boolean equals(CharSequence cs, Object obj) {
		if(obj instanceof CharSequence) {
			CharSequence other = (CharSequence) obj;

			if(cs.length()!=other.length()) {
				return false;
			}

			for(int i=cs.length()-1; i>=0; i--) {
				if(cs.charAt(i)!=other.charAt(i)) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	private static final Pattern lineBreak = Pattern.compile("\r\n|\n\r|\n|\r"); //$NON-NLS-1$

	public static String[] splitLines(String s) {
		return s==null ? null : lineBreak.split(s);
	}

	// HASHING
	//
	// both hash functions mirror the default behavior of String.hashCode() so that
	// substitution in hash-tables is possible.
	// If the hash function of String should ever be changed this needs to be addressed!

	public static int hash(CharSequence cs) {

		if(cs==null) {
			return 0;
		}

		int h = 0;

        for (int i = 0; i < cs.length(); i++) {
            h = 31 * h + cs.charAt(i);
        }

        return h;
	}

	public static int hash(char[] c, int offset, int len) {

		int h = 0;

        for (int i = 0; i < len; i++) {
            h = 31 * h + c[offset+i];
        }

        return h;
	}

	// STRING CONVERSION

	public static String toString(CharSequence cs) {
		if(cs instanceof String) {
			return (String) cs;
		}

		char[] tmp = new char[cs.length()];

		for(int i=cs.length()-1; i>=0; i--) {
			tmp[i] = cs.charAt(i);
		}

		return new String(tmp);
	}

	/**
	 * @see String#regionMatches(int, String, int, int)
	 */
    public static boolean regionMatches(CharSequence s, int toffset, CharSequence other, int ooffset,
            int len) {
    	int size = s.length();
    	int sizeo = other.length();
        int to = toffset;
        int po = ooffset;
        // Note: toffset, ooffset, or len might be near -1>>>1.
        if ((ooffset < 0) || (toffset < 0)
                || (toffset > (long)size - len)
                || (ooffset > (long)sizeo - len)) {
            return false;
        }
        while (len-- > 0) {
            if (s.charAt(to++) != other.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

	/**
	 * @see String#regionMatches(boolean, int, String, int, int)
	 */
    public static boolean regionMatches(CharSequence s, boolean ignoreCase, int toffset,
            CharSequence other, int ooffset, int len) {
    	int size = s.length();
    	int sizeo = other.length();
        int to = toffset;
        int po = ooffset;
        // Note: toffset, ooffset, or len might be near -1>>>1.
        if ((ooffset < 0) || (toffset < 0)
                || (toffset > (long)size - len)
                || (ooffset > (long)sizeo - len)) {
            return false;
        }
        while (len-- > 0) {
            char c1 = s.charAt(to++);
            char c2 = other.charAt(po++);
            if (c1 == c2) {
                continue;
            }
            if (ignoreCase) {
                // If characters don't match but case may be ignored,
                // try converting both characters to uppercase.
                // If the results match, then the comparison scan should
                // continue.
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (u1 == u2) {
                    continue;
                }
                // Unfortunately, conversion to uppercase does not work properly
                // for the Georgian alphabet, which has strange rules about case
                // conversion.  So we need to make one last check before
                // exiting.
                if (Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

	/**
	 * @see String#startsWith(String, int)
	 */
    public static boolean startsWith(CharSequence s, CharSequence prefix, int toffset) {
        int to = toffset;
        int po = 0;
        int pc = prefix.length();
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > s.length() - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (s.charAt(to++) != prefix.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

    /**
	 * @see String#startsWith(String)
	 */
    public static boolean startsWith(CharSequence s, CharSequence prefix) {
        return startsWith(s, prefix, 0);
    }

    /**
	 * @see String#endsWith(String)
	 */
    public static boolean endsWith(CharSequence s, CharSequence suffix) {
        return startsWith(s, suffix, s.length() - suffix.length());
    }

    /**
	 * @see String#indexOf(int)
	 */
    public static int indexOf(CharSequence s, char ch) {
        return indexOf(s, ch, 0);
    }

    /**
	 * @see String#indexOf(int, int)
	 */
    public static int indexOf(CharSequence s, char ch, int fromIndex) {
//        final int max = s.length();
//        if (fromIndex < 0) {
//            fromIndex = 0;
//        } else if (fromIndex >= max) {
//            // Note: fromIndex might be near -1>>>1.
//            return -1;
//        }
//
//        for (int i = fromIndex; i < max; i++) {
//            if (s.charAt(i) == ch) {
//                return i;
//            }
//        }
//        return -1;
    	return indexOf(s, ch, fromIndex, s.length()-1);
    }

    /**
	 * @see String#indexOf(int, int)
	 */
    public static int indexOf(CharSequence s, char ch, int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex > toIndex) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        for (int i = fromIndex; i <= toIndex; i++) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * @see String#lastIndexOf(int)
	 */
    public static int lastIndexOf(CharSequence s, char ch) {
        return lastIndexOf(s, ch, s.length() - 1);
    }

    /**
	 * @see String#lastIndexOf(int, int)
	 */
    public static int lastIndexOf(CharSequence s, char ch, int fromIndex) {
        int i = Math.min(fromIndex, s.length() - 1);
        for (; i >= 0; i--) {
            if (s.charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(CharSequence s, CharSequence str) {
        return indexOf(s, str, 0);
    }

    public static int indexOf(CharSequence s, CharSequence str, int fromIndex) {
        return indexOf(s, 0, s.length(),
                str, 0, str.length(), fromIndex);
    }

    public static int lastIndexOf(CharSequence s, CharSequence str) {
        return lastIndexOf(s, str, s.length());
    }

    public static int lastIndexOf(CharSequence s, CharSequence str, int fromIndex) {
        return lastIndexOf(s, 0, s.length(),
                str, 0, str.length(), fromIndex);
    }

    public static int indexOf(CharSequence source, int sourceOffset, int sourceCount,
    		CharSequence target, int targetOffset, int targetCount,
            int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target.charAt(targetOffset);
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source.charAt(i) != first) {
                while (++i <= max && source.charAt(i) != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source.charAt(j)
                        == target.charAt(k); j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    public static int lastIndexOf(CharSequence source, int sourceOffset, int sourceCount,
    		CharSequence target, int targetOffset, int targetCount,
            int fromIndex) {
        /*
         * Check arguments; return immediately where possible. For
         * consistency, don't check for null str.
         */
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        /* Empty string always matches. */
        if (targetCount == 0) {
            return fromIndex;
        }

        int strLastIndex = targetOffset + targetCount - 1;
        char strLastChar = target.charAt(strLastIndex);
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar:
        while (true) {
            while (i >= min && source.charAt(i) != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start) {
                if (source.charAt(j--) != target.charAt(k--)) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start - sourceOffset + 1;
        }
    }

	public static String fit(String s, int maxLength) {
		return fit(s, maxLength, null);
	}

	public static String fit(String s, int maxLength, String wildcard) {
		if(s==null)
			return ""; //$NON-NLS-1$
		if(s.length()<=maxLength)
			return s;
		if(wildcard==null || wildcard.isEmpty()) {
			wildcard = TEXT_WILDCARD;
		}

		int chunkLength = (maxLength-wildcard.length())/2;

		StringBuilder sb = new StringBuilder(maxLength);
		sb.append(s, 0, chunkLength)
		.append(wildcard)
		.append(s, chunkLength+wildcard.length(), maxLength-chunkLength);

		return sb.toString();
	}

	private static DecimalFormat decimalFormat = new DecimalFormat("#,###"); //$NON-NLS-1$

	public static String formatDecimal(Number n) {
		if(n instanceof Float || n instanceof Double) {
			return formatDecimal(n.doubleValue());
		} else {
			return formatDecimal(n.longValue());
		}
	}

	public static String formatDecimal(int value) {
		synchronized (decimalFormat) {
			return decimalFormat.format(value);
		}
	}
	public static String formatDecimal(long value) {
		synchronized (decimalFormat) {
			return decimalFormat.format(value);
		}
	}

	private static DecimalFormat fractionDecimalFormat = new DecimalFormat("#,##0.00"); //$NON-NLS-1$

	public static String formatDecimal(double value) {
		synchronized (fractionDecimalFormat) {
			return fractionDecimalFormat.format(value);
		}
	}
	public static String formatDecimal(float value) {
		synchronized (fractionDecimalFormat) {
			return fractionDecimalFormat.format(value);
		}
	}
	public static String formatShortenedDecimal(double value) {
		synchronized (fractionDecimalFormat) {
			if(value>=1_000_000_000)
				return fractionDecimalFormat.format(value/1_000_000_000)+'G';
			else if(value>=1_000_000)
				return fractionDecimalFormat.format(value/1_000_000)+'M';
			else if(value>=1_000)
				return fractionDecimalFormat.format(value/1_000)+'K';
			else
				return formatDecimal((int) value);
		}
	}

	public static String formatShortenedDecimal(int value) {
		if(value>=1_000_000_000)
			return formatDecimal(value/1_000_000_000)+'G';
		else if(value>=1_000_000)
			return formatDecimal(value/1_000_000)+'M';
		else if(value>=1_000)
			return formatDecimal(value/1_000)+'K';
		else
			return formatDecimal(value);
	}

	public static String formatDuration(long time) {
		if(time<=0)
			return null;

		long s = time/1000;
		long m = s/60;
		long h = m/60;
		long d = h/24;

		s = s%60;
		m = m%60;
		h = h%24;

		StringBuilder sb = new StringBuilder();
		if(d>0) {
			sb.append(' ').append(d).append('D');
		}
		if(h>0) {
			sb.append(' ').append(h).append('H');
		}
		if(m>0) {
			sb.append(' ').append(m).append('M');
		}
		if(s>0) {
			sb.append(' ').append(s).append('S');
		}

		StringUtil.trim(sb);

		return sb.toString();
	}

	public static void trim(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(0))) {
			sb.delete(0, 1);
		}
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(sb.length()-1))) {
			sb.delete(sb.length()-1, sb.length());
		}
	}

	public static void trimLeft(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(0))) {
			sb.delete(0, 1);
		}
	}

	public static void trimRight(StringBuilder sb) {
		while(sb.length()>0 && Character.isWhitespace(sb.charAt(sb.length()-1))) {
			sb.delete(sb.length()-1, sb.length());
		}
	}

	public static final int MIN_WRAP_WIDTH = 50;

	public static String wrap(String s, Component comp, int width) {
		return wrap(s, comp.getFontMetrics(comp.getFont()), width);
	}

	/**
	 * Wraps a given {@code String} so that its lines do not exceed
	 * the specified {@code width} value in length.
	 */
	public static String wrap(String s, FontMetrics fm, int width) {
		if(fm==null)
			throw new NullPointerException("Invalid font metrics"); //$NON-NLS-1$
		//if(width<MIN_WRAP_WIDTH)
		//	throw new IllegalArgumentException("Width must not be less than "+MIN_WRAP_WIDTH+" pixels"); //$NON-NLS-1$ //$NON-NLS-2$

		if(s==null || s.length()==0)
			return s;

		// FIXME if input string contains multiple linebreaks the last one will be omitted

		StringBuilder sb = new StringBuilder();
		StringBuilder line = new StringBuilder();
		StringBuilder block = new StringBuilder();

		int size = s.length();
		int len = 0;
		int blen = 0;
		for(int i=0; i<size; i++) {
			char c = s.charAt(i);
			boolean lb = false;
			if(c=='\r') {
				continue;
			} else if(c=='\n' || isBreakable(c)) {
				lb = c=='\n';
				if(!lb) {
					block.append(c);
					blen += fm.charWidth(c);
				}
				line.append(block);
				block.setLength(0);
				len += blen;
				blen = 0;
			} else {
				block.append(c);
				blen += fm.charWidth(c);
				lb = (len+blen) >= width;
			}

			if(lb && sb.length()>0) {
				sb.append('\n');
			}
			if(i==size-1) {
				line.append(block);
			}
			if(lb || i==size-1) {
				sb.append(line.toString().trim());
				line.setLength(0);
				len = 0;
			}
		}

		return sb.toString();
	}

	public static String[] split(String s, Component comp, int width) {
		return split(s, comp.getFontMetrics(comp.getFont()), width);
	}

	public static String[] split(String s, FontMetrics fm, int width) {
		if(fm==null)
			throw new NullPointerException("Invalid font metrics"); //$NON-NLS-1$
		//if(width<MIN_WRAP_WIDTH)
		//	throw new IllegalArgumentException("Width must not be less than "+MIN_WRAP_WIDTH+" pixels"); //$NON-NLS-1$ //$NON-NLS-2$

		if(s==null || s.length()==0)
			return new String[0];

		List<String> result = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		StringBuilder block = new StringBuilder();

		int size = s.length();
		int len = 0;
		int blen = 0;
		for(int i=0; i<size; i++) {
			char c = s.charAt(i);
			boolean lb = false;
			if(c=='\r') {
				continue;
			} else if(c=='\n' || isBreakable(c)) {
				lb = c=='\n';
				if(!lb) {
					block.append(c);
					blen += fm.charWidth(c);
				}
				line.append(block);
				block.setLength(0);
				len += blen;
				blen = 0;
			} else {
				block.append(c);
				blen += fm.charWidth(c);
				lb = (len+blen) >= width;
			}

			if(i==size-1) {
				line.append(block);
				lb = true;
			}

			if(lb) {
				result.add(line.toString().trim());
				line.setLength(0);
				len = 0;
			}
		}

		return result.toArray(new String[result.size()]);
	}

	private static boolean isBreakable(char c) {
		return Character.isWhitespace(c) || !Character.isLetterOrDigit(c);
	}

	public static String capitalize(String s) {
		if(s==null || s.length()<2)
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$

		return Character.toUpperCase(s.charAt(0))+s.substring(1);
	}

	public static String join(String[] tokens) {
		return join(tokens, ", ", '[', ']'); //$NON-NLS-1$
	}

	public static String join(String[] tokens, String separator, char start, char end) {
		if(tokens==null || tokens.length==0)
			return ""; //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();

		sb.append(start);
		for(int i=0; i<tokens.length; i++) {
			if(i>0) {
				sb.append(separator);
			}
			sb.append(tokens[i]);
		}
		sb.append(end);

		return sb.toString();
	}

	public static String join(String[] tokens, String separator) {
		if(tokens==null || tokens.length==0)
			return ""; //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();

		for(int i=0; i<tokens.length; i++) {
			if(i>0) {
				sb.append(separator);
			}
			sb.append(tokens[i]);
		}

		return sb.toString();
	}

	public static int compareNumberAwareIgnoreCase(String s1, String s2) {
		try {
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);

			return i1-i2;
		} catch(NumberFormatException e) {
			// ignore
		}

		return s1.compareToIgnoreCase(s2);
	}

	public static int compareNumberAware(String s1, String s2) {
		try {
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);

			return i1-i2;
		} catch(NumberFormatException e) {
			// ignore
		}

		return s1.compareToIgnoreCase(s2);
	}

	public static boolean endsWith(CharSequence s, char c) {
		return s.length()>0 && s.charAt(s.length()-1)==c;
	}

	public static String padRight(String s, int n) {
	     return String.format("%1$-" + n + "s", s); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String padLeft(String s, int n) {
	    return String.format("%1$" + n + "s", s); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
