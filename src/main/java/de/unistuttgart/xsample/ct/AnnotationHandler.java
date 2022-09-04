/**
 * 
 */
package de.unistuttgart.xsample.ct;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mp.Mapping;

/**
 * @author Markus Gärtner
 *
 */
public interface AnnotationHandler extends Serializable {

	void excerpt(Reader annotationReader, @Nullable Mapping mapping, List<XmpFragment> fragments, OutputStream out) throws IOException;
}
