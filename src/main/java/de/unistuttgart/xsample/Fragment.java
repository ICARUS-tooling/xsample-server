/**
 * 
 */
package de.unistuttgart.xsample;

import java.util.stream.LongStream;

/**
 * @author Markus Gärtner
 *
 */
public class Fragment {
	
	public static Fragment of(long from, long to) {
		Fragment f = new Fragment();
		f.setFrom(from);
		f.setTo(to);
		return f;
	}
	
	public static Fragment of(long value) {
		return of(value, value);
	}
	
	private long from;
	private long to;
	
	public long getFrom() { return from; }
	public void setFrom(long from) { this.from = from; }
	public long getTo() { return to; }
	public void setTo(long to) { this.to = to; }
	
	public LongStream stream() { return LongStream.rangeClosed(from, to); }
}
