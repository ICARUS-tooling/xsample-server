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

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
class PdfHandlerTest implements ExcerptHandlerTest<PdfHandler> {

	@Override
	public PdfHandler create() { return new PdfHandler(); }

	@Override
	public String[] supportedContentTypes() { return new String[]{ XSampleUtils.MIME_PDF }; }

	@Override
	public byte[] input(int size, String contentType, Charset encoding) throws IOException {		
		try(PDDocument doc = new PDDocument()) {
			for(int i=0; i<size; i++) {
				PDPage page = new PDPage();
				try(PDPageContentStream stream = new PDPageContentStream(doc, page)) {
					stream.beginText();
					stream.setFont(PDType1Font.TIMES_ROMAN, 30);
					stream.newLineAtOffset(200, 500);
					stream.showText(String.valueOf(i));
					stream.endText();
				}
				doc.addPage(page);
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			doc.save(new BufferedOutputStream(out));
			return out.toByteArray();
		}
	}
	
	@Override
	public void assertExcerpt(byte[] original, InputStream in, long[] fragments) throws IOException {
		PDDocument doc = PDDocument.load(in);
		assertThat(doc.getNumberOfPages()).as("Page count mismatch").isEqualTo(fragments.length);
		
		for (int i = 0; i < fragments.length; i++) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setStartPage(i+1);
			stripper.setEndPage(i+1);
			
			int origIndex = strictToInt(fragments[i]);
			int storedIndex = Integer.parseInt(stripper.getText(doc).trim());
			assertThat(storedIndex)
				.as("Page mismath in excerpt at page %d", _int(i))
				.isEqualTo(origIndex);
		}		
	}
}
