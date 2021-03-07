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
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.io.FileInfo;
import de.unistuttgart.xsample.io.LocalCache;
import de.unistuttgart.xsample.io.NonClosingOutputStreamDelegate;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData.ExcerptEntry;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class DownloadPage extends XsamplePage {
	
	public static final String PAGE = "download";
	
	private static final Logger logger = Logger.getLogger(DownloadPage.class.getCanonicalName());

	@Inject
	LocalCache cache;
	
	//TODO check http://www.primefaces.org:8080/showcase/ui/file/download.xhtml?jfwid=0b585 for example of monitoring during download initialization

	@Override
	protected void rollBack() {
		excerptData.resetExcerpt();
	}
	
	public boolean isHasAnnotations() { return excerptData.hasCorpus(Corpus::hasManifests); }
	
	@Transactional
	public void download() {
		
		final List<ExcerptEntry> entries = excerptData.getExcerpt();
		final List<XmpLocalCopy> copies = new ArrayList<>(entries.size());
		
		// Early check if we already downloaded the excerpt
		for(ExcerptEntry entry : entries) {
			XmpLocalCopy copy = cache.getCopy(entry.getResource());
			if(copy==null || !cache.isPopulated(copy)) {
				Messages.addGlobalError(BundleUtil.get("download.msg.resourceDeleted"));
				return;
			}
			copies.add(copy);
		}
		
		// Acquire locks on all required resoruces
		for(int i=0; i<copies.size(); i++) {
			XmpLocalCopy copy = copies.get(i);
			boolean locked = false;
			try {
				locked = copy.tryLockRead(50, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				locked = false;
			}
			
			if(!locked) {
				// Release all previously acquired locks if one failed!!
				for (int j = i-1; j >=0; j--) {
					copies.get(j).unlockRead();
				}
				// Notify user and bail
				Messages.addGlobalError(BundleUtil.format("download.msg.cacheBusy", copy.getDataFile()));
				return;
			}
		}
		
		assert entries.size()==copies.size() : "Mismatch between entries and copies";

		final FacesContext fc = FacesContext.getCurrentInstance();
		final HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();			
		response.reset();
		response.setContentType("application/zip");
		//TODO enable size info again if we buffer excerpt on server
//	    response.setContentLength(strictToInt(fileInfo.getSize())); // disabled since we don't know size of excerpt
	    response.setHeader("Content-Disposition", "attachment; filename=\"XSample_excerpt.zip\"");
		
		try(OutputStream out = response.getOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(out)) {
			
			// First place a index list containing al lthe files to expect
			addIndex(zipOut, entries);
			
			// Add all the excerpts
			for(int i=0; i<entries.size(); i++) {
				addExcerptEntry(zipOut, entries.get(i), copies.get(i));
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
	
	private void addIndex(ZipOutputStream zipOut, List<ExcerptEntry> entries) throws IOException {
		zipOut.putNextEntry(new ZipEntry("INDEX.txt"));
		
		try(OutputStream out = new NonClosingOutputStreamDelegate(zipOut);
				OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(osw)) {
			for(ExcerptEntry entry : entries) {
				final FileInfo fileInfo = excerptData.findFileInfo(entry.getCorpusId());

				writer.write(fileInfo.getTitle());
				writer.newLine();
				
				out.flush();
			}
			writer.newLine();
		}
	}
	
	private void addExcerptEntry(ZipOutputStream zipOut, ExcerptEntry entry, XmpLocalCopy copy) 
			throws IOException, GeneralSecurityException {
		final FileInfo fileInfo = excerptData.findFileInfo(entry.getCorpusId());
		final ZipEntry zipEntry = new ZipEntry("excerpt_"+fileInfo.getTitle());
		final ExcerptHandler handler = fileInfo.getExcerptHandler();
		final List<XmpFragment> fragments = entry.getFragments();
		final Path file = cache.getCopyFile(copy);
		final Cipher cipher = decrypt(XSampleUtils.deserializeKey(copy.getKey()));
		
		zipOut.putNextEntry(zipEntry);
		// Now produce excerpt and add file to zip
		try(InputStream raw = Files.newInputStream(file, StandardOpenOption.READ);
				InputStream in = new CipherInputStream(raw, cipher);
				OutputStream out = new NonClosingOutputStreamDelegate(zipOut);) {
			
			handler.excerpt(fileInfo, in, fragments, out);
			
			out.flush();
		}
		
		//TODO in the future also split and add annotations here
	}
	
	private void addLegalNote(ZipOutputStream zipOut) throws IOException {
		zipOut.putNextEntry(new ZipEntry("LEGAL.txt"));
		
		try(OutputStream out = new NonClosingOutputStreamDelegate(zipOut);
				OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(osw)) {
			writer.write("<here be legal notes>");
			writer.newLine();
			
			out.flush();
		}
	}
}
