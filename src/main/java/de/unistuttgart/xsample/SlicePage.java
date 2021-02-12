/**
 * 
 */
package de.unistuttgart.xsample;

import java.util.Arrays;
import java.util.logging.Logger;

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
	
	private static final Logger logger = Logger.getLogger(SlicePage.class.getCanonicalName());

	@Inject
	XsampleSliceData sliceData;
	
	@Inject
	XsampleWorkflow workflow;
	
	@Inject
	XsampleExcerptData excerptData;
	
	@Inject
	XsampleServices services;
	
	public void init() {
		final long range = excerptData.getFileInfo().getSegments();
		sliceData.setBegin(1);
		sliceData.setEnd(1);
		sliceData.setRange(range);
		sliceData.setLimit((long) (range * services.getDoubleSetting(Key.ExcerptLimit)));
		
		Excerpt quota = excerptData.getQuota();
		if(!quota.isEmpty()) {
			sliceData.setQuota(Fragment.encodeAll(quota.getFragments()));
		}
	}

	/** Callback for button to continue workflow */
	public void onContinue() {
		excerptData.setExcerpt(Arrays.asList(Fragment.of(
				sliceData.getBegin()-1, sliceData.getEnd()-1)));
		
		//TODO sanity check against exceeding the excerpt limit
		
		workflow.setPage(DownloadPage.PAGE);
	}
}
