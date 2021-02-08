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
package de.unistuttgart.xsample.dep;

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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.util.BundleUtil;

/**
 * @author Markus Gärtner
 *
 */
//@Named
//@RequestScoped
@Deprecated
public class XsampleController {
	
	private static final Logger logger = Logger.getLogger(XsampleController.class.getCanonicalName());

	@Inject
	private XsampleData xsampleData;
	
	@Inject
	private XsampleServices xsampleServices;

	public void onLoad() {
		XsampleConfig config = xsampleData.getConfig();
		config.setServerRoute(xsampleServices.getSetting(Key.ServerName));
	} 
	
	public void welcomeProgress() {
		//TODO check file and continue depending on setup
		
		XsampleConfig config = xsampleData.getConfig();
		
		String page = null;
		
		switch (config.getType()) {
		case STATIC: {
			ExcerptConfig excerpt = xsampleData.getExcerpt();
			//TODO configure excerpt
			downloadExcerpt(excerpt);
		} break;
		case WINDOW: page = "window"; break;
		case QUERY: page = "query"; break;

		default:
			Messages.addGlobalError("Unknown excerpt type: %s", config.getType());
			break;
		}
		
		if(page!=null) {
			xsampleData.setPage(page);
		}
	}
	
	public void windowProgress() {
		System.out.println("slice requested");
	}
	
	public void queryProgress() {
		System.out.println("query requested");
	}
	
	static void downloadExcerpt(ExcerptConfig excerpt) {
		FileInfo fileInfo = excerpt.getFileInfo();
		ExcerptHandler handler = excerpt.getHandler();

		FacesContext fc = FacesContext.getCurrentInstance();
		
		try {			
			Fragment fragment = Fragment.of(excerpt.getStart()-1, excerpt.getEnd()-1);
			
			if(!Files.exists(fileInfo.getTempFile())) {
				Messages.addGlobalWarn(BundleUtil.get("homepage.tabs.download.deleted"));
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
