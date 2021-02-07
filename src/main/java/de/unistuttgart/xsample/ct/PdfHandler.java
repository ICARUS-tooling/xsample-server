/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import de.unistuttgart.xsample.InputType;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
public class PdfHandler implements ExcerptHandler {

	@Override
	public InputType getType() { return InputType.PDF; }

	@Override
	public void analyze(FileInfo file, InputStream in) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		requireNonNull(file);
		requireNonNull(in);
		if(!XSampleUtils.MIME_PDF.equals(file.getContentType()))
			throw new UnsupportedContentTypeException(
					"Not an '"+XSampleUtils.MIME_PDF+"' resource: "+file.getContentType());
		
		try(PDDocument document = PDDocument.load(in)) {			
			if(document.getNumberOfPages()==0)
				throw new EmptyResourceException("PDF is empty");
			
			file.setSegments(document.getNumberOfPages());
		} catch (InvalidPasswordException e) {
			throw new UnsupportedContentTypeException("Cannot open encrypted PDF file", e);
		}
	}

	@Override
	public void excerpt(FileInfo file, InputStream in, Fragment[] fragments, OutputStream out) throws IOException {
		requireNonNull(file);
		requireNonNull(in);
		requireNonNull(fragments);
		requireNonNull(out);
		if(fragments.length==0)
			throw new IllegalArgumentException("Empty fragments list");
		
		try(PDDocument document = PDDocument.load(in); PDDocument newDocument = new PDDocument()) {
			PDPageTree source = document.getPages();
			PDPageTree target = newDocument.getPages();
			
			for(Fragment fragment : fragments) {
				int from = strictToInt(fragment.getFrom());
				int to = strictToInt(fragment.getTo());
				for(int idx = from; idx <= to; idx++) {
					PDPage page = source.get(idx);
					target.add(page);
				}
			}
		
			assert target.getCount()>0 : "no pages added to new document";
			
			newDocument.save(out);
		}
	}

	@Override
	public String getSegmentLabel() { return BundleUtil.get("pages"); }
}
