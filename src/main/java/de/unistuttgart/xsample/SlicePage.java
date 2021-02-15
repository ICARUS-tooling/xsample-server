/**
 * 
 */
package de.unistuttgart.xsample;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Messages;

import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.XSampleUtils;

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
		List<Fragment> excerpt = Arrays.asList(Fragment.of(
				sliceData.getBegin()-1, sliceData.getEnd()-1));
		List<Fragment> quota = excerptData.getQuota().getFragments();
		
		/* The following issue should never occur, since we do the same
		 * validation on the client side to enable/disable the button.
		 * We need this additional sanity check to defend against bugs 
		 * or tampering with the JS code on the client side!
		 */
		long usedUpSlots = XSampleUtils.combinedSize(excerpt, quota);
		if(usedUpSlots>sliceData.getLimit()) {
			Messages.addError("navMsg", BundleUtil.get("slice.msg.quotaExceeded"), 
					_long(usedUpSlots), _long(sliceData.getLimit()));
			return;
		}
		
		// Everything's fine, continue the workflow
		excerptData.setExcerpt(excerpt);
		workflow.setPage(DownloadPage.PAGE);
	}
	
	public boolean isShowQuota() {
		return !excerptData.getQuota().isEmpty();
	}
	
	public long getQuotaSize() {
		return excerptData.getQuota().size();
	}
	
	public double getQuotaPercent() {
		return (double)getQuotaSize() / sliceData.getRange() * 100.0;
	}
}
