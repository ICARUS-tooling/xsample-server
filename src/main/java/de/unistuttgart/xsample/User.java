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
@Entity(name = "Users")
@NamedQueries({ 
	@NamedQuery(name = "User.findAll", query = "SELECT u FROM Users u ORDER BY u.id"),
	@NamedQuery(name = "User.findByKey", query = "SELECT u FROM Users u WHERE u.key = :key"), 
})
public class User {

	@Column(nullable = false, length = 36, unique = true)
	private String key;

	@Id
	@GeneratedValue
	private Long id;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Override
	public String toString() { return String.format("User@[id=%d, key=%s]", id, key); }
}
