/**
 * 
 */
package de.unistuttgart.xsample.mp;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Markus Gärtner
 *
 */
public interface Mapping {
	
	public void load(Reader reader) throws IOException;

	/** Map a source index to 1 or more target indices. The return value is the number of target
	 * indices stored in the {@code buffer} argument. If the return value is {@code -1} the buffer
	 * did not hold enough space to store all the indices.  */
	public int map(long sourceIndex, long[] buffer);
}
