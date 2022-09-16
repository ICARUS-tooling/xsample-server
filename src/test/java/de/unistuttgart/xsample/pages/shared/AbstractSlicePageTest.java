/**
 * 
 */
package de.unistuttgart.xsample.pages.shared;

import de.unistuttgart.xsample.pages.XsamplePageTest;
import de.unistuttgart.xsample.pages.download.DownloadData;
import de.unistuttgart.xsample.pages.parts.PartsData;
import de.unistuttgart.xsample.pages.slice.SliceData;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractSlicePageTest<P extends AbstractSlicePage> extends XsamplePageTest<P> {

	@Override
	protected void initPage() {
		super.initPage();
		page.corpusData = new CorpusData();
		page.downloadData = new DownloadData();
		page.partData = new PartData();
		page.partsData = new PartsData();
		page.selectionData = new SelectionData();
		page.sliceData = new SliceData();
	}

	@Override
	protected void clearPage() {
		page.corpusData = null;
		page.downloadData = null;
		page.partData = null;
		page.partsData = null;
		page.selectionData = null;
		page.sliceData = null;
		super.clearPage();
	}
	
	protected CorpusData getCorpusData() { return page.corpusData; }
	protected DownloadData getDownloadData() { return page.downloadData; }
	protected PartData getPartData() { return page.partData; }
	protected PartsData getPartsData() { return page.partsData; }
	protected SelectionData getSelectionData() { return page.selectionData; }
	protected SliceData getSliceData() { return page.sliceData; }
}
