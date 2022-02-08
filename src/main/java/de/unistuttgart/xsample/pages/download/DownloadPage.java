/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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

import com.google.common.io.CountingOutputStream;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.io.LocalCache;
import de.unistuttgart.xsample.io.NonClosingOutputStreamDelegate;
import de.unistuttgart.xsample.mf.XsampleManifest;
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
		//TODO do we actually have anything to roll back here?
	}
	
	public boolean isHasAnnotations() {
		XsampleManifest manifest = excerptData.getManifest();
		return manifest!=null && manifest.hasManifests();
	}
	
	@Transactional
	public void download() {
		
		final List<ExcerptEntry> entries = excerptData.getExcerpt();
		final List<XmpLocalCopy> copies = new ArrayList<>(entries.size());
		
		// Early check if we already obsoleted the source data
		for(ExcerptEntry entry : entries) {
			XmpLocalCopy copy = cache.getCopy(entry.getResource());
			if(copy==null || !cache.isPopulated(copy)) {
				Messages.addGlobalError(BundleUtil.get("download.msg.resourceDeleted"));
				return;
			}
			copies.add(copy);
		}

		final FacesContext fc = FacesContext.getCurrentInstance();
		
		// Acquire locks on all required resoruces
		for(int i=0; i<copies.size(); i++) {
			XmpLocalCopy copy = copies.get(i);
			boolean locked = false;
			try {
				locked = copy.getLock().tryLock(50, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				locked = false;
			}
			
			if(!locked) {
				// Release all previously acquired locks if one failed!!
				for (int j = i-1; j >=0; j--) {
					copies.get(j).getLock().unlock();
				}
				// Notify user and bail
				logger.info(String.format("Copy %s of resource %s locked", copy.getFilename(), copy.getTitle()));
				Messages.addGlobalError(BundleUtil.format("download.msg.cacheBusy", copy.getTitle()));
				return;
			}
		}
		
		assert entries.size()==copies.size() : "Mismatch between entries and copies";
		
		try {
			final HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();			
			response.reset();
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"XSample_excerpt.zip\"");
			
		    final long zipSize;
		    
			try(OutputStream out = response.getOutputStream();
					CountingOutputStream cout = new CountingOutputStream(out);
					ZipOutputStream zipOut = new ZipOutputStream(cout)) {
				
				// First place an index list containing all the files to expect
				addIndex(zipOut, entries, copies);
				
				// Add all the excerpts
				for(int i=0; i<entries.size(); i++) {
					addExcerptEntry(zipOut, entries.get(i), copies.get(i));
				}
				
				// Add the legal note
				addLegalNote(zipOut);
				
				// Force flush of pending data
				zipOut.finish();
				
				// Collect final size of the zip file for client information purposes
				zipSize = cout.getCount();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Failed to create excerpt", e);
				Messages.addGlobalError(BundleUtil.get("download.msg.error"), e.getMessage());
				response.reset();
				return;
			} catch (GeneralSecurityException e) {
				logger.log(Level.SEVERE, "Failed to prepare cipher", e);
				Messages.addGlobalError(BundleUtil.get("download.msg.error"), e.getMessage());
				response.reset();
				return;
			}
			
			response.setContentLength(strictToInt(zipSize));
		    
		} finally {
			// Release all locks again (use reverse order of lock acquisition)
			for(int i=copies.size()-1; i>=0; i--) {
				XmpLocalCopy copy = copies.get(i);
				copy.getLock().unlock();
			}
		}
		
	    fc.responseComplete();
		

		// If everything went well, add to quota
		for(ExcerptEntry entry : entries) {
			final XmpExcerpt quota = entry.getQuota();
			quota.merge(entry.getFragments());
			services.update(quota);
		}
	}
	
	private void addIndex(ZipOutputStream zipOut, List<ExcerptEntry> entries,
			List<XmpLocalCopy> copies) throws IOException {
		zipOut.putNextEntry(new ZipEntry("INDEX.txt"));
		
		try(OutputStream out = new NonClosingOutputStreamDelegate(zipOut);
				OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
				BufferedWriter writer = new BufferedWriter(osw)) {
			for(XmpLocalCopy copy : copies) {
				writer.write(copy.getTitle());
				writer.newLine();
				
				out.flush();
			}
			writer.newLine();
		}
	}
	
	private void addExcerptEntry(ZipOutputStream zipOut, ExcerptEntry entry, XmpLocalCopy copy) 
			throws IOException, GeneralSecurityException {
		final ZipEntry zipEntry = new ZipEntry("excerpt_"+copy.getTitle());
		final List<XmpFragment> fragments = entry.getFragments();
		final Path file = cache.getDataFile(copy);
		final Cipher cipher = decrypt(XSampleUtils.deserializeKey(copy.getKey()));
		final XmpFileInfo fileInfo = services.findFileInfo(copy.getResource());
		final Charset encoding = Charset.forName(copy.getEncoding());
		final ExcerptHandler handler;
		try {
			handler = ExcerptHandlers.forSourceType(fileInfo.getSourceType());
		} catch (UnsupportedContentTypeException e) {
			throw new InternalError("No handler available for rpeviously validated file: "+copy.getTitle(), e);
		}
		
		zipOut.putNextEntry(zipEntry);
		// Now produce excerpt and add file to zip
		try(InputStream raw = Files.newInputStream(file, StandardOpenOption.READ);
				InputStream in = new CipherInputStream(raw, cipher);
				OutputStream out = new NonClosingOutputStreamDelegate(zipOut);) {
			handler.excerpt(fileInfo, encoding, in, fragments, out);
			
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
