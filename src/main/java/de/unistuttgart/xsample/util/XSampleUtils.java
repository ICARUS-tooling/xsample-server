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
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.qe.Result;

/**
 * @author Markus Gärtner
 *
 */
public class XSampleUtils {
	
	public static final String MIME_PDF = "application/pdf";
	public static final String MIME_TXT = "text/plain";
	public static final String MIME_EPUB = "application/epub+zip";
	
	private static class SizeAccumulator implements Consumer<XmpFragment>, BiConsumer<XmpFragment, XmpFragment> {
		private LongAdder size = new LongAdder();

		@Override
		public void accept(XmpFragment f1, XmpFragment f2) {
			long left = Math.min(f1.getBeginIndex(), f2.getBeginIndex());
			long right = Math.max(f1.getEndIndex(), f2.getEndIndex());
			size.add(right - left + 1);
		}

		@Override
		public void accept(XmpFragment f) {
			size.add(f.size());
		}
		
		public long size() { return size.sum(); }
	}
	
	public static long combinedSize(List<XmpFragment> a1, List<XmpFragment> a2) {
		final SizeAccumulator acc = new SizeAccumulator();
		merge(a1, a2, acc, acc);
		return acc.size();
	}
	
	public static List<XmpFragment> intersect(List<XmpFragment> a1, List<XmpFragment> a2) {
		requireNonNull(a1);
		requireNonNull(a2);
		
		List<XmpFragment> result = new ArrayList<>();
		

		int i1 = 0;
		int i2 = 0; 
		
		for (; i1 < a1.size() && i2 < a2.size(); ) {
			XmpFragment f1 = a1.get(i1);
			XmpFragment f2 = a2.get(i2);
			
			if(f1.getBeginIndex() > f2.getEndIndex()) { // no overlap, f1 > f2
				i2++;
			} else if(f2.getBeginIndex() > f1.getEndIndex()) { // no overlap, f2 > f1
				i1++;
			} else if(f1.contains(f2)) { // f2 completely contained in f1
				result.add(f2);
				i2++;
			} else if(f2.contains(f1)) { // f1 completely contained in f2
				result.add(f1);
				i1++;
			} else { // true overlap, need to create new fragment
				long left = Math.max(f1.getBeginIndex(), f2.getBeginIndex());
				long right = Math.min(f1.getEndIndex(), f2.getEndIndex());
				result.add(XmpFragment.of(left, right));
				if(f1.getEndIndex() >= f2.getEndIndex()) {
					i2++;
				} 
				if(f2.getEndIndex() >= f1.getEndIndex()) {
					i1++;
				}
			}
		}
		
		return result;
	}
	
	public static void merge(List<XmpFragment> a1, List<XmpFragment> a2, 
			Consumer<? super XmpFragment> distinct,
			BiConsumer<? super XmpFragment, ? super XmpFragment> overlap) {
		requireNonNull(a1);
		requireNonNull(a2);
		requireNonNull(distinct);
		requireNonNull(overlap);
		
		int i1 = 0;
		int i2 = 0; 
		for (; i1 < a1.size() && i2 < a2.size(); ) {
			XmpFragment f1 = a1.get(i1);
			XmpFragment f2 = a2.get(i2);
			
			if(f1.getBeginIndex() > f2.getEndIndex()) { // no overlap, f1 > f2
				distinct.accept(f2);
				i2++;
			} else if(f2.getBeginIndex() > f1.getEndIndex()) { // no overlap, f2 > f1
				distinct.accept(f1);
				i1++;
			} else { // overlap
				overlap.accept(f1, f2);
				i1++;
				i2++;
			}
		}
		
		// Handle leftovers from first array
		for (; i1 < a1.size(); i1++) {
			distinct.accept(a1.get(i1));
		}
		// Handle leftovers from second array
		for (; i2 < a2.size(); i2++) {
			distinct.accept(a2.get(i2));
		}
	}
	
	public static List<XmpFragment> asFragments(Result result) {
		requireNonNull(result);
		checkArgument("Result is empty", !result.isEmpty());
		List<XmpFragment> fragments = new ArrayList<>();
		long[] hits = result.getHits();
		XmpFragment current = XmpFragment.of(hits[0]);
		
		for (int i = 1; i < hits.length; i++) {
			long value = hits[i];
			if(!current.append(value)) {
				fragments.add(current);
				current = XmpFragment.of(value);
			}
		}
		
		fragments.add(current);
		
		return fragments;
	}

	private static final IvParameterSpec iv = new IvParameterSpec(new SecureRandom().generateSeed(16));
	
