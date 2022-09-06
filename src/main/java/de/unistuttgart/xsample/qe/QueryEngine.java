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
package de.unistuttgart.xsample.qe;

import static de.unistuttgart.xsample.util.XSampleUtils.checkArgument;
import static de.unistuttgart.xsample.util.XSampleUtils.decrypt;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.io.LocalCache;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.ManifestFile;
import de.unistuttgart.xsample.mf.MappingFile;
import de.unistuttgart.xsample.mp.Mapping;
import de.unistuttgart.xsample.pages.shared.CorpusData;
import de.unistuttgart.xsample.pages.shared.SharedData;
import de.unistuttgart.xsample.qe.MappingException.MappingErrorCode;
import de.unistuttgart.xsample.qe.QueryException.QueryErrorCode;
import de.unistuttgart.xsample.qe.icarus1.Icarus1Wrapper;
import de.unistuttgart.xsample.qe.icarus1.Icarus1Wrapper.ResultPart;
import de.unistuttgart.xsample.util.XSampleUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class QueryEngine implements Serializable {

	private static final long serialVersionUID = 4603513765690341840L;

	@Inject
	LocalCache cache;
	
	@Inject
	XsampleServices services;
	
	@Inject
	SharedData excerptData;
	@Inject
	CorpusData corpusData;
	
	public QueryInfo query(String query) throws QueryException {
		Icarus1Wrapper wrapper = new Icarus1Wrapper();
		wrapper.init(query, new Properties()); //TODO forward search settings
		
		List<Result> results = new ObjectArrayList<>();
		long segments = 0;
		
		for(Corpus corpus : excerptData.getManifest().getAllParts()) {
			final ManifestFile manifest = excerptData.findManifest(corpus);
			final XmpResource resource = services.findResource(excerptData.getServer(), manifest.getId());
			final XmpLocalCopy copy = cache.getCopy(resource);
			
			try {
				copy.getLock().tryLock(50, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new QueryException("Failed to acquire lock for annotations file", QueryErrorCode.RESOURCE_LOCKED, manifest.getLabel(), e);
			}
			try {		
				final Path file = cache.getDataFile(copy);
				Cipher cipher = decrypt(XSampleUtils.deserializeKey(copy.getKey()));
				final Charset encoding = Charset.forName(copy.getEncoding());

				try(InputStream raw = Files.newInputStream(file, StandardOpenOption.READ);
						InputStream in = new CipherInputStream(raw, cipher);
						Reader reader = new InputStreamReader(in, encoding);) {
					
					ResultPart resultPart = wrapper.evaluate(reader);
					if(!resultPart.isEmpty()) {
						results.add(resultPart.getResult());
					}
					// Acucmulate searchable segments in any case
					segments += resultPart.getSegments();
				}
			} catch(QueryException e) {
				// Decorate exception with contextual info and rethrow
				e.setCorpusId(corpus.getId());
				throw e;
			} catch (IOException e) {
				throw new QueryException("Unable to read annotations file", QueryErrorCode.IO_ERROR, manifest.getLabel(), e);
			} catch (GeneralSecurityException e) {
				throw new QueryException("Unable to decrypt annotations file", QueryErrorCode.SECURITY_ERROR, manifest.getLabel(), e);
			} finally {
				copy.getLock().unlock();
			}
		}
		
		return new QueryInfo(results, segments);
	}
	
	/** Maps 0-based hits in the annotation space into 1-based segments of the primary data.
	 * @throws MappingException */
	public List<Result> mapSegments(List<Result> results) throws MappingException {
		requireNonNull(results);
		checkArgument("Only supports a single result set currently", results.size()==1);
		
		List<Result> result = new ArrayList<>();
		
		for(Result original : results) {
			if(original.isEmpty()) {
				continue;
			}
			final String corpusId = original.getCorpusId();
			final Result mapped = new Result();
			mapped.setCorpusId(corpusId);
			final Corpus corpus = excerptData.findCorpus(corpusId);
			final ManifestFile manifestFile = excerptData.findManifest(corpus);
			if(manifestFile==null)
				throw new MappingException("No manifest for "+corpusId, MappingErrorCode.MISSING_MANIFEST, corpusId);
			final MappingFile mappingFile = manifestFile.getMappingFile();
			if(mappingFile==null)
				throw new MappingException("No mapping for "+corpusId, MappingErrorCode.MISSING_MAPPING, corpusId);
			
			Mapping mapping;
			try {
				mapping = excerptData.getMapping(null, services, cache);
			} catch (UnsupportedContentTypeException e) {
				throw new MappingException("Can't read mapping format", MappingErrorCode.UNSUPPORTED_FORMAT, mappingFile.getLabel(), e);
			} catch (GeneralSecurityException e) {
				throw new MappingException("Failed to access mapping file", MappingErrorCode.SECURITY_ERROR, mappingFile.getLabel(), e);
			} catch (IOException e) {
				throw new MappingException("Reading of mapping file failed", MappingErrorCode.IO_ERROR, mappingFile.getLabel(), e);
			} catch (InterruptedException e) {
				throw new MappingException("Mapping file locked", MappingErrorCode.RESOURCE_LOCKED, mappingFile.getLabel(), e);
			}
			
			try {
				map(original, mapping, mapped, corpusData.getSegments(corpus));
			} catch(RuntimeException e) {
				throw new MappingException("Unexpected internal error", MappingErrorCode.INTERNAL_ERROR, mappingFile.getLabel(), e);
			}
			
			if(!mapped.isEmpty()) {
				result.add(mapped);
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param source 0-based input indices
	 * @param mapping converter, works 0-based on both ends
	 * @param target 1-based output indices
	 * @param targetLimit 1-based maximum for output indices
	 */
	private void map(Result source, Mapping mapping, Result target, long targetLimit) {
		LongList buffer = new LongArrayList();
		
		/* We only need to onsider mapped segments that are "new" to the
		 * result, since we're bound to have a lot of overlap.
		 */
		long max = -1;
		for(long sourceIndex : source.getHits()) {
			long targetBegin = mapping.getTargetBegin(sourceIndex)+1;
			long targetEnd = mapping.getTargetEnd(sourceIndex)+1;
			
			// Initial span or completely outside stored area
			if(max==-1 || targetBegin>max) {
				if(!feed(buffer, targetBegin, targetEnd, targetLimit)) {
					break;
				}
				max = targetEnd;
			} else if(targetEnd > max) {
				// Span is overlapping with last one
				if(!feed(buffer, max+1, targetEnd, targetLimit)) {
					break;
				}
				
				max = targetEnd;
			}
		}
		
		if(!buffer.isEmpty()) {
			target.setHits(buffer.toLongArray());
		}
	}
	
	private boolean feed(LongList buffer, long from, long to, long limit) {
		for (long index = from; index <= to; index++) {
			if(index>limit) {
				return false;
			}
			buffer.add(index);
		}
		return true;
	}
}
