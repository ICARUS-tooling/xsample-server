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
	@NamedQuery(name = "LocalCopy.findByDataFile", query = "SELECT c FROM LocalCopy c WHERE c.dataFile = :filename"), 
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
	private String dataFile;
	
	@Column(nullable = false)
	private String infoFile;
	
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

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public String getInfoFile() {
		return infoFile;
	}
	public void setInfoFile(String infoFile) {
		this.infoFile = infoFile;
	}
	public SecretKey getKey() {
		return key;
	}

	public void setKey(SecretKey key) {
		this.key = key;
	}
}
