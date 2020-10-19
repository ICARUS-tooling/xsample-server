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
	@NamedQuery(name = "Dataverse.findAll", query = "SELECT d FROM Dataverse d ORDER BY d.id"),
	@NamedQuery(name = "Dataverse.findByUrl", query = "SELECT d FROM Dataverse d WHERE d.url = :url"), 
})
public class Dataverse {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true)
	private String url;

	public Long getId() { return id; }

	public void setId(Long id) { this.id = id; }

	public String getUrl() { return url; }

	public void setUrl(String url) { this.url = url; }

	@Override
	public String toString() { return String.format("Dataverse@[id=%d, url=%s]", id, url); }
}
