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
package de.unistuttgart.xsample.qe.icarus1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Location {

	public URL getURL();

	public boolean isLocal();

	public Path getLocalPath();

	public OutputStream openOutputStream() throws IOException;

	public InputStream openInputStream() throws IOException;
	
	public static abstract class Base implements Location {

		@Override
		public URL getURL() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isLocal() { return true; }

		@Override
		public Path getLocalPath() {
			throw new UnsupportedOperationException();
		}

		@Override
		public OutputStream openOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream openInputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
		
	}
}
