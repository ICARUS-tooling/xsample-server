/**
 * 
 */
package de.unistuttgart.xsample.pages;

import javax.inject.Inject;

import org.primefaces.PrimeFaces;

import de.unistuttgart.xsample.XsampleServices;
import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData;
import de.unistuttgart.xsample.pages.shared.XsampleWorkflow;

/**
 * @author Markus Gärtner
 *
 */
public class XsamplePage {
	
	@Inject
	protected XsampleWorkflow workflow;
	
	@Inject
	protected XsampleServices services;
	
	@Inject
	protected XsampleExcerptData excerptData;
	
	protected void initQuota(ExcerptUtilityData data) {
		final long range = excerptData.getFileInfo().getSegments();
		data.setRange(range);
		data.setLimit((long) (range * services.getDoubleSetting(Key.ExcerptLimit)));
		
		Excerpt quota = excerptData.getQuota();
		if(!quota.isEmpty()) {
			data.setQuota(Fragment.encodeAll(quota.getFragments()));
		}
	}
	
	public void back() {
		if(workflow.back()) {
			updatePage();
		}
	}
	
	protected void updatePage() {
		PrimeFaces.current().ajax().update(":content");
	}
	
	protected void forward(String page) {
		// Only cause a "page change" if page actually changed
		if(workflow.forward(page)) {
			updatePage();
		}
	}
}
