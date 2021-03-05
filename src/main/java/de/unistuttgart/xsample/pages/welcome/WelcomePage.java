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
import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.dv.DvResult;
import de.unistuttgart.xsample.dv.DvUserInfo;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.mf.Corpus;
import de.unistuttgart.xsample.mf.SourceFile;
import de.unistuttgart.xsample.mf.SourceType;
import de.unistuttgart.xsample.mf.Span;
import de.unistuttgart.xsample.mf.SpanType;
import de.unistuttgart.xsample.mf.XsampleManifest;
import de.unistuttgart.xsample.pages.XsamplePage;
import de.unistuttgart.xsample.pages.download.DownloadPage;
import de.unistuttgart.xsample.pages.query.QueryPage;
import de.unistuttgart.xsample.pages.shared.ExcerptType;
import de.unistuttgart.xsample.pages.shared.XsampleExcerptData.ExcerptEntry;
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
	
	@Inject
	WelcomeView view;
	
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
		return isShowOutline() && excerptData.isHasAnnotation();
	}
	
	/** Indicate that the choice for excerpt selection should be shown */
	public boolean isShowExcerptSelection() {
		return isShowOutline() && !excerptData.hasFileInfo(FileInfo::isSmallFile);
	}
	
	/** Produce table data for current manifest file */
	public void refreshManifestProperties() {
		view.setManifestProperties(createManifestProperties());
	}
	
	private List<Property> createManifestProperties() {
		if(!isShowOutline()) {
			return Collections.emptyList();
		}
		
		List<Property> properties = new ArrayList<>();
		
		final XsampleManifest manifest = excerptData.getManifest();
		final List<Corpus> corpora = manifest.getCorpora();
		
		properties.add(new Property("corpus-files", String.valueOf(corpora.size())));
		properties.add(new Property("total-segments", String.valueOf(excerptData.getSegments())));
		//TODO
		
		return properties;
	}

	/** Produce table data for currently selected resource */
	public void refreshFileProperties() {
		view.setFileProperties(createFileProperties());
	}
	
	private List<Property> createFileProperties() {
		if(!isShowOutline()) {
			return Collections.emptyList();
		}
		
		final String corpusId = view.getSelectedCorpus();
		
		if(corpusId==null) {
			return Collections.emptyList();
		}

		final FileInfo info = excerptData.findFileInfo(corpusId);
		final List<Property> properties = new ArrayList<>();

		properties.add(new Property("name", info.getTitle()));
		properties.add(new Property("id", corpusId));
		properties.add(new Property("type", info.getExcerptHandler().getType().name()));
		properties.add(new Property("content-type", info.getContentType()));
		properties.add(new Property("encoding", info.getEncoding().displayName()));
		properties.add(new Property("size", XSampleUtils.formatSize(info.getSize())));
		properties.add(new Property("segments", String.valueOf(info.getSegments())));
		//TODO add legal info from manifest

		final ExcerptEntry entry = excerptData.findEntry(corpusId);
		final XmpExcerpt quota = entry.getQuota();
		
		if(info.isSmallFile()) {
			properties.add(new Property("small-file", "true"));
		} else if(!quota.isEmpty()) {
			long used = entry.getQuota().size();
			final long segments = info.getSegments();
			double percent = (double) used / segments * 100.0;
			properties.add(new Property("quota", String.valueOf(used)));
			properties.add(new Property("quota-ratio", String.format("%.2f%%", _double(percent))));
			final long limit = (long) (segments * services.getDoubleSetting(Key.ExcerptLimit));
			
			if(used>=limit) {
				logger.log(Level.SEVERE, String.format("Quota of %d used up on resource %s by user %s", 
						_long(limit), entry.getResource(), excerptData.getDataverseUser()));
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.quotaExceeded", 
						_long(used), info.getTitle());
			}
		}
		return properties;
	}
	
	boolean prepareStaticExcerpt() {
		final long begin = excerptData.getStaticExcerptBegin();
		final long end = excerptData.getStaticExcerptEnd();
		final FileInfo info = excerptData.getStaticExcerptFileInfo();
		final ExcerptEntry entry = excerptData.findEntry(info.getCorpusId());
		
		final long segments = info.getSegments();
		final long first;
		final long last;
		if(excerptData.isStaticExcerptFixed()) {
			first = Math.max(1, begin);
			last = Math.min(segments, end);
		} else {
			first = Math.max(1, (long) (segments / 100.0 * begin));
			last = Math.min(segments, first + (long) (segments / 100.0 * (end-begin+1)) - 1);
		}
		List<XmpFragment> fragments = Arrays.asList(XmpFragment.of(first, last));
		long limit = (long)(segments * services.getDoubleSetting(Key.ExcerptLimit));
		long usedUpSlots = XSampleUtils.combinedSize(fragments, entry.getQuota().getFragments());
		if(usedUpSlots > limit) {
			String text = BundleUtil.format("welcome.msg.staticExcerptExceedsQuota", 
					_long(begin), _long(end), info.getTitle());
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
			FacesContext.getCurrentInstance().addMessage("navMsgs", msg);
			return false;			
		}

		entry.setFragments(fragments);
		excerptData.setExcerpt(Arrays.asList(entry));
		
		return true;
	}
	
	void prepareFullDownload(FileInfo info) {
		long segments = info.getSegments();
		List<XmpFragment> fragments = Arrays.asList(XmpFragment.of(1, segments));
		
		ExcerptEntry entry = new ExcerptEntry();
		entry.setCorpusId(info.getCorpusId());
		entry.setFragments(fragments);
		excerptData.setExcerpt(Arrays.asList(entry));
	}
	
	/** Callback for button to continue workflow */
	public void next() {
		String page = null;
		
		final FileInfo info = excerptData.findFileInfo(view.getSelectedCorpus());
		
		if(info.isSmallFile()) {
			// Small file -> full download
			prepareFullDownload(info);
			page = DownloadPage.PAGE;
		} else {
			// Big file -> Delegate to correct page
			ExcerptType excerptType = view.getExcerptType();
			switch (excerptType) {
			case STATIC: {
				page = prepareStaticExcerpt() ? DownloadPage.PAGE : PAGE;
			} break;
			case SLICE: page = SlicePage.PAGE; break;
			case QUERY: page = QueryPage.PAGE; break;
			default:
				break;
			}
			
			//TODO handle 'includeAnnotations' flag
			
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
	
	private static class Context {
		/** Interface for dataverse access */
		DataverseClient client;
	}
	
	@Transactional
	public void verifyInput() {
		if(excerptData.isVerified()) {
			return;
		}
		
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
		excerptData.setVerified(true);
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
				new Step(this::loadManifest, "welcome.step.loadManifest"),
				new Step(this::validateManifest, "welcome.step.validateManifest"),
				new Step(this::loadFiles, "welcome.step.loadFile"),
				new Step(this::validateExcerpt, "welcome.step.validateExcerpt"),
				new Step(this::checkFileSize, "welcome.step.checkFileSize"),
				new Step(this::checkQuota, "welcome.step.checkQuota"),
				new Step(this::prepareUI, "welcome.step.prepareUI")
		);
		
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
		final Optional<XmpDataverse> dataverse = services.findDataverseByUrl(address);
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
		final XmpDataverse server = excerptData.getServer();
		final XmpDataverseUser user = services.findDataverseUser(server, info.getData().getPersistentUserId());
		excerptData.setDataverseUser(user);
		
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
		
		final XsampleManifest manifest;
		
		// Successfully opened and accessed data, now load content
		try(ResponseBody body = response.body()) {
			final MediaType mediaType = body.contentType();
			if(mediaType==null) {
				logger.log(Level.SEVERE, "Failed to obtain media type");
				message(FacesMessage.SEVERITY_ERROR,"welcome.msg.noMediaType");
				return false;
			}
			
			try(Reader reader = body.charStream()) {
				manifest = XsampleManifest.parse(reader);
			}
			
			// Update manifest in current setup
			excerptData.setManifest(manifest);
		} catch (IOException | JsonIOException e) {
			logger.log(Level.SEVERE, "Failed to load manifest content", e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.loadFailed");
			return false;
		} catch (JsonSyntaxException e) {
			logger.log(Level.SEVERE, "Malformed manifest file: "+request.request().url(), e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.malformedManifest");
			return false;
		}
		
		return true;
	}
	
	/** Validate the root manifest. */
	boolean validateManifest(Context context) {
		final XsampleManifest manifest = excerptData.getManifest();
		
		// Let the self-vlaidating manifest do an internal check first!
		try {
			manifest.validate();
		} catch(RuntimeException e) {
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.invalidManifest", e.getMessage());
			return false;
		}
		// From here on we know the manifest is _basically_ correct
		
		// Now run a few extra checks to make sure certain manifest parts match
		final List<Corpus> corpora = manifest.getCorpora();
		for(Corpus corpus : corpora) {
			// Check against deep corpora that we can't handle (yet)
			if(corpus.hasParts()) {
				logger.log(Level.SEVERE, "Can't handle deeply nested corpus part in manifest.");
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.manifestNotFlat");
				return false;
			}
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

	/** Load the entire source files */
	boolean loadFiles(Context context) {
		final String key = excerptData.getServer().getMasterKey();
		final DataverseClient client = requireNonNull(context.client);
		
		long totalSegmemnts = 0;
		
		for(Corpus corpus : excerptData.getManifest().getCorpora()) {
			final SourceFile sourceFile = corpus.getPrimaryData();
			
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
				
				final FileInfo fileInfo = new FileInfo(corpus);
				fileInfo.setContentType(mediaType.type()+"/"+mediaType.subtype());
				fileInfo.setEncoding(mediaType.charset(StandardCharsets.UTF_8));
				fileInfo.setTitle(extractName(mediaType.toString()));

				final SourceType sourceType = sourceFile.getSourceType();
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
				fileInfo.setExcerptHandler(handler);
				
				// Override segment count with value from manfiest if present
				long segments = fileInfo.getSegments();
				Long segmentsOverride = sourceFile.getSegments();
				if(segmentsOverride!=null && segmentsOverride.longValue()>0 && segmentsOverride.longValue()<segments) {
					fileInfo.setSegments(segmentsOverride.longValue());
				}
				
				totalSegmemnts += fileInfo.getSegments();
				
				// Update info in current config
				excerptData.addFileInfo(fileInfo);
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
		}
		
		excerptData.setSegments(totalSegmemnts);
		
		return true;
	}	
	
	/** Verify manifest excerpt data - needs file info for loaded target resource!! */
	boolean validateExcerpt(Context context) {
		final XsampleManifest manifest = excerptData.getManifest();
		
		long begin = 0, end = 0;
		boolean fixed = false;
		
		final FileInfo info = excerptData.getStaticExcerptFileInfo();
		
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
				long segments = info.getSegments();
				limit = (long) (services.getDoubleSetting(Key.ExcerptLimit) * segments);
			} else {
				limit = (long) (services.getDoubleSetting(Key.ExcerptLimit) * 100);
			}
			
			if(size > limit) {
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.manifestExceedsQuota", 
						_long(size), _double(limit), info.getTitle(), _long(info.getSegments()));
				return false;
			}
		} else {
			begin = 0;
			end = services.getIntSetting(Key.DefaultStaticExcerpt);
		}
		
		final ExcerptHandler handler = info.getExcerptHandler();
		
		// Compute label once and store in excerpt info
		String label;
		if(fixed) {
			final long size = end-begin+1; 
			if(size==1) {
				label = BundleUtil.format("welcome.excerptLabel.singleton", 
						handler.getSegmentLabel(false), _long(begin), info.getTitle());
			} else {
				label = BundleUtil.format("welcome.excerptLabel.fixed", 
						handler.getSegmentLabel(true), _long(begin), _long(end), info.getTitle());
			}
		} else {
			label = BundleUtil.format("welcome.excerptLabel.relative", 
					_long(begin), _long(end), info.getTitle());
		}
		
		excerptData.setStaticExcerptBegin(begin);
		excerptData.setStaticExcerptEnd(end);
		view.setStaticExcerptLabel(label);
		
		return true;
	}

	boolean checkFileSize(Context context) {
		final long threshold = services.getLongSetting(Key.SmallFileLimit);
		for(FileInfo info : excerptData.getFileInfos()) {
			final long segments = info.getSegments();
			info.setSmallFile(segments<=threshold);
		}
		return true;
	}
		
	/** Verify that user still has quota left on the designated resource */
	//TODO we need a better way to compute global quota across all the involved resouruces
	boolean checkQuota(Context context) {
		final XmpDataverseUser user = excerptData.getDataverseUser();
		final XmpDataverse server = excerptData.getServer();
		
		long globalQuota = 0;
		
		for(FileInfo info : excerptData.getFileInfos()) {
			final SourceFile sourceFile = excerptData.findCorpus(info).getPrimaryData();
			final Long fileId = sourceFile.getId();
			final XmpResource resource = services.findResource(server, fileId);
			final XmpExcerpt excerpt = services.findQuota(user, resource);
			
			final ExcerptEntry entry = new ExcerptEntry();
			entry.setCorpusId(info.getCorpusId());
			entry.setResource(resource);
			entry.setQuota(excerpt);
			excerptData.addExcerptEntry(entry);
			
			if(!info.isSmallFile() && !excerpt.isEmpty()) {
				final long quota = excerpt.size();
				final long segments = excerptData.getSegments();
				final long limit = (long) (segments * services.getDoubleSetting(Key.ExcerptLimit));
				
				if(quota>=limit) {
					logger.log(Level.SEVERE, String.format("Quota of %d used up on resource %s by user %s", 
							_long(limit), resource, user));
					message(FacesMessage.SEVERITY_ERROR, "welcome.msg.quotaExceeded", 
							_long(quota), info.getTitle());
					
					return false;
				}
			}
		}
		
		
		return true;
	}
	
	boolean prepareUI(Context context) {
		if(view.getSelectedCorpus()==null) {
			List<FileInfo> fileInfos = excerptData.getFileInfos();
			if(!fileInfos.isEmpty()) {
				view.setSelectedCorpus(fileInfos.get(0).getCorpusId());
			}
		}
		
		refreshManifestProperties();
		refreshFileProperties();
		
		return true;
	}
}
