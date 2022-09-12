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

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mf.SourceType;
import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
public class PdfHandler implements ExcerptHandler {

	private static final long serialVersionUID = -6009524518534487163L;

	@Override
	public SourceType getType() { return SourceType.PDF; }

	@Override
	public void analyze(XmpFileInfo file, Charset encoding, InputStream in) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		requireNonNull(file);
		requireNonNull(encoding);
		requireNonNull(in);
		
		try(PDDocument document = PDDocument.load(in)) {			
			if(document.getNumberOfPages()==0)
				throw new EmptyResourceException("PDF is empty");
			
			file.setSegments(document.getNumberOfPages());
		} catch (InvalidPasswordException e) {
			throw new UnsupportedContentTypeException("Cannot open encrypted PDF file", e);
		}
	}

	@Override
	public void excerpt(Charset encoding, InputStream in, List<XmpFragment> fragments, OutputStream out) throws IOException {
		requireNonNull(encoding);
		requireNonNull(in);
		requireNonNull(fragments);
		requireNonNull(out);
		checkArgument("Empty fragments list", !fragments.isEmpty());
		
		try(PDDocument document = PDDocument.load(in); PDDocument newDocument = new PDDocument()) {
			PDPageTree source = document.getPages();
			PDPageTree target = newDocument.getPages();
			
			for(XmpFragment fragment : fragments) {
				int from = strictToInt(fragment.getBeginIndex());
				int to = strictToInt(fragment.getEndIndex());
				for(int idx = from; idx <= to; idx++) {
					PDPage page = source.get(idx-1);
					target.add(page);
				}
			}
		
			assert target.getCount()>0 : "no pages added to new document";
			
			newDocument.save(out);
		}
	}

	@Override
	public String getSegmentLabel(boolean plural) { 
		return BundleUtil.get(plural ? "pages" : "page"); 
	}
}
