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
package de.unistuttgart.xsample;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * @author Markus Gärtner
 *
 */
public class PdfHelper {

	public static void main(String[] args) throws IOException {
		Path file = Paths.get("100p_dummy.pdf");
		try(OutputStream out = Files.newOutputStream(file)) {
			createPDF(100, 1, out);
		}
	}
	
	private static void createPDF(int pages, int begin, OutputStream out) throws IOException {	
		try(PDDocument doc = new PDDocument()) {
			for(int i=0; i<pages; i++) {
				PDPage page = new PDPage();
				try(PDPageContentStream stream = new PDPageContentStream(doc, page)) {
					stream.beginText();
					stream.setFont(PDType1Font.TIMES_ROMAN, 100);
					stream.newLineAtOffset(200, 500);
					stream.showText(String.valueOf(begin++));
					stream.endText();
				}
				doc.addPage(page);
			}
			
			doc.save(out);
		}
	}
}
