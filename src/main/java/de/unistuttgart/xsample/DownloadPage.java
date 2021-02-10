/**
 * 
 */
package de.unistuttgart.xsample;

import static de.unistuttgart.xsample.util.XSampleUtils.decrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.CipherInputStream;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class DownloadPage {
	
	public static final String PAGE = "download";
	
	private static final Logger logger = Logger.getLogger(DownloadPage.class.getCanonicalName());
	
	@Inject
	XsampleExcerptData excerptData;

	//TODO check http://www.primefaces.org:8080/showcase/ui/file/download.xhtml?jfwid=0b585 for example of monitoring during download initialization
	
	public void onDownload() {
		//TODO

		final FileInfo fileInfo = excerptData.getFileInfo();
		ExcerptHandler handler;
		try {
			handler = ExcerptHandlers.forInputType(excerptData.getInputType());
		} catch (UnsupportedContentTypeException e) {
			logger.log(Level.SEVERE, "Content type of file not supported (this should not happen!!)", e);
			Messages.addGlobalError("download.msg.unsupportedType");
			return;
		}

		FacesContext fc = FacesContext.getCurrentInstance();
		
		try {			
			Fragment[] fragments = excerptData.getExcerpt().toArray(new Fragment[0]);
			
			if(!Files.exists(fileInfo.getTempFile())) {
				Messages.addGlobalError(BundleUtil.get("download.msg.resourceDeleted"));
				return;
			}
			
			// Prepare basic response header
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();			
			response.reset();
			response.setContentType(fileInfo.getContentType());
			//TODO enable size info again if we buffer excerpt on server
//		    response.setContentLength(strictToInt(fileInfo.getSize())); // disabled since we don't know size of excerpt
		    response.setHeader("Content-Disposition", "attachment; filename=\"XSample_" + fileInfo.getTitle() + "\"");
		    
		    // Prepare response content
			try(InputStream raw = Files.newInputStream(fileInfo.getTempFile(), 
					StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
					InputStream in = new CipherInputStream(raw, decrypt(fileInfo.getKey()))) {
				OutputStream out = response.getOutputStream();
				handler.excerpt(fileInfo, in, fragments, out);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to create excerpt", e);
			Messages.addGlobalError(BundleUtil.get("download.msg.error"), e.getMessage());
			return;
		} catch (GeneralSecurityException e) {
			logger.log(Level.SEVERE, "Failed to prepare cipher", e);
			Messages.addGlobalError(BundleUtil.get("download.msg.error"), e.getMessage());
			return;
		}
	    
	    fc.responseComplete();
	}
}
