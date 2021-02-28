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
import static de.unistuttgart.xsample.util.XSampleUtils._long;
import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.encrypt;
import static de.unistuttgart.xsample.util.XSampleUtils.makeKey;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.unistuttgart.xsample.XsampleServices.Key;
import de.unistuttgart.xsample.XsampleSession;
import de.unistuttgart.xsample.ct.EmptyResourceException;
import de.unistuttgart.xsample.ct.ExcerptHandler;
import de.unistuttgart.xsample.ct.ExcerptHandlers;
import de.unistuttgart.xsample.ct.FileInfo;
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.Dataverse;
import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.dv.DataverseUser;
import de.unistuttgart.xsample.dv.DvFileMetadata;
import de.unistuttgart.xsample.dv.DvResult;
import de.unistuttgart.xsample.dv.DvUserInfo;
import de.unistuttgart.xsample.dv.Excerpt;
import de.unistuttgart.xsample.dv.Fragment;
import de.unistuttgart.xsample.dv.Resource;
import de.unistuttgart.xsample.mf.XsampleManifest;
import de.unistuttgart.xsample.mf.XsampleManifest.SourceFile;
import de.unistuttgart.xsample.mf.XsampleManifest.SourceType;
import de.unistuttgart.xsample.mf.XsampleManifest.Span;
import de.unistuttgart.xsample.mf.XsampleManifest.SpanType;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.query.QueryPage;
import de.unistuttgart.xsample.pages.shared.ExcerptType;
import de.unistuttgart.xsample.pages.shared.XsampleInputData;
import de.unistuttgart.xsample.pages.shared.XsampleWorkflow.Flag;
import de.unistuttgart.xsample.pages.shared.XsampleWorkflow.Status;
import de.unistuttgart.xsample.pages.slice.SlicePage;
import de.unistuttgart.xsample.util.BundleUtil;
import de.unistuttgart.xsample.util.CountingSplitStream;
import de.unistuttgart.xsample.util.Property;
import de.unistuttgart.xsample.util.XSampleUtils;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Markus Gärtner
 *
 */
@Named
@RequestScoped
public class WelcomePage extends XsamplePage {
	
	public static final String PAGE = "welcome";
	
	private static final Logger logger = Logger.getLogger(WelcomePage.class.getCanonicalName());
	
	@Inject
	XsampleSession session;
	
	@Inject
	XsampleInputData inputData;
	
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
		final long begin = excerptData.getStaticExcerptBegin();
		final long end = excerptData.getStaticExcerptEnd();
		
