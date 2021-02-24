/*
 * XSample Server
 * Copyright (C) 2020-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.unistuttgart.xsample.pages.welcome;

import static de.unistuttgart.xsample.util.XSampleUtils._double;
import static de.unistuttgart.xsample.util.XSampleUtils._int;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.mf.XsampleManifest.SourceType;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.query.QueryPage;
import de.unistuttgart.xsample.pages.shared.ExcerptType;
import de.unistuttgart.xsample.pages.shared.XsampleWorkflow.Flag;
import de.unistuttgart.xsample.pages.slice.SlicePage;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.Property;
import de.unistuttgart.xsample.util.XSampleUtils;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class WelcomePage extends XsamplePage {
	
	public static final String PAGE = "welcome";
	
	private static final Logger logger = Logger.getLogger(WelcomePage.class.getCanonicalName());
	
	/** Returns text for current status or empty string */
	public String getStatusInfo() {
		if(isShowOutline()) {
			return "";
		}
		
		return workflow.getStatus().getLabel();
	}
	
	/** Indicate that the outline for valid files should be shown */
	public boolean isShowOutline() {
		return workflow.getStatus().isFlagSet(Flag.FILE_VALID);
	}
	
	public boolean isHasAnnotations() {
		return isShowOutline() && excerptData.getManifest().hasManifests();
	}
	
	/** Indicate that the choice for excerpt selection should be shown */
	public boolean isShowExcerptSelection() {
		return isShowOutline() && !excerptData.isSmallFile();
	}
	
	/** Produce table data for current main file */
	public List<Property> getFileProperties() {
		if(!isShowOutline()) {
			return Collections.emptyList();
		}
		
		List<Property> properties = new ArrayList<>();
		
		SourceType sourceType = excerptData.getManifest().getTarget().getSourceType();
		
		properties.add(new Property("type", sourceType.name()));
		
		FileInfo info = excerptData.getFileInfo();
		
		properties.add(new Property("name", info.getTitle()));
		properties.add(new Property("content-type", info.getContentType()));
		properties.add(new Property("encoding", info.getEncoding().displayName()));
		properties.add(new Property("size", XSampleUtils.formatSize(info.getSize())));
		properties.add(new Property("segments", String.valueOf(info.getSegments())));
		
		Excerpt quota = excerptData.getQuota();
		if(!quota.isEmpty()) {
			long used = quota.size();
			double percent = (double) used / info.getSegments() * 100.0;
			properties.add(new Property("quota", String.valueOf(used)));
			properties.add(new Property("quota-ratio", String.format("%.2f%%", _double(percent))));
		}
		
		return properties;
	}
	
//	/** Label appendix for the static excerpt option */
//	public String getStaticExcerptRange() {
//		if(!isShowOutline()) {
//			return "???";
//		}
//
//		final int begin = excerptData.getStaticExcerptBegin();
//		final int end = excerptData.getStaticExcerptEnd();
//		
//		return String.format("(%d-%d%%)", _int(begin), _int(end));
//	}
	
	boolean prepareStaticExcerpt() {
		final int begin = excerptData.getStaticExcerptBegin();
		final int end = excerptData.getStaticExcerptEnd();
		
		long segments = excerptData.getFileInfo().getSegments();
		long first = Math.max(1, (long) (segments / 100.0 * begin));
		long last = Math.min(segments, first + (long) (segments / 100.0 * (end-begin+1)) - 1);
		List<Fragment> excerpt = Arrays.asList(Fragment.of(first, last));
		long limit = (long)(segments * services.getDoubleSetting(Key.ExcerptLimit));
		long usedUpSlots = XSampleUtils.combinedSize(excerpt, excerptData.getQuota().getFragments());
		if(usedUpSlots > limit) {
			String text = BundleUtil.format("welcome.msg.staticExcerptExceedsQuota", 
					_int(begin), _int(end), excerptData.getFileInfo().getTitle());
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
			FacesContext.getCurrentInstance().addMessage("navMsgs", msg);
			return false;			
		}
		
		excerptData.setExcerpt(excerpt);
		
		return true;
	}
	
	void prepareFullDownload() {
		long segments = excerptData.getFileInfo().getSegments();
		List<Fragment> excerpt = Arrays.asList(Fragment.of(1, segments));
		excerptData.setExcerpt(excerpt);
	}
	
	/** Callback for button to continue workflow */
	public void continueWorkflow() {

		String oldPage = workflow.getPage();
		String page = null;
		
		if(excerptData.isSmallFile()) {
			// Small file -> full download
			prepareFullDownload();
			page = DownloadPage.PAGE;
		} else {
			// Big file -> Delegate to correct page
			ExcerptType excerptType = excerptData.getExcerptType();
			switch (excerptType) {
			case STATIC: {
				page = prepareStaticExcerpt() ? DownloadPage.PAGE : PAGE;
			} break;
			case SLICE: page = SlicePage.PAGE; break;
			case QUERY: page = QueryPage.PAGE; break;
			default:
				break;
			}
			
			if(page==null) {
				logger.severe("Unknown page result from routing in welcome page for type: "+excerptType);
				String text = BundleUtil.format("welcome.msg.unknownPage", excerptType);
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
				FacesContext.getCurrentInstance().addMessage("navMsgs", msg);
				return;
			}
		}

//		logger.fine("Navigating to subpage "+page);
		
		// Only cause a "page change" if page actually changed
		if(!Objects.equals(oldPage, page)) {
			workflow.setPage(page);
			PrimeFaces.current().ajax().update(":content");
		}
	}
}
