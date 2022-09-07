package de.unistuttgart.xsample.pages.shared;

import java.io.Serializable;
import java.util.List;

import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpResource;

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
	/** DB wrapper for the source */
	private XmpResource resource;
	/** Used up quota */
	private XmpExcerpt quota;
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
	
	public String encode() {
		return isEmpty() ? "" : XmpFragment.encodeAll(fragments);
	}
	
	public boolean isEmpty() { return fragments==null || fragments.isEmpty(); }
	
	/** Reset the fragment data on this excerpt */
	public void clear() {
		fragments = null;
	}
	
	public XmpResource getResource() { return resource; }
	public void setResource(XmpResource xmpResource) { this.resource = xmpResource; }
	
	public XmpExcerpt getQuota() { return quota; }
	public void setQuota(XmpExcerpt quota) { this.quota = quota; }
	
	public long getLimit() { return limit; }
	public void setLimit(long limit) { this.limit = limit; }
	
	@Override
	public String toString() {
		return String.format("%s@[corpus=%s, limit=%d, fragments=%s]", getClass().getSimpleName(), corpusId, limit, fragments);
	}
}