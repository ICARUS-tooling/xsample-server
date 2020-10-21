/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.util.FileInfo;
import de.unistuttgart.xsample.util.Payload;

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
		ExcerptHandler handler = config.getHandler();
		FileInfo fileInfo = config.getFileInfo();
		
		Fragment fragment = Fragment.of(config.getStart(), config.getEnd());
		
		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
		
		response.reset();
		response.setContentType(fileInfo.getContentType());
//	    response.setContentLength(strictToInt(fileInfo.getSize())); // disabled since we don't know size of excerpt
	    response.setHeader("Content-Disposition", "attachment; filename=\"XSample_" + fileInfo.getTitle() + "\"");

		try {
			Payload output = Payload.forOutput(fileInfo.getEncoding(), 
					fileInfo.getContentType(), response.getOutputStream());
			handler.excerpt(new Fragment[] {fragment}, output);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to create excerpt", e);
			Messages.addGlobalError(BundleUtil.get("homepage.tabs.data.download.fetch"));
			return;
		}
	    
	    fc.responseComplete();
	}
}
