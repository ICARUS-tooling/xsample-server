/**
 * 
 */
package de.unistuttgart.xsample.mp;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.mf.MappingType;

/**
 * @author Markus Gärtner
 *
 */
public class Mappings {
	
	private static final Map<MappingType, Supplier<Mapping>> mappings = new EnumMap<>(MappingType.class);
	static {
		mappings.put(MappingType.TABULAR, TabularMapping::new);
	}

	public static Mapping forMappingType(MappingType type) throws UnsupportedContentTypeException {
		requireNonNull(type);
		Supplier<Mapping> sup = mappings.get(type);
		if(sup==null)
			throw new UnsupportedContentTypeException("Unsupported mapping format: "+type);
		return sup.get();
	}
}
