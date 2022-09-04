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
package de.unistuttgart.xsample.dv;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

/**
 * Models all the informatio nregarding a local copy of a protected resource.
 * Note that <b>all</b> I/O operations on the associated file obejcts <b>must</b>
 * be performed while holding the respective lock! Various methods are available for
 * obtianing and releasing locks for write or read operations, such as
 * {@link #lockRead()}, {@link #tryLockRead(long, TimeUnit)}, {@link #unlockWrite()}, etc...
 * 
 * @author Markus Gärtner
 *
 */
@Entity(name = XmpLocalCopy.TABLE_NAME)
@NamedQueries({
	@NamedQuery(name = "LocalCopy.findByResource", query = "SELECT c FROM LocalCopy c WHERE c.resource = :resource"), 
	@NamedQuery(name = "LocalCopy.findByDataFile", query = "SELECT c FROM LocalCopy c WHERE c.filename = :filename"), 
	@NamedQuery(name = "LocalCopy.findExpired", query = "SELECT c FROM LocalCopy c WHERE c.expiresAt < :timestamp"), 
})
public class XmpLocalCopy {
	
    public static final String TABLE_NAME= "LocalCopy";

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, columnDefinition = "TIMESTAMP")
	private LocalDateTime expiresAt;
	
	@OneToOne
	@JoinColumn(name = "resourceId", nullable = false, unique = true)
	private XmpResource resource;
	
	@Column(nullable = false)
	private String filename;
	
	@Column(nullable = true)
	private Serializable metadata;
	
	@Column(nullable = false)
	private String key;

	/** Size in bytes of the source file */
	@Column
	private long size = 0;

	/** Display name of the source file */
	@Column
	private String title;
	/** MIME type of the source file */
	@Column
	private String contentType;
	/** Character encoding used for source file */
	@Column
	private String encoding;
	
	private transient final Lock lock = new ReentrantLock();
	
	public Lock getLock() { return lock; }
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public XmpResource getResource() {
		return resource;
	}

	public void setResource(XmpResource resource) {
		this.resource = resource;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String dataFile) {
		this.filename = dataFile;
	}

	public Serializable getMetadata() {
		return metadata;
	}
	public <T extends Serializable> T getMetadata(Class<T> type) {
		return type.cast(metadata);
	}
	public void setMetadata(Serializable infoFile) {
		this.metadata = infoFile;
	}
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	// UTILITY
	
	/** Increases the expiry timer by 2 hours if the cached file were to expire within the next hour. */
	public void prolongExpiry() {
		LocalDateTime expiry = getExpiresAt();
		if(expiry.isBefore(LocalDateTime.now().plusHours(1))) {
			setExpiresAt(expiry.plusHours(2));
		}
	}
}
