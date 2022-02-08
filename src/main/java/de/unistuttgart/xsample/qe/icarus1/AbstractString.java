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

import java.util.regex.Pattern;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractString implements CharSequence, Comparable<CharSequence> {

	private int hash;

	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
//		int size = end-start+1;
//
//		char[] ca = new char[size];
//
//		while(size-- > 0) {
//			ca[size] = charAt(start+size);
//		}
//
//		return new String(ca);
		return new SubSequence(this, start, end-start+1);
	}

	protected void resetHash() {
		hash = 0;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int h = hash;
		if(h==0 && length()>0) {
			h = StringUtil.hash(this);
			hash = h;
		}
		return h;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return StringUtil.equals(this, obj);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String(toCharArray());
	}

	/**
	 * @see String#isEmpty()
	 */
    public boolean isEmpty() {
        return length()==0;
    }

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @see String#compareTo(String)
	 */
	@Override
	public int compareTo(CharSequence cs) {
        int len1 = length();
        int len2 = cs.length();
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            char c1 = charAt(k);
            char c2 = cs.charAt(k);
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
	}

	/**
	 * @see String#regionMatches(int, String, int, int)
	 */
    public boolean regionMatches(int toffset, CharSequence other, int ooffset,
            int len) {
    	int size = length();
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
            if (charAt(to++) != other.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

	/**
	 * @see String#regionMatches(boolean, int, String, int, int)
	 */
    public boolean regionMatches(boolean ignoreCase, int toffset,
            CharSequence other, int ooffset, int len) {
    	int size = length();
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
            char c1 = charAt(to++);
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
    public boolean startsWith(CharSequence prefix, int toffset) {
        int to = toffset;
        int po = 0;
        int pc = prefix.length();
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > length() - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (charAt(to++) != prefix.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

    /**
	 * @see String#startsWith(String)
	 */
    public boolean startsWith(CharSequence prefix) {
        return startsWith(prefix, 0);
    }

    /**
	 * @see String#endsWith(String)
	 */
    public boolean endsWith(CharSequence suffix) {
        return startsWith(suffix, length() - suffix.length());
    }

    /**
	 * @see String#indexOf(int)
	 */
    public int indexOf(char ch) {
        return indexOf(ch, 0);
    }

    /**
	 * @see String#indexOf(int, int)
	 */
    public int indexOf(char ch, int fromIndex) {
        final int max = length();
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }

        for (int i = fromIndex; i < max; i++) {
            if (charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * @see String#lastIndexOf(int)
	 */
    public int lastIndexOf(char ch) {
        return lastIndexOf(ch, length() - 1);
    }

    /**
	 * @see String#lastIndexOf(int, int)
	 */
    public int lastIndexOf(char ch, int fromIndex) {
        int i = Math.min(fromIndex, length() - 1);
        for (; i >= 0; i--) {
            if (charAt(i) == ch) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(CharSequence str) {
        return indexOf(str, 0);
    }

    public int indexOf(CharSequence str, int fromIndex) {
        return StringUtil.indexOf(this, 0, length(),
                str, 0, str.length(), fromIndex);
    }

    public int lastIndexOf(CharSequence str) {
        return lastIndexOf(str, length());
    }

    public int lastIndexOf(CharSequence str, int fromIndex) {
        return StringUtil.lastIndexOf(this, 0, length(),
                str, 0, str.length(), fromIndex);
    }

    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }

    public boolean contains(CharSequence s) {
        return indexOf(s) > -1;
    }

    public char[] toCharArray() {
        // Cannot use Arrays.copyOf because of class initialization order issues
    	int size = length();
        char result[] = new char[size];
        while(size-- > 0) {
        	result[size] = charAt(size);
        }
        return result;
    }
}
