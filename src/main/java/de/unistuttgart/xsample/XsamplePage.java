/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.omnifaces.cdi.Param;

/**
 * @author Markus Gärtner
 *
 */
@Named("XSamplePage")
@SessionScoped
public class XsamplePage implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;
	
	/** The file id as supplied by the external-tools URL */
	@Param private Long file;	
	/** The user's API key as supplied by the external-tools URL */
	@Param private String key;

	private User user;
	private Resource resource;

    @EJB
	private UserService userService;
    @EJB
    private ResourceService resourceService;
    @EJB
    private BundleService bundleService;
	
	@PostConstruct
	public void init() {
		
		if(key!=null) {
			user = userService.findByKey(key);
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					bundleService.get("homepage.noFile.summary"), 
					bundleService.get("homepage.noFile.detail", "key")));
		}
		
		if(file!=null) {
			resource = resourceService.findByFile(file);
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					bundleService.get("homepage.noUser.summary"), 
					bundleService.get("homepage.noUser.detail", "key")));
		}
	}
	
	public Long getFile() { return file; }
	
	public String getKey() { return key; }
	
	public User getUser() { return user; }
	
	public Resource getResource() { return resource; }
	
	private String localeCode = "en";

	public String getLocaleCode() { return localeCode; }

	public void setLocaleCode(String localeCode) { 	this.localeCode = localeCode; }
}
