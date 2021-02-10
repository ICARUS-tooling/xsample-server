/**
 * 
 */
package de.unistuttgart.xsample;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class SlicePage {
	
	public static final String PAGE = "slice";

	@Inject
	XsampleSliceData sliceData;
	
	@Inject
	XsampleWorkflow workflow;
	
	@Inject
	XsampleExcerptInput excerptInput;
	
	@Inject
	XsampleServices services;
	
	public void init() {
		final long range = excerptInput.getFileInfo().getSegments();
		sliceData.setBegin(1);
		sliceData.setEnd(Math.min(15, range));
		sliceData.setRange(range);
		sliceData.setLimit((long) (range * services.getDoubleSetting(Key.ExcerptLimit)));
		
		Excerpt quota = excerptInput.getQuota();
		if(!quota.isEmpty()) {
			sliceData.setQuota(Fragment.encodeAll(quota.getFragments()));
		}
	}

	/** Callback for button to continue workflow */
	public void onContinue() {
		//TODO
		
		workflow.setPage(DownloadPage.PAGE);
	}
}
