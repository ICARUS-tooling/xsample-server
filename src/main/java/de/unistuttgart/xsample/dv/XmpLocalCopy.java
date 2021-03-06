/**
 * 
 */
package de.unistuttgart.xsample.dv;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.crypto.SecretKey;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

/**
 * @author Markus Gärtner
 *
 */
@Entity(name = XmpLocalCopy.TABLE_NAME)
@NamedQueries({
	@NamedQuery(name = "LocalCopy.findByResource", query = "SELECT c FROM LocalCopy c WHERE c.resource = :resource"), 
	@NamedQuery(name = "LocalCopy.findByFilename", query = "SELECT c FROM LocalCopy c WHERE c.filename = :filename"), 
	@NamedQuery(name = "LocalCopy.findExpired", query = "SELECT c FROM LocalCopy c WHERE c.expiresAt < :timestamp"), 
})
public class XmpLocalCopy {
	
    public static final String TABLE_NAME= "LocalCopy";

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, columnDefinition = "TIMESTAMP")
	private LocalDateTime expiresAt;
	
	@Column(nullable = false, unique = true)
	@OneToOne
	private XmpResource resource;
	
	@Column(nullable = false)
	private String filename;
	
	@Column(nullable = false)
	private SecretKey key;
	
	private transient final ReadWriteLock lock = new ReentrantReadWriteLock();

	public void lockWrite() { lock.writeLock().lock(); }
	public void unlockWrite() { lock.writeLock().unlock(); }
	public void lockRead() { lock.readLock().lock(); }
	public void unlockRead() { lock.readLock().unlock(); }
	
	public boolean tryLockWrite(long amount, TimeUnit unit) throws InterruptedException { 
		return lock.writeLock().tryLock(amount, unit); 
	}
	
	public boolean tryLockRead(long amount, TimeUnit unit) throws InterruptedException { 
		return lock.readLock().tryLock(amount, unit); 
	}
	
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

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public SecretKey getKey() {
		return key;
	}

	public void setKey(SecretKey key) {
		this.key = key;
	}
}
