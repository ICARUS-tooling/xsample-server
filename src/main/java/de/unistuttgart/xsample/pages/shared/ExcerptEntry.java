package de.unistuttgart.xsample.pages.shared;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.io.Serializable;
import java.util.List;

import de.unistuttgart.xsample.dv.XmpFragment;

/**
 * 
 * @author Markus Gärtner
 *
 */
public class ExcerptEntry implements Serializable {

	private static final long serialVersionUID = 3111407155877405794L;
	
	/** Corpus or subcorpus to extract from */
	private String corpusId;
	/** Designated output */
	private List<XmpFragment> fragments;
	/** Limit within the associated corpus */
	private long limit;
	
	public String getCorpusId() {
		return corpusId;
	}
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}
	public List<XmpFragment> getFragments() {
		return fragments;
	}
	public void setFragments(List<XmpFragment> fragments) {
		this.fragments = fragments;
	}
	
	public boolean isEmpty() { return fragments==null || fragments.isEmpty(); }
	
	/** Reset the fragment data on this excerpt */
	public void clear() {
		fragments = null;
	}
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	@Override
	public String toString() {
		return String.format("%s@[corpus=%s, limit=%d, fragments=%s]", getClass().getSimpleName(), corpusId, _long(limit), fragments);
	}
}