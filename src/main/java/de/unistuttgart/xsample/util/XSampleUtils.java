/**
 * 
 */
package de.unistuttgart.xsample.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Markus Gärtner
 *
 */
public class XSampleUtils {
	
	public static final String MIME_PDF = "application/pdf";
	public static final String MIME_TXT = "text/plain";
	public static final String MIME_EPUB = "application/epub+zip";

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
}