		final long segments = excerptData.getFileInfo().getSegments();
		final long first;
		final long last;
		if(excerptData.isStaticExcerptFixed()) {
			first = Math.max(1, begin);
			last = Math.min(segments, end);
		} else {
			first = Math.max(1, (long) (segments / 100.0 * begin));
			last = Math.min(segments, first + (long) (segments / 100.0 * (end-begin+1)) - 1);
		}
		List<Fragment> excerpt = Arrays.asList(Fragment.of(first, last));
		long limit = (long)(segments * services.getDoubleSetting(Key.ExcerptLimit));
		long usedUpSlots = XSampleUtils.combinedSize(excerpt, excerptData.getQuota().getFragments());
		if(usedUpSlots > limit) {
			String text = BundleUtil.format("welcome.msg.staticExcerptExceedsQuota", 
					_long(begin), _long(end), excerptData.getFileInfo().getTitle());
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
	public void next() {
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
		
		forward(page);
	}

	@Override
	public void back() { throw new UnsupportedOperationException("This is the landing page..."); }

	
	private static class Context {
		DataverseClient client;
	}
	
	@Transactional
	public void verifyInput() {		
		Status status = Status.FILE_VALID;
		
		Context context = new Context();
		
		for(Step step : steps()) {
			try {
				if(!step.execute(context)) {
					status = Status.FILE_INVALID;
					break;
				}
				message(FacesMessage.SEVERITY_INFO, step.key());
			} catch(RuntimeException e) {
				status = Status.INTERNAL_ERROR;
				logger.log(Level.SEVERE, "Failed analysis step: "+step.label(), e);
				message(FacesMessage.SEVERITY_FATAL,"welcome.msg.internalError",
						step.label(), e.getMessage());
				break;
			}
		}
		
		workflow.setStatus(status);
	}

	private static void message(Severity severity, String key, Object...args) {
		String text = BundleUtil.format(key, args);
		FacesMessage msg = new FacesMessage(severity, text, null);
		FacesContext.getCurrentInstance().addMessage("initMsgs", msg);
	}

	static class Step {
		final String key;
		final Predicate<Context> task;
		
		Step(Predicate<Context> taks, String key) {
			this.task = requireNonNull(taks);
			this.key = requireNonNull(key);
		}
		
		boolean execute(Context context) { return task.test(context); }		
		String key() { return key; }	
		String label() { return BundleUtil.get(key); }
	}
	
	private Step[] steps() {
		List<Step> steps = new ArrayList<>();
		
		if(session.isDebug()) {
//			steps.add(new Step(this::initDebug, "welcome.step.initDebug"));
		}
		
		Collections.addAll(steps, 
				new Step(this::checkParams, "welcome.step.checkParams"),
				new Step(this::checkDataverse, "welcome.step.checkDataverse"),
				new Step(this::checkUser, "welcome.step.checkUser"),
//				new Step(this::checkFileType, "welcome.step.checkFileType"),
				new Step(this::loadManifest, "welcome.step.loadManifest"),
				new Step(this::loadFile, "welcome.step.loadFile"),
				new Step(this::validateManifest, "welcome.step.validateManifest"),
				new Step(this::checkFileSize, "welcome.step.checkFileSize"),
				new Step(this::checkQuota, "welcome.step.checkQuota"));
		
		return steps.toArray(new Step[steps.size()]);
	}
	
	private void ioErrorMessage(IOException e) {
		message(FacesMessage.SEVERITY_ERROR, "welcome.msg.dataverseIoError", e.getMessage());
	}
	
	private void httpErrorMessage(Response<?> response) {
		// Determine what kind of error we are dealing with
		String key = "welcome.msg.responseUnknown";
		if(response.code()>=500) {
			key = "welcome.msg.response500";
		} else if(response.code()>=400) {
			key = "welcome.msg.response400";
		}
		
		// Fetch complete error message from remote
		String msg;
		try(ResponseBody body = response.errorBody()) {
			msg = body.string();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to fetch remote error message", e);
			ioErrorMessage(e);
			return;
		}
		
		// Log full error message
		logger.severe(String.format("Error response from Dataverse (code %d): %s", _int(response.code()), msg));
		
		// Display proper error message to user
		message(FacesMessage.SEVERITY_ERROR, key, _int(response.code()));
	}
	
	/** Verify obligatory URL parameters */
	boolean checkParams(Context context) {
		boolean result = true;
		
		result &= checkNotNull(inputData.getFile(), "welcome.msg.noFile");
		result &= checkNotNull(inputData.getSite(), "welcome.msg.noSite");
		result &= checkNotNull(inputData.getKey(), "welcome.msg.noKey");
	
		return result;
	}
			
	private boolean checkNotNull(Object value, String errorKey) {
		boolean valid = value!=null;
		if(!valid) {
			message(FacesMessage.SEVERITY_ERROR, errorKey);
		}
		return valid;
	}

	/** Verify that the dataverse server is registered and we have a master API key */
	boolean checkDataverse(Context context) {
		// Check that we know the given Dataverse
		final String address = inputData.getSite();
		final Optional<Dataverse> dataverse = services.findDataverseByUrl(address);
		if(!dataverse.isPresent() || dataverse.get().getMasterKey()==null) {
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.unknownDataverse", address);
			return false;
		}				
		excerptData.setServer(dataverse.get());
		
		// Now check that it is a valid URL and initialize our client wrapper
		URL url;
		try {
			url = new URL(address);
		} catch(MalformedURLException e) {
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.invalidDataverseUrl", address);
			return false;
		}
		context.client = DataverseClient.forServer(url);
		
		return true;
	}

	/** Verify that the provided API key belongs to a real Dataverse user */
	boolean checkUser(Context context) {
		final String key = inputData.getKey();
		final DataverseClient client = requireNonNull(context.client);
		
		Response<DvResult<DvUserInfo>> response;
		try {
			response = client.getUserInfo(key).execute();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "I/O error while fetching user info", e);
			ioErrorMessage(e);
			return false;
		}
		if(!response.isSuccessful()) {
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.userInfoFailed");
			logger.severe(String.format("Failed to fetch user info for key '%s': code=%d body='%s'", 
					key, _int(response.code()), response.errorBody()));
			return false;
		}
		
		final DvResult<DvUserInfo> info = response.body();
		if(!info.isOk() || info.getData()==null) {
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.userInfoFailed");
			logger.severe(String.format("Received empty user info for key '%s': status=%d", 
					key, info.getStatus()));
			return false;
		}
		final Dataverse server = excerptData.getServer();
		final DataverseUser user = services.findDataverseUser(server, info.getData().getPersistentUserId());
		excerptData.setDataverseUser(user);
		
		return true;
	}
		
