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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.List;

import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.SourceType;
import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
public class PlaintextHandler implements ExcerptHandler {

	private static final long serialVersionUID = 7803808074101364595L;

	@Override
	public SourceType getType() { return SourceType.TXT; }
	
	//TODO currently we're not really unicode aware here

	@Override
	public void analyze(XmpFileInfo file, Charset encoding, InputStream in) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		ReadableByteChannel ch = Channels.newChannel(in);
		Reader reader = Channels.newReader(ch, encoding.newDecoder(), -1);
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[1<<13];
		int read;
		while((read = reader.read(buffer)) > 0) {
			sb.append(buffer, 0, read);
		}
		file.setSegments(sb.length());
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(de.unistuttgart.xsample.dv.XmpFragment[])
	 */
	@Override
	public void excerpt(Charset encoding, InputStream in, List<XmpFragment> fragments, OutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getSegmentLabel(boolean plural) {
		// For now we don't distinguish between pl and sg label here
		return BundleUtil.get("characters"); 
	}
}
