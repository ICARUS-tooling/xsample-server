/**
 * 
 */
package de.unistuttgart.xsample.ct;

import static java.util.Objects.requireNonNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import de.unistuttgart.xsample.mf.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class AnnotationHandlers {

	private static final Map<ManifestType, Supplier<AnnotationHandler>> handlers = new EnumMap<>(ManifestType.class);
	static {
		handlers.put(ManifestType.ICARUS_LEGACY, CoNLL09Handler::new);
	}

	public static AnnotationHandler forManifestType(ManifestType type) throws UnsupportedManifestTypeException {
		requireNonNull(type);
		Supplier<AnnotationHandler> sup = handlers.get(type);
		if(sup==null)
			throw new UnsupportedManifestTypeException("Unsupported manifest typet: "+type);
		return sup.get();
	}
}
