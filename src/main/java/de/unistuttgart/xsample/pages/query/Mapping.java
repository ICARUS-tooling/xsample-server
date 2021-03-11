/**
 * 
 */
package de.unistuttgart.xsample.pages.query;

import java.io.Serializable;

/**
 * Models an laignment between primary segmentation units and some arbitrary
 * sort of underlying segments. 
 * 
 * @author Markus Gärtner
 *
 */
public class Mapping implements Serializable {
	
	private static final long serialVersionUID = 2777158458785145698L;

	private static final long[] EMPTY = {};

	private long[] spans = EMPTY;
	
	public long size() { return spans.length>>>1; }
	
	public long spanContaining(long value) {
		throw new UnsupportedOperationException();
	}
}
