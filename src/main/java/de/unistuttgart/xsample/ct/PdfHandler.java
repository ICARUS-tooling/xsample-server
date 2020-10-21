/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.util.Payload;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus G�rtner
 *
 */
public class PdfHandler implements ExcerptHandler {
	
	private PDDocument document;
	
	private void checkDoc() {
		if(document==null)
			throw new IllegalStateException("No document available");
	}

	@Override
	public void init(Payload input) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		requireNonNull(input);
		if(!XSampleUtils.MIME_PDF.equals(input.contentType()))
			throw new UnsupportedContentTypeException(
					"Not an '"+XSampleUtils.MIME_PDF+"' resource: "+input.contentType());
		
		PDDocument document;
		try(InputStream in = input.inputStream()) {
			document = PDDocument.load(in);
		} catch (InvalidPasswordException e) {
			throw new UnsupportedContentTypeException("Cannot open encrypted PDF file", e);
		}
		
		if(document.getNumberOfPages()==0)
			throw new EmptyResourceException("PDF is empty");
		
		// Only set internal doc if everything is ok
		this.document = document;
	}

	@Override
	public long segments() {
		checkDoc();
		return document.getNumberOfPages();
	}
	
	@Override
	public void close() throws IOException {
		if(document!=null) {
			document.close();
		}
		document = null;
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(de.unistuttgart.xsample.Fragment[], de.unistuttgart.xsample.util.DataOutput)
	 */
	@Override
	public void excerpt(Fragment[] fragments, Payload output) throws IOException {
		requireNonNull(fragments);
		if(fragments.length==0)
			throw new IllegalArgumentException("Empty fragments list");
		
		try(PDDocument newDocument = new PDDocument()) {
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
			
			try(OutputStream out = buffer(output.outputStream())) {
				newDocument.save(out);
			}
		}
	}

}
