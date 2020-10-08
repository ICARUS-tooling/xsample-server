/**
 * 
 */
package de.unistuttgart.xsample;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Markus Gärtner
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "Resource.findAll", query = "SELECT r FROM Resource r ORDER BY r.id"),
	@NamedQuery(name = "Resource.findByFile", query = "SELECT r FROM Resource r WHERE r.file = :file"), 
})
public class Resource {

	@Column
	private Long file;

	@Id
	@GeneratedValue
	private Long id;

	public Long getFile() {
		return file;
	}

	public void setFile(Long file) {
		this.file = file;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public String toString() { return String.format("Resource@[id=%d, file=%d]", id, file); }
}
