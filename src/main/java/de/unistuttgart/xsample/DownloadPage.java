/**
 * 
 */
package de.unistuttgart.xsample;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class DownloadPage {
	
	public static final String PAGE = "download";

	public void onDownload() {
		//TODO

//		ExcerptConfig excerpt = xsampleData.getExcerpt();
//		FileInfo fileInfo = excerpt.getFileInfo();
//		ExcerptHandler handler = excerpt.getHandler();
//
//		FacesContext fc = FacesContext.getCurrentInstance();
//		
//		try {			
//			Fragment fragment = Fragment.of(excerpt.getStart()-1, excerpt.getEnd()-1);
//			
//			if(!Files.exists(fileInfo.getTempFile())) {
//				Messages.addGlobalWarn(BundleUtil.get("homepage.tabs.download.deleted"));
//				return;
//			}
//			
//			// Prepare basic response header
//			HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();			
//			response.reset();
//			response.setContentType(fileInfo.getContentType());
//			//TODO enable size info again if we buffer excerpt on server
////		    response.setContentLength(strictToInt(fileInfo.getSize())); // disabled since we don't know size of excerpt
//		    response.setHeader("Content-Disposition", "attachment; filename=\"XSample_" + fileInfo.getTitle() + "\"");
//		    
//		    // Prepare response content
//			try(InputStream raw = Files.newInputStream(fileInfo.getTempFile(), 
//					StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
//					InputStream in = new CipherInputStream(raw, decrypt(fileInfo.getKey()))) {
//				OutputStream out = response.getOutputStream();
//				handler.excerpt(fileInfo, in, new Fragment[] {fragment}, out);
//			}
//		} catch (IOException e) {
//			logger.log(Level.SEVERE, "Failed to create excerpt", e);
//			Messages.addGlobalError(BundleUtil.get("homepage.tabs.download.error"));
//			return;
//		} catch (GeneralSecurityException e) {
//			logger.log(Level.SEVERE, "Failed to prepare cipher", e);
//			Messages.addGlobalError(BundleUtil.get("homepage.error.cipher"));
//			return;
//		}
//	    
//	    fc.responseComplete();
	}
}
