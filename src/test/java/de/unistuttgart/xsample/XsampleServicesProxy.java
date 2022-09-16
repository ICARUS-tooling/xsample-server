/**
 * 
 */
package de.unistuttgart.xsample;

import static de.unistuttgart.xsample.util.XSampleUtils._long;

import java.util.List;
import java.util.Optional;

import de.unistuttgart.xsample.dv.UserId;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class XsampleServicesProxy extends XsampleServices {
	
	public void clear() {
		resources.clear();
	}
	
	@Override
	public <T> T update(T obj) {
		// TODO Auto-generated method stub
		return super.update(obj);
	}

	@Override
	public <T> void store(T obj) {
		// TODO Auto-generated method stub
		super.store(obj);
	}

	@Override
	public <T> void delete(T obj) {
		// TODO Auto-generated method stub
		super.delete(obj);
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		super.sync();
	}

	@Override
	public String getSetting(Key key) {
		// TODO Auto-generated method stub
		return super.getSetting(key);
	}

	@Override
	public int getIntSetting(Key key) {
		// TODO Auto-generated method stub
		return super.getIntSetting(key);
	}

	@Override
	public long getLongSetting(Key key) {
		// TODO Auto-generated method stub
		return super.getLongSetting(key);
	}

	@Override
	public double getDoubleSetting(Key key) {
		// TODO Auto-generated method stub
		return super.getDoubleSetting(key);
	}

	@Override
	public boolean getBooleanSetting(Key key) {
		// TODO Auto-generated method stub
		return super.getBooleanSetting(key);
	}
	
	private Long2ObjectMap<XmpResource> resources = new Long2ObjectOpenHashMap<>();

	@Override
	public XmpResource findResource(XmpDataverse dataverse, Long file) {
		XmpResource resource = resources.get(file.longValue());
		if(resource==null) {
			resource = new XmpResource();
			resource.setDataverse(dataverse);
			resource.setId(_long(resources.size()));
			resource.setFile(file);
			
			resources.put(file.longValue(), resource);
		}
		return resource;
	}

	@Override
	public Optional<XmpDataverse> findDataverseByUrl(String url) {
		if(XSampleTestUtils.DATAVERSE.getUrl().equals(url)) {
			return Optional.of(XSampleTestUtils.DATAVERSE);
		}
		return Optional.empty();
	}

	@Override
	public XmpDataverseUser findDataverseUser(XmpDataverse dataverse, String userId) {
		if(dataverse==XSampleTestUtils.DATAVERSE 
				&& XSampleTestUtils.USER.getId().getPersistentUserId().equals(userId)) {
			return XSampleTestUtils.USER;
		}
		
		XmpDataverseUser user = new XmpDataverseUser();
		user.setDataverse(dataverse);
		user.setId(new UserId(dataverse.getUrl(), userId));
		return user;
	}
	
	private Long2ObjectMap<XmpExcerpt> quotas = new Long2ObjectOpenHashMap<>();

	@Override
	public XmpExcerpt findQuota(XmpDataverseUser user, XmpResource resource) {
		long file = resource.getFile().longValue();
		XmpExcerpt quota = quotas.get(file);
		if(quota==null) {
			quota = new XmpExcerpt();
			quota.setDataverseUser(user);
			quota.setId(_long(quotas.size()));
			quota.setResource(resource);
			quotas.put(file, quota);
		}
		return quota;
	}

	@Override
	public List<XmpLocalCopy> findExpiredCopies() {
		// TODO Auto-generated method stub
		return super.findExpiredCopies();
	}

	@Override
	public Optional<XmpLocalCopy> findCopy(XmpResource resource) {
		// TODO Auto-generated method stub
		return super.findCopy(resource);
	}

	@Override
	public Optional<XmpLocalCopy> findCopy(String filename) {
		// TODO Auto-generated method stub
		return super.findCopy(filename);
	}

	@Override
	public XmpFileInfo findFileInfo(XmpResource resource) {
		// TODO Auto-generated method stub
		return super.findFileInfo(resource);
	}

}
