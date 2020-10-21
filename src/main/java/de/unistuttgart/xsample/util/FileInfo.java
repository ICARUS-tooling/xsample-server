/*
 * XSample Server
 * Copyright (C) 2020-2020 Markus G�rtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.nio.charset.Charset;

/**
 * @author Markus G�rtner
 *
 */
public class FileInfo {

	/** Display name of the source file */
	private String title;
	/** MIME type of the source file */
	private String contentType;
	/** Character encoding used for source file */
	private Charset encoding;
	/** Size in bytes of the yource file */
	private long size;
	
	public FileInfo() { /* no-op */ }
	
	public FileInfo(String title, String contentType, Charset encoding, long size) {
		setTitle(title);
		setContentType(contentType);
		setEncoding(encoding);
		setSize(size);
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
		return encoding;
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
}
