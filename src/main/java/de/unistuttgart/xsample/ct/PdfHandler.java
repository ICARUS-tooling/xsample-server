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
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import de.unistuttgart.xsample.Fragment;
import de.unistuttgart.xsample.util.DataInput;
import de.unistuttgart.xsample.util.DataOutput;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
public class PdfHandler implements ExcerptHandler {
	
	private PDDocument document;
	
	private void checkDoc() {
		if(document==null)
			throw new IllegalStateException("No document available");
	}

	@Override
	public void init(DataInput input) throws IOException, UnsupportedContentTypeException, EmptyResourceException {
		requireNonNull(input);
		if(!XSampleUtils.MIME_PDF.equals(input.contentType()))
			throw new UnsupportedContentTypeException(
					"Not an '"+XSampleUtils.MIME_PDF+"' resource: "+input.contentType());
		
		PDDocument document;
		try(InputStream in = input.content()) {
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
	}

	/**
	 * @see de.unistuttgart.xsample.ct.ExcerptHandler#excerpt(de.unistuttgart.xsample.Fragment[], de.unistuttgart.xsample.util.DataOutput)
	 */
	@Override
	public DataOutput excerpt(Fragment[] fragments) throws IOException {
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
			
			DataOutput output = DataOutput.virtual(XSampleUtils.MIME_PDF, StandardCharsets.UTF_8);
			try(OutputStream out = buffer(output.content())) {
				newDocument.save(out);
			}
			return output;
		}
	}

}
