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
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.FileInfo;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class ExcerptDownloadBean {

	private static final Logger logger = Logger.getLogger(ExcerptDownloadBean.class.getCanonicalName());
	
	@Inject
	XsamplePage xsamplePage;

	public void downloadExcerpt() {
		XsampleExcerptConfig config = xsamplePage.getConfig();
		FileInfo fileInfo = config.getFileInfo();
		ExcerptHandler handler = config.getHandler();

		FacesContext fc = FacesContext.getCurrentInstance();
		
		try {			
			Fragment fragment = Fragment.of(config.getStart()-1, config.getEnd()-1);
			
			if(!Files.exists(fileInfo.getTempFile())) {
				Messages.addGlobalWarn(BundleUtil.get("homepage.tabs.download.deleted"));
				return;
			}
			
			// Prepare basic response header
			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();			
			response.reset();
			response.setContentType(fileInfo.getContentType());
//		    response.setContentLength(strictToInt(fileInfo.getSize())); // disabled since we don't know size of excerpt
		    response.setHeader("Content-Disposition", "attachment; filename=\"XSample_" + fileInfo.getTitle() + "\"");
		    
		    // Prepare response content
			try(InputStream raw = Files.newInputStream(fileInfo.getTempFile(), 
					StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
					InputStream in = new CipherInputStream(raw, decrypt(fileInfo.getKey()))) {
				OutputStream out = response.getOutputStream();
				handler.excerpt(fileInfo, in, new Fragment[] {fragment}, out);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to create excerpt", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.download.error"));
			return;
		} catch (GeneralSecurityException e) {
			logger.log(Level.SEVERE, "Failed to prepare cipher", e);
			Messages.addGlobalError(BundleUtil.get("homepage.error.cipher"));
			return;
		}
	    
	    fc.responseComplete();
	}
}