	public static Cipher encrypt(SecretKey key) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		return cipher;
	}
	
	public static Cipher decrypt(SecretKey key) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		return cipher;
	}
	
	public static SecretKey makeKey() throws NoSuchAlgorithmException {
		return KeyGenerator.getInstance("AES").generateKey();
	}
	
	public static SecretKey deserializeKey(String s) {
		// decode the base64 encoded string
		byte[] decodedKey = Base64.getDecoder().decode(s);
		// rebuild key using SecretKeySpec
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"); 
	}
	
	public static String serializeKey(SecretKey key) {
		// get base64 encoded version of the key
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
 
	private static final int KB = 1024;
	private static final int MB = KB * KB;
	private static final int GB = KB * KB * KB;
	
	public static String formatSize(long size) {
		if(size>GB) {
			return (size/GB)+"GB";
		} else if(size>MB) {
			return (size/MB)+"MB";
		} else if(size>KB) {
			return (size/KB)+"KB";
		}
		
		return size+"B";
	}

	public static String format(String text, Object...params) {
		requireNonNull(text);
		if(text.indexOf('{')==-1) {
			return text;
		}

		final StringBuilder result = new StringBuilder();
		final StringBuilder index = new StringBuilder();
		boolean isArg = false;

		int paramsIndex = 0;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (c == '{') {
				index.setLength(0);
				isArg = true;
			} else if (isArg && c == '}') {

				int tmp = paramsIndex;

				if(index.length()>0) {
					tmp = Integer.parseInt(index.toString());
					index.setLength(0);
				} else {
					paramsIndex++;
				}

				if (tmp >= 0 && params!=null && tmp < params.length) {
					result.append(params[tmp]);
				}
				isArg = false;
			} else if (isArg) {
				index.append(c);
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	public static int unbox(Integer value) {
		return value==null ? 0 : value.intValue();
	}

	public static long unbox(Long value) {
		return value==null ? 0L : value.intValue();
	}

	public static double unbox(Double value) {
		return value==null ? 0D : value.doubleValue();
	}

	public static float unbox(Float value) {
		return value==null ? 0F : value.floatValue();
	}

	public static short unbox(Short value) {
		return value==null ? 0 : value.shortValue();
	}

	public static byte unbox(Byte value) {
		return value==null ? 0 : value.byteValue();
	}

	public static boolean unbox(Boolean value) {
		return value==null ? false : value.booleanValue();
	}

	public static char unbox(Character value) {
		return value==null ? 0 : value.charValue();
	}

	public static Integer _int(int value) {
		return Integer.valueOf(value);
	}

	public static Long _long(long value) {
		return Long.valueOf(value);
	}

	public static Double _double(double value) {
		return Double.valueOf(value);
	}

	public static Float _float(float value) {
		return Float.valueOf(value);
	}

	public static Short _short(short value) {
		return Short.valueOf(value);
	}

	public static Byte _byte(byte value) {
		return Byte.valueOf(value);
	}

	public static Boolean _boolean(boolean value) {
		return Boolean.valueOf(value);
	}

	public static Character _char(char value) {
		return Character.valueOf(value);
	}

	private static RuntimeException forOverflow(String type, long value) {
		return new IllegalArgumentException(String.format("Given value is overflowing %s space: %d", type, _long(value)));
	}

	public static byte strictToByte(short v) {
		if(v<Byte.MIN_VALUE || v>Byte.MAX_VALUE)
			throw forOverflow("byte", v);
		return (byte)v;
	}

	public static byte strictToByte(int v) {
		if(v<Byte.MIN_VALUE || v>Byte.MAX_VALUE)
			throw forOverflow("byte", v);
		return (byte)v;
	}

	public static byte strictToByte(long v) {
		if(v<Byte.MIN_VALUE || v>Byte.MAX_VALUE)
			throw forOverflow("byte", v);
		return (byte)v;
	}

	public static short strictToShort(int v) {
		if(v<Short.MIN_VALUE || v>Short.MAX_VALUE)
			throw forOverflow("short", v);
		return (short)v;
	}

	public static short strictToShort(long v) {
		if(v<Short.MIN_VALUE || v>Short.MAX_VALUE)
			throw forOverflow("short", v);
		return (short)v;
	}

	public static char strictToChar(int v) {
		if(v<Character.MIN_VALUE || v>Character.MAX_VALUE)
			throw forOverflow("char", v);
		return (char)v;
	}

	public static char strictToChar(long v) {
		if(v<Character.MIN_VALUE || v>Character.MAX_VALUE)
			throw forOverflow("char", v);
		return (char)v;
	}

	public static int strictToInt(long v) {
		if(v<Integer.MIN_VALUE || v>Integer.MAX_VALUE)
			throw forOverflow("int", v);
		return (int)v;
	}
	
	public static BufferedOutputStream buffer(OutputStream out) {
		if(out instanceof BufferedOutputStream) {
			return (BufferedOutputStream) out;
		}
		return new BufferedOutputStream(out);
	}
	
	public static BufferedInputStream buffer(InputStream in) {
		if(in instanceof BufferedInputStream) {
			return (BufferedInputStream) in;
		}
		return new BufferedInputStream(in);
	}
	
	public static BufferedReader buffer(Reader reader) {
		if(reader instanceof BufferedReader) {
			return (BufferedReader) reader;
		}
		return new BufferedReader(reader);
	}
	
	
	public static boolean isNullOrEmpty(String s) { return s==null || "".equals(s); }


	public static void checkState(boolean condition) {
		if(!condition)
			throw new IllegalStateException();
	}

	public static void checkState(String msg, boolean condition) {
		if(!condition)
			throw new IllegalStateException(msg);
	}

	public static void checkArgument(boolean condition) {
		if(!condition)
			throw new IllegalArgumentException();
	}

	public static void checkArgument(String msg, boolean condition) {
		if(!condition)
			throw new IllegalArgumentException(msg);
	}

	public static void checkIndex(int index, int min, int max) {
		if(index<min || (index!=min && index>max))
			throw new IndexOutOfBoundsException();
	}

	public static <E> E[] checkNotEmpty(E[] array) {
		requireNonNull(array);
		checkArgument("Array must not be empty", array.length>0);
		return array;
	}

	public static String checkNotEmpty(String s) {
		requireNonNull(s);
		if(s.isEmpty())
			throw new IllegalArgumentException("String must not be empty");
		return s;
	}

	public static String checkNullOrNotEmpty(String s) {
		if(s!=null && s.isEmpty())
			throw new IllegalArgumentException("String must be null or not be empty");
		return s;
	}
}
