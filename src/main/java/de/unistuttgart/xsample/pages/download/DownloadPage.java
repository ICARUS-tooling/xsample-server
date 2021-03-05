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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.CipherInputStream;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData.ExcerptEntry;
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

	@Override
	protected void rollBack() {
		excerptData.resetExcerpt();
	}
	
	@Transactional
	public void download() {
		
		// Early check if we already downloaded the excerpt
		for(FileInfo fileInfo : excerptData.getFileInfos()) {
			if(!Files.exists(fileInfo.getTempFile())) {
				Messages.addGlobalError(BundleUtil.get("download.msg.resourceDeleted"));
				return;
			}
		}
		
		final List<ExcerptEntry> entries = excerptData.getExcerpt();

		final FacesContext fc = FacesContext.getCurrentInstance();
		final HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();			
		response.reset();
		response.setContentType("application/zip");
		//TODO enable size info again if we buffer excerpt on server
//	    response.setContentLength(strictToInt(fileInfo.getSize())); // disabled since we don't know size of excerpt
	    response.setHeader("Content-Disposition", "attachment; filename=\"XSample_excerpt.zip\"");
		
		try(OutputStream out = response.getOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(out)) {
			
			// Add all the excerpts
			for(ExcerptEntry entry : entries) {
				addExcerptEntry(zipOut, entry);
			}
			
			// Add the legal note
			addLegalNote(zipOut);
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to create excerpt", e);
			Messages.addGlobalError(BundleUtil.get("download.msg.error"), e.getMessage());
		} catch (GeneralSecurityException e) {
			logger.log(Level.SEVERE, "Failed to prepare cipher", e);
			Messages.addGlobalError(BundleUtil.get("download.msg.error"), e.getMessage());
		}
		
	    fc.responseComplete();
		

		// If everything went well, add to quota
		for(ExcerptEntry entry : entries) {
			final XmpExcerpt quota = entry.getQuota();
			quota.merge(entry.getFragments());
			services.save(quota);
		}
	}
	
	private void addExcerptEntry(ZipOutputStream zipOut, ExcerptEntry entry) 
			throws IOException, GeneralSecurityException {
		final FileInfo fileInfo = excerptData.findFileInfo(entry.getCorpusId());
		final ZipEntry zipEntry = new ZipEntry("excerpt_"+fileInfo.getTitle());
		final ExcerptHandler handler = fileInfo.getExcerptHandler();
		final List<XmpFragment> fragments = entry.getFragments();

		zipOut.putNextEntry(zipEntry);
		// Now produce excerpt and add file to zip
		try(InputStream raw = Files.newInputStream(fileInfo.getTempFile(), 
				StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
				InputStream in = new CipherInputStream(raw, decrypt(fileInfo.getKey()))) {
			handler.excerpt(fileInfo, in, fragments, zipOut);
		}
		
		//TODO in the future also split and add annotations here
	}
	
	private void addLegalNote(ZipOutputStream zipOut) throws IOException {
		zipOut.putNextEntry(new ZipEntry("LEGAL.txt"));
		
		try(OutputStreamWriter osw = new OutputStreamWriter(zipOut, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(osw)) {
			writer.write("<here be legal notes>");
			writer.newLine();
		}
	}
}
