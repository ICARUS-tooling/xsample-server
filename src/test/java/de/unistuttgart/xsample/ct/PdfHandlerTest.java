/**
 * 
 */
package de.unistuttgart.xsample.ct;

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;

import de.unistuttgart.xsample.util.DataInput;
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
	
	private byte[] doc(int pages) throws IOException {		
		try(PDDocument doc = new PDDocument()) {
			for(int i=0; i<pages; i++) {
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
	public DataInput input(int size, String contentType, Charset encoding) throws IOException {
		byte[] data = doc(size);
		return DataInput.virtual(encoding, contentType, data);
	}

	@Override
	public void assertExcerpt(DataInput excerpt, long[] fragments) throws IOException {
		PDDocument doc = PDDocument.load(buffer(excerpt.content()));
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
