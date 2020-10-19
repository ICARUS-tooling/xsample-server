/**
 * 
 */
package de.unistuttgart.xsample;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.FlowEvent;

import de.unistuttgart.xsample.XsampleServices.Key;

/**
 * @author Markus Gärtner
 *
 */
@Named
@ViewScoped
public class XsamplePage implements Serializable {

	private static final long serialVersionUID = -7669592681728323609L;
	
	private static final Logger logger = Logger.getLogger(XsamplePage.class.getCanonicalName());

	private XsampleExcerptConfig config = new XsampleExcerptConfig();

	private User user;
	private Resource resource;
	
	private String serverRoute;

    @EJB
	private XsampleServices xsampleServices;
	
	public void init() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.SourceFileParam)))
			.map(Long::valueOf).ifPresent(config::setFile);
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.ApiKeyParam)))
			.ifPresent(config::setKey);
		Optional.ofNullable(params.get(xsampleServices.getSetting(Key.SourceDataverseParam)))
			.ifPresent(config::setSite);
		
		serverRoute = xsampleServices.getSetting(Key.ServerRoute);
		
//		if(config.site==null) {
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					BundleUtil.get("homepage.noSite.summary"), 
//					BundleUtil.get("homepage.noSite.detail", "site")));
//		}
//		
//		if(file==null) {
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					BundleUtil.get("homepage.noUser.summary"), 
//					BundleUtil.get("homepage.noUser.detail", "file")));
//		}
//		
//		if(key==null) {
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//					BundleUtil.get("homepage.noFile.summary"), 
//					BundleUtil.get("homepage.noFile.detail", "key")));
//		}
	} 
	
	public XsampleExcerptConfig getConfig() { return config; }
	public void setConfig(XsampleExcerptConfig config) { this.config = config; }
	
	public String onFlowProcess(FlowEvent event) {
		return event.getNewStep();
	}

	public User getUser() { return user; }
	
	public Resource getResource() { return resource; }
	
	public String getServerRoute() { return serverRoute; }
}
