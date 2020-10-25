/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.unistuttgart.xsample.util;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.crypto.SecretKey;

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
	private Charset encoding;
	/** Size in bytes of the source file */
	private long size;
	/** Number of segments that can be extracted. */
	private long segments;
	
	/** Temporary file on the server, encrypted with {@link #key}.  */
	private Path tempFile;
	/** Key for encrypting/decrypting temporary file. */
	private SecretKey key;
	
	public FileInfo() { /* no-op */ }
	
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
		return encoding==null ? StandardCharsets.UTF_8 : encoding;
	}
	public void setEncoding(Charset encoding) {
		requireNonNull(encoding);
		this.encoding = encoding;
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
	public Path getTempFile() {
		return tempFile;
	}
	public void setTempFile(Path tempFile) {
		this.tempFile = requireNonNull(tempFile);
	}
	public SecretKey getKey() {
		return key;
	}
	public void setKey(SecretKey key) {
		this.key = key;
	}
}
