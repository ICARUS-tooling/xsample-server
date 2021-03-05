/**
 * 
 */
package de.unistuttgart.xsample.util;

import java.util.List;

/**
 * @author Markus Gärtner
 *
 */
public interface SelfValidating {

	/**
	 * Verifies integrity of this object.
	 * @throws IllegalStateException iff any check fails
	 */
	void validate();
	
	public static void validateNested(List<? extends SelfValidating> items, String name) {
		if(items==null) {
			throw new IllegalStateException("Missing '"+name+"' field");
		} else if(items.isEmpty()) {
			throw new IllegalStateException("Field '"+name+"' is empty");			
		}
		items.forEach(SelfValidating::validate);
	}
	
	public static void validateNested(SelfValidating item, String name) {
		if(item==null)
			throw new IllegalStateException("Missing '"+name+"' field");
		
		item.validate();
	}
	
	public static void validateOptionalNested(List<? extends SelfValidating> items) {
		if(items!=null) {
			items.forEach(SelfValidating::validate);	
		}
	}
	
	public static void validateOptionalNested(SelfValidating item) {
		if(item!=null) {
			item.validate();
		}
	}
}
