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
package de.unistuttgart.xsample.io;

import static de.unistuttgart.xsample.util.XSampleUtils._int;
import static de.unistuttgart.xsample.util.XSampleUtils._long;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.mf.Corpus;

/**
 * @author Markus Gärtner
 *
 */
public class FileInfo implements Serializable {

	private static final long serialVersionUID = -8926296993866371029L;
	
	/** Display name of the source file */
	private String title;
	/** MIME type of the source file */
	private String contentType;
	/** Character encoding used for source file */
	private String encoding;
	/** Size in bytes of the source file */
	private long size;
	/** Number of segments that can be extracted. */
	private long segments;
	/** Flag to indicate that the file is too small for excerpt generation */
	private boolean smallFile = false;
	
	/** Id of the associated {@link Corpus} in the manifest. */
	private String corpusId;

	/** The designated handler to manage excerpt generation and analysis of the source file */
	private transient ExcerptHandler excerptHandler;
	
	public FileInfo() { /* no-op */ }
	
	public FileInfo(Corpus corpus) {
		setCorpusId(requireNonNull(corpus.getId(), "Corpus is missing id"));
	}
	
	public FileInfo(String contentType, Charset encoding) {
		setContentType(contentType);
		setEncoding(encoding);
		
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		requireNonNull(title);
		this.title = title;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		requireNonNull(contentType);
		this.contentType = contentType;
	}
	public Charset getEncoding() {
		return encoding==null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
	}
	public void setEncoding(Charset encoding) {
		requireNonNull(encoding);
		this.encoding = encoding.name();
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getSegments() {
		return segments;
	}
	public void setSegments(long segments) {
		this.segments = segments;
	}
	
	public ExcerptHandler getExcerptHandler() { return excerptHandler; }
	public void setExcerptHandler(ExcerptHandler excerptHandler) { this.excerptHandler = excerptHandler; }
	
	public String getCorpusId() {
		return corpusId;
	}
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}
	
	public boolean isSmallFile() { return smallFile; }
	public void setSmallFile(boolean smallFile) { this.smallFile = smallFile; }

	@Override
	public String toString() {
		return String.format("FileInfo@%d[title=%s, contentType=%s, encoding)%s, size=%d, segments=%d]", 
				_int(hashCode()), title, contentType, encoding, _long(size), _long(segments));
	}
}
