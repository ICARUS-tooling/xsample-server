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
