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
package de.unistuttgart.xsample.pages.download;

import static de.unistuttgart.xsample.util.XSampleUtils.decrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.CipherInputStream;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.mf.SourceType;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class DownloadPage extends XsamplePage {
	
	public static final String PAGE = "download";
	
	private static final Logger logger = Logger.getLogger(DownloadPage.class.getCanonicalName());

	//TODO check http://www.primefaces.org:8080/showcase/ui/file/download.xhtml?jfwid=0b585 for example of monitoring during download initialization
	
	@Transactional
	public void download() {
		
		final FileInfo fileInfo = excerptData.getFileInfo();
		
		// Early check if we already downloaded the excerpt
		if(!Files.exists(fileInfo.getTempFile())) {
			Messages.addGlobalError(BundleUtil.get("download.msg.resourceDeleted"));
			return;
		}
		
		// Obtain handler to create excerpt
		ExcerptHandler handler;
		try {
			final SourceType sourceType = excerptData.getManifest().getTarget().getSourceType();
			handler = ExcerptHandlers.forSourceType(sourceType);
		} catch (UnsupportedContentTypeException e) {
			logger.log(Level.SEVERE, "Content type of file not supported (this should not happen!!)", e);
			Messages.addGlobalError("download.msg.unsupportedType");
			return;
		}
		
		// Fetch excerpt
		final List<Fragment> fragments = excerptData.getExcerpt();

		// Now produce excerpt and send back data
		FacesContext fc = FacesContext.getCurrentInstance();
		try {
			
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
		
		// If everything went well, add to quota
		final Excerpt quota = excerptData.getQuota();
		quota.merge(fragments);
		services.save(quota);
		
	    fc.responseComplete();
	}
}
