/**
 * 
 */
package de.unistuttgart.xsample.dv;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;


/**
 * Primar key wrapper class for {@link User}.
 * @author Markus Gärtner
 *
 */
@Embeddable
public class UserId implements Serializable {
	private static final long serialVersionUID = 6675659702120722414L;
	
	private String dataverseUrl;
	private String persistentUserId;
	
	public UserId() { /* no-op */ }
	
	public UserId(String dataverseUrl, String persistentUserId) {
		setDataverseUrl(dataverseUrl);
		setPersistentUserId(persistentUserId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(dataverseUrl, persistentUserId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof UserId) {
			UserId other = (UserId) obj;
			return Objects.equals(dataverseUrl, other.dataverseUrl)
					&& Objects.equals(persistentUserId, other.persistentUserId);
		}
		return false;
	}
	public String getDataverseUrl() {
		return dataverseUrl;
	}
	public void setDataverseUrl(String dataverse) {
		this.dataverseUrl = dataverse;
	}
	public String getPersistentUserId() {
		return persistentUserId;
	}
	public void setPersistentUserId(String persistentUserId) {
		this.persistentUserId = persistentUserId;
	}
}