	/** Load file metadata and check that it is a type we can handle */
	@Deprecated
	boolean checkFileType(Context context) {
		final long fileId = inputData.getFile().longValue();
		final String key = excerptData.getServer().getMasterKey();
		final DataverseClient client = requireNonNull(context.client);
		
		Response<DvFileMetadata> response;
		try {
			//TODO change to regular metadata method once we solved the API TOKEN issue
			response = client.getDraftFileMetadata(fileId, key).execute();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "I/O error while fetching file metadata", e);
			ioErrorMessage(e);
			return false;
		}
		if(!response.isSuccessful()) {
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.fileInfoFailed");
			logger.severe(String.format("Failed to fetch file info for id '%s': code=%d body='%s'", 
					_long(fileId), _int(response.code()), response.errorBody()));
			return false;
		}
		
		final DvFileMetadata metadata = response.body();
		final SourceType inputType = SourceType.forFileName(metadata.getLabel());
		if(inputType==null) { //TODO need a better check then null here?!
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.incompatibleResource");
			return false;
		}
//		excerptData.setInputType(inputType);
		
		return true;
	}
		
	/** Load the root manifest */
	boolean loadManifest(Context context) {
		final long fileId = inputData.getFile().longValue();
		final String key = inputData.getKey();
		final DataverseClient client = requireNonNull(context.client);
		
		final Call<ResponseBody> request = client.downloadFile(fileId, key);
	
		Response<ResponseBody> response;		
		try {
			response = request.execute();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to fetch resource", e);
			ioErrorMessage(e);
			return false;
		}
		
		if(!response.isSuccessful()) {
			httpErrorMessage(response);
			return false;
		}
		
		// Successfully opened and accessed data, now load content
		try(ResponseBody body = response.body()) {
			final MediaType mediaType = body.contentType();
			if(mediaType==null) {
				logger.log(Level.SEVERE, "Failed to obtain media type");
				message(FacesMessage.SEVERITY_ERROR,"welcome.msg.noMediaType");
				return false;
			}
			
			XsampleManifest manifest;
			
			try(Reader reader = body.charStream()) {
				manifest = XsampleManifest.parse(reader);
			}
			
			// Update manifest in current setup
			excerptData.setManifest(manifest);
		} catch (IOException | JsonIOException e) {
			logger.log(Level.SEVERE, "Failed to load manifest content", e);
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.loadFailed");
			return false;
		} catch (JsonSyntaxException e) {
			logger.log(Level.SEVERE, "Malformed manifest file: "+request.request().url(), e);
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.malformedManifest");
			return false;
		}
		
		return true;
	}

	private static final String TOKEN = "([a-zA-Z0-9-!#$%&'*+.^_`{|}~]+)";
	private static final String QUOTED = "\"([^\"]*)\"";
	private final Pattern PARAMETER = Pattern
			.compile(";\\s*(?:" + TOKEN + "=(?:" + TOKEN + "|" + QUOTED + "))?");
	
	private String extractName(String mediaType) {
		final Matcher m = PARAMETER.matcher(mediaType);
		while(m.find()) {
			String name = m.group(1);
			if(name==null) {
				continue;
			}
			String value = m.group(2);
			if(value==null) {
				value = m.group(3);
			}
			if(value!=null && "name".equals(name)) {
				return value;
			}
		}
		return null;
	}

	/** Load the entire source file */
	boolean loadFile(Context context) {
		final SourceFile sourceFile = excerptData.getManifest().getTarget();
		final String key = excerptData.getServer().getMasterKey();
		final DataverseClient client = requireNonNull(context.client);
		
		final Call<ResponseBody> request;
		if(sourceFile.getId()!=null) {
			request = client.downloadFile(sourceFile.getId().longValue(), key);
		} else if(sourceFile.getPersistentId()!=null) {
			request = client.downloadFile(sourceFile.getPersistentId(), key);
		} else {
			message(FacesMessage.SEVERITY_FATAL, "welcome.msg.missingFileId", "manifest");
			return false;
		}
	
		Response<ResponseBody> response;		
		try {
			response = request.execute();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to fetch resource", e);
			ioErrorMessage(e);
			return false;
		}
		
		if(!response.isSuccessful()) {
			httpErrorMessage(response);
			return false;
		}
		
		// Successfully opened and accessed data, now load content
		try(ResponseBody body = response.body()) {
			final MediaType mediaType = body.contentType();
			if(mediaType==null) {
				logger.log(Level.SEVERE, "Failed to obtain media type");
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.noMediaType");
				return false;
			}
			
			final FileInfo fileInfo = new FileInfo();
			fileInfo.setContentType(mediaType.type()+"/"+mediaType.subtype());
			//TODO strictly speaking we should handle an unset or incompatible charset here!!
			fileInfo.setEncoding(mediaType.charset(StandardCharsets.UTF_8));
			fileInfo.setTitle(extractName(mediaType.toString()));

			final SourceType sourceType = excerptData.getManifest().getTarget().getSourceType();
			final ExcerptHandler handler = ExcerptHandlers.forSourceType(sourceType);
			
			// Ensure an encrypted copy of the resource
			final Path tempFile = Files.createTempFile("xsample_", ".tmp");
			final SecretKey secret = makeKey();
			long size = 0;
			try(OutputStream out = new CipherOutputStream(buffer(Files.newOutputStream(tempFile)), encrypt(secret));
					CountingSplitStream in = new CountingSplitStream(body.byteStream(), out)) {
				// Let handler do the actual work. Any acquired information is stored in fileInfo
				handler.analyze(fileInfo, in);
				size = in.getCount();
			}
			
			// If everything went well, finally complete
			fileInfo.setSize(size);
			fileInfo.setTempFile(tempFile);
			fileInfo.setKey(secret);
			
			// Update info in current config
			excerptData.setFileInfo(fileInfo);
			excerptData.setExcerptHandler(handler);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to load file content", e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.loadFailed");
			return false;
		} catch (UnsupportedContentTypeException e) {
			logger.log(Level.SEVERE, "Content type of file not supported", e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.unsupportedType");
			return false;
		} catch (EmptyResourceException e) {
			logger.log(Level.SEVERE, "Source file empty", e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.emptyResource");
			return false;
		} catch (GeneralSecurityException e) {
			logger.log(Level.SEVERE, "Failed to prepare cipher", e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.cipherPreparation");
			return false;
		}
		
		return true;
	}	
	
	/** Verify manifest integrity - needs file info for loaded target resource!! */
	boolean validateManifest(Context context) {
		final XsampleManifest manifest = excerptData.getManifest();
		
		long begin = 0, end = 0;
		boolean fixed = false;
		
		// Check static excerpt declaratio nand translate into proper bounded range data
		if(manifest.hasStaticExcerpt()) {
			Span staticExcerpt = manifest.getStaticExcerpt();
			begin = staticExcerpt.getBegin();
			if(begin==-1) begin = 0;
			end = staticExcerpt.getEnd();
			if(end==-1) end = 100;
			
			final long size = end-begin+1;
			final long limit;
			fixed = staticExcerpt.getSpanType()==SpanType.FIXED;
			
			if(fixed) {
				long segments = excerptData.getFileInfo().getSegments();
				limit = (long) (services.getDoubleSetting(Key.ExcerptLimit) * segments);
			} else {
				limit = (long) (services.getDoubleSetting(Key.ExcerptLimit) * 100);
			}
			
			if(size > limit) {
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.manifestExceedsQuota", 
						_long(size), _double(limit));
				return false;
			}
		} else {
			begin = 0;
			end = services.getIntSetting(Key.DefaultStaticExcerpt);
		}
		
		// Compute label once and store in excerpt info
		String label;
		if(fixed) {
			final ExcerptHandler handler = excerptData.getExcerptHandler();
			final long size = end-begin+1; 
			if(size==1) {
				label = handler.getSegmentLabel(false) + " " + begin;
			} else {
				label = String.format("%s %d - %d", handler.getSegmentLabel(true), _long(begin), _long(end));
			}
		} else {
			label = String.format("%d - %d%%", _long(begin), _long(end));
		}

		excerptData.setStaticExcerptBegin(begin);
		excerptData.setStaticExcerptEnd(end);
		excerptData.setStaticExcerptLabel(label);
		
		return true;
	}

	boolean checkFileSize(Context context) {
		final long segments = excerptData.getFileInfo().getSegments();
		final long threshold = services.getLongSetting(Key.SmallFileLimit);
		excerptData.setSmallFile(segments<=threshold);
		return true;
	}
		
	/** Verify that user still has quota left on the designated resource */
	boolean checkQuota(Context context) {
		final Long fileId = inputData.getFile();
		final DataverseUser user = excerptData.getDataverseUser();
		final Dataverse server = excerptData.getServer();
		final Resource resource = services.findResource(server, fileId);
		final Excerpt excerpt = services.findQuota(user, resource);
		
		if(!excerptData.isSmallFile() && !excerpt.isEmpty()) {
			long quota = excerpt.size();
			long segments = excerptData.getFileInfo().getSegments();
			long limit = (long) (segments * services.getDoubleSetting(Key.ExcerptLimit));
			
			if(quota>=limit) {
				logger.log(Level.SEVERE, String.format("Quota of %d used up on resource %s by user %s", 
						_long(limit), resource, user));
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.quotaExceeded", 
						_long(quota), excerptData.getFileInfo().getTitle());
				
				return false;
			}
		}
		
		excerptData.setResource(resource);
		excerptData.setQuota(excerpt);
		
		return true;
	}
}