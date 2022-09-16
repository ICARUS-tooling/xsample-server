/**
 * 
 */
package de.unistuttgart.xsample.pages;

import static java.util.Objects.requireNonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.XsampleServicesProxy;
import de.unistuttgart.xsample.pages.shared.SharedData;
import de.unistuttgart.xsample.pages.shared.WorkflowData;

/**
 * @author Markus Gärtner
 *
 */
public abstract class XsamplePageTest<P extends XsamplePage> {

	protected P page;
	
	protected XsampleServicesProxy servicesProxy;
	protected XsampleUiProxy uiProxy;
	
	protected abstract P createPage();
	
	@BeforeEach
	void setUp() throws Exception {
		page = requireNonNull(createPage(), "page creation failed");
		servicesProxy = new XsampleServicesProxy();
		uiProxy = new XsampleUiProxy();
		
		initPage();
	}
	
	protected void initPage() {
		page.services = servicesProxy;
		page.ui = uiProxy;
		page.workflow = new WorkflowData();
		page.sharedData = new SharedData();
	}

	@AfterEach
	void tearDown() throws Exception {
		clearPage();
		
		page = null;
		servicesProxy.clear();
		servicesProxy = null;
	}
	
	protected void clearPage() {
		page.services = null;
		page.workflow = null;
		page.sharedData = null;
	}
	
	protected XsampleServices getServices() { return page.services; }
	protected SharedData getSharedData() { return page.sharedData; }
	protected WorkflowData getWorkflow() { return page.workflow; }
}
