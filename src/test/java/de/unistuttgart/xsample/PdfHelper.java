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
