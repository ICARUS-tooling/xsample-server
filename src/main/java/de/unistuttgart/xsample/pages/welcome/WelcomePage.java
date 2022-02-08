/*
 * XSample Server
 * Copyright (C) 2020-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import de.unistuttgart.xsample.ct.UnsupportedContentTypeException;
import de.unistuttgart.xsample.dv.DataverseClient;
import de.unistuttgart.xsample.dv.DvResult;
import de.unistuttgart.xsample.dv.DvUserInfo;
import de.unistuttgart.xsample.dv.XmpDataverse;
import de.unistuttgart.xsample.dv.XmpDataverseUser;
import de.unistuttgart.xsample.dv.XmpExcerpt;
import de.unistuttgart.xsample.dv.XmpFileInfo;
import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.dv.XmpLocalCopy;
import de.unistuttgart.xsample.dv.XmpResource;
import de.unistuttgart.xsample.io.AccessException;
import de.unistuttgart.xsample.io.InternalServerException;
import de.unistuttgart.xsample.io.LocalCache;
import de.unistuttgart.xsample.io.TransmissionException;
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
	
	private static final long DEFAULT_LOCK_TIMEOUT_MIMLLIS = 50L;
	
	static final String NAV_MSG = "navMsgs";
	
	@Inject
	XsampleSession session;
	
	@Inject
	XsampleInputData inputData;
	
	@Inject
	LocalCache cache;
	
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
	
	/** Indicate that the choice for excerpt selection should be shown */
	public boolean isShowExcerptSelection() {
		return isShowOutline() && !excerptData.isOnlySmallFiles();
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
		
		final Corpus corpus = excerptData.findCorpus(corpusId);
		final XmpResource resource = services.findResource(excerptData.getServer(), 
				corpus.getPrimaryData().getId());
		final XmpLocalCopy copy = cache.getCopy(resource);
		final XmpFileInfo fileInfo = services.findFileInfo(resource);
		
		final List<Property> properties = new ArrayList<>();

		properties.add(new Property("name", copy.getTitle()));
		properties.add(new Property("id", corpusId));
		properties.add(new Property("type", fileInfo.getSourceType().name()));
		properties.add(new Property("content-type", copy.getContentType()));
		properties.add(new Property("encoding", copy.getEncoding()));
		properties.add(new Property("size", XSampleUtils.formatSize(copy.getSize())));
		properties.add(new Property("segments", String.valueOf(fileInfo.getSegments())));
		//TODO add legal info from manifest

		final ExcerptEntry entry = excerptData.findEntry(corpusId);
		final XmpExcerpt quota = entry.getQuota();
		
		if(fileInfo.isSmallFile()) {
			properties.add(new Property("small-file", "true"));
		} else if(!quota.isEmpty()) {
			long used = entry.getQuota().size();
			final long segments = fileInfo.getSegments();
			double percent = (double) used / segments * 100.0;
			properties.add(new Property("quota", String.valueOf(used)));
			properties.add(new Property("quota-ratio", String.format("%.2f%%", _double(percent))));
			final long limit = (long) (segments * services.getDoubleSetting(Key.ExcerptLimit));
			
			if(used>=limit) {
				logger.log(Level.SEVERE, String.format("Quota of %d used up on resource %s by user %s", 
						_long(limit), entry.getResource(), excerptData.getDataverseUser()));
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.quotaExceeded", 
						_long(used), copy.getTitle());
			}
		}
		return properties;
	}
	
	boolean prepareStaticExcerpt() {
		final long begin = excerptData.getStaticExcerptBegin();
		final long end = excerptData.getStaticExcerptEnd();
		final Corpus corpus = excerptData.getStaticExcerptCorpus();
		final XmpResource resource = services.findResource(excerptData.getServer(), 
				corpus.getPrimaryData().getId());
		final XmpFileInfo fileInfo = services.findFileInfo(resource);
		final ExcerptEntry entry = excerptData.findEntry(corpus.getId());
		
		final long segments = fileInfo.getSegments();
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
					_long(begin), _long(end), corpus.getId());
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
			FacesContext.getCurrentInstance().addMessage(NAV_MSG, msg);
			return false;			
		}

		entry.setFragments(fragments);
		excerptData.setExcerpt(Arrays.asList(entry));
		
		return true;
	}
	
	void prepareFullDownload(Corpus corpus, XmpFileInfo info) {
		long segments = info.getSegments();
		List<XmpFragment> fragments = Arrays.asList(XmpFragment.of(1, segments));
		
		ExcerptEntry entry = new ExcerptEntry();
		entry.setCorpusId(corpus.getId());
		entry.setFragments(fragments);
		excerptData.setExcerpt(Arrays.asList(entry));
	}
	
	/** Callback for button to continue workflow */
	public void next() {
		String page = null;
		
		final String corpusId = view.getSelectedCorpus();
		final Corpus corpus = excerptData.findCorpus(corpusId);
		final XmpResource resource = services.findResource(excerptData.getServer(), 
				corpus.getPrimaryData().getId());
		final XmpFileInfo fileInfo = services.findFileInfo(resource);
		
		if(fileInfo.isSmallFile()) {
			// Small file -> full download
			prepareFullDownload(corpus, fileInfo);
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
			
			assert view.getSelectedCorpus()!=null : "no corpus selected";
			excerptData.setSelectedCorpus(view.getSelectedCorpus());
			
			//TODO handle 'includeAnnotations' flag
			
			if(page==null) {
				logger.severe("Unknown page result from routing in welcome page for type: "+excerptType);
				String text = BundleUtil.format("welcome.msg.unknownPage", excerptType);
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
				FacesContext.getCurrentInstance().addMessage(NAV_MSG, msg);
				return;
			}
		}

//		logger.fine("Navigating to subpage "+page);
		
		forward(page);
	}
	
	private static class Context {
		/** Interface for dataverse access */
		DataverseClient client;
		/** Cache for file infos of current input */
		final List<XmpFileInfo> fileInfos = new ArrayList<>();
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
	
		refreshManifestProperties();
		refreshFileProperties();
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
	
	private String errorBody(Response<?> response) {
		String msg;
		try {
			msg = response.errorBody().string();
		} catch (IOException e) {
			logger.severe("Failed to obtain error body for API response: "+e.getMessage());
			msg = "=== failed to obtain error body ===";
		}
		return msg;
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
		try {
			new URL(address);
		} catch(MalformedURLException e) {
			message(FacesMessage.SEVERITY_ERROR,"welcome.msg.invalidDataverseUrl", address);
			return false;
		}
		context.client = DataverseClient.forServer(dataverse.get().getUrl()); //TODO change to getUsableUrl()
		
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
					key, _int(response.code()), errorBody(response)));
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
	

	/** Load the entire source files */
	boolean loadFiles(Context context) {
		final XmpDataverse server = excerptData.getServer();
		
		long totalSegmemnts = 0;
		
		final List<Corpus> corpora = excerptData.getManifest().getCorpora();
		
		for(Corpus corpus : corpora) {
			final String corpusId = corpus.getId();
			final SourceFile sourceFile = corpus.getPrimaryData();

			// Sanity check against manifest's content type
			final SourceType sourceType = sourceFile.getSourceType();
			final ExcerptHandler excerptHandler;
			try {
				excerptHandler = ExcerptHandlers.forSourceType(sourceType);
			} catch (UnsupportedContentTypeException e) {
				logger.log(Level.SEVERE, "Content type of file not supported", e);
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.unsupportedType", corpusId);
				return false;
			}
			
			// Ensure local copy
			final XmpResource resource = services.findResource(server, sourceFile.getId());	
			final XmpLocalCopy copy = cache.getCopy(resource);	
			try {
				copy.getLock().tryLock(DEFAULT_LOCK_TIMEOUT_MIMLLIS, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Unable to lock copy for corpus: "+corpusId, e);
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.cacheBusy", corpusId);
				return false;
			}
			try {
				// Ensure local copy, i.e. load remote data if needed
				try {
					cache.ensureLocal(copy);
				} catch (GeneralSecurityException e) {
					logger.log(Level.SEVERE, "Failed to prepare cipher", e);
					message(FacesMessage.SEVERITY_ERROR, "welcome.msg.cipherPreparation", corpusId);
					return false;
				} catch (AccessException e) {
					logger.log(Level.SEVERE, String.format("Access error response from Dataverse (code %d): %s",
							_int(e.getCode()), e.getMessage()), e);
					message(FacesMessage.SEVERITY_ERROR, "welcome.msg.response400", _int(e.getCode()));
					return false;
				} catch (InternalServerException e) {
					logger.log(Level.SEVERE, String.format("Internal error response from Dataverse (code %d): %s",
							_int(e.getCode()), e.getMessage()), e);
					message(FacesMessage.SEVERITY_ERROR, "welcome.msg.response500", _int(e.getCode()));
					return false;
				} catch (TransmissionException e) {
					logger.log(Level.SEVERE, "Failed to load remote content", e);
					message(FacesMessage.SEVERITY_ERROR, "welcome.msg.loadFailed", corpusId);
					return false;
				}
				
				// Ensure we have metadata about the file
				final XmpFileInfo fileInfo = services.findFileInfo(resource);
				if(!fileInfo.isSet()) {
					final Charset encoding = Charset.forName(copy.getEncoding());
					fileInfo.setSourceType(sourceType);
					try(InputStream in = cache.openLocal(copy)) {
						excerptHandler.analyze(fileInfo, encoding, in);
					} catch (UnsupportedContentTypeException e) {
						logger.log(Level.SEVERE, "Content type of remote file not supported", e);
						message(FacesMessage.SEVERITY_ERROR, "welcome.msg.unsupportedType", corpusId);
						return false;
					} catch (EmptyResourceException e) {
						logger.log(Level.SEVERE, "Remote source file empty", e);
						message(FacesMessage.SEVERITY_ERROR, "welcome.msg.emptyResource", corpusId);
						return false;
					} catch (IOException e) {
						logger.log(Level.SEVERE, "Failed to load cached file content", e);
						message(FacesMessage.SEVERITY_ERROR, "welcome.msg.loadFailed", corpusId);
						return false;
					} catch (GeneralSecurityException e) {
						logger.log(Level.SEVERE, "Failed to prepare cipher", e);
						message(FacesMessage.SEVERITY_ERROR, "welcome.msg.cipherPreparation", corpusId);
						return false;
					}
				} 
				
				// Accumulate segments globally
				totalSegmemnts += fileInfo.getSegments();
				
				context.fileInfos.add(fileInfo);
			} finally {
				copy.getLock().unlock();
			}
		}
		
		excerptData.setSegments(totalSegmemnts);
		
		assert context.fileInfos.size()==corpora.size() : "Missed files in loading process";
		
		return true;
	}
	
	
	/** Verify manifest excerpt data - needs file info for loaded target resource!! */
	boolean validateExcerpt(Context context) {
		final XsampleManifest manifest = excerptData.getManifest();
		
		long begin = 0, end = 0;
		boolean fixed = false;

		final Corpus corpus = excerptData.getStaticExcerptCorpus();
		final XmpResource resource = services.findResource(excerptData.getServer(), 
				corpus.getPrimaryData().getId());
		final XmpFileInfo fileInfo = services.findFileInfo(resource);
		final XmpLocalCopy copy = cache.getCopy(resource);
		
		// Check static excerpt declaration and translate into proper bounded range data
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
				long segments = fileInfo.getSegments();
				limit = (long) (services.getDoubleSetting(Key.ExcerptLimit) * segments);
			} else {
				limit = (long) (services.getDoubleSetting(Key.ExcerptLimit) * 100);
			}
			
			if(size > limit) {
				message(FacesMessage.SEVERITY_ERROR, "welcome.msg.manifestExceedsQuota", 
						_long(size), _double(limit), corpus.getId(), _long(fileInfo.getSegments()));
				return false;
			}
		} else {
			begin = 0;
			end = services.getIntSetting(Key.DefaultStaticExcerpt);
		}

		final ExcerptHandler excerptHandler;
		try {
			excerptHandler = ExcerptHandlers.forSourceType(corpus.getPrimaryData().getSourceType());
		} catch (UnsupportedContentTypeException e) {
			logger.log(Level.SEVERE, "Content type of file not supported", e);
			message(FacesMessage.SEVERITY_ERROR, "welcome.msg.unsupportedType", corpus.getId());
			return false;
		}
		
		excerptData.setStaticExcerptBegin(begin);
		excerptData.setStaticExcerptEnd(end);
		
		// Compute label once and store in view
		final String label;
		if(fixed) {
			final long size = end-begin+1; 
			if(size==1) {
				label = BundleUtil.format("welcome.excerptLabel.singleton", 
						excerptHandler.getSegmentLabel(false), _long(begin), copy.getTitle());
			} else {
				label = BundleUtil.format("welcome.excerptLabel.fixed", 
						excerptHandler.getSegmentLabel(true), _long(begin), _long(end), copy.getTitle());
			}
		} else {
			label = BundleUtil.format("welcome.excerptLabel.relative", 
					_long(begin), _long(end), copy.getTitle());
		}
		view.setStaticExcerptLabel(label);
		
		return true;
	}

	boolean checkFileSize(Context context) {
		final long threshold = services.getLongSetting(Key.SmallFileLimit);
		boolean onlySmallFiles = true;
		for(XmpFileInfo info : context.fileInfos) {
			final long segments = info.getSegments();
			info.setSmallFile(segments<=threshold);
			onlySmallFiles &= info.isSmallFile();
		}
		excerptData.setOnlySmallFiles(onlySmallFiles);
		return true;
	}
		
	/** Verify that user still has quota left on the designated resource */
	//TODO we need a better way to compute global quota across all the involved resouruces
	boolean checkQuota(Context context) {
		final XmpDataverseUser user = excerptData.getDataverseUser();
		final double limitFactor = services.getDoubleSetting(Key.ExcerptLimit);
		
		long globalQuota = 0;
		
		for(XmpFileInfo info : context.fileInfos) {
			final XmpResource resource = info.getResource();
			final XmpLocalCopy copy = cache.getCopy(resource);
			final XmpExcerpt excerpt = services.findQuota(user, resource);
			final Corpus corpus = excerptData.findCorpus(resource);

			final long segments = info.getSegments();
			final long limit = (long) Math.floor(segments * limitFactor);
			
			final ExcerptEntry entry = new ExcerptEntry();
			entry.setCorpusId(corpus.getId());
			entry.setResource(resource);
			entry.setQuota(excerpt);
			entry.setLimit(limit);
			excerptData.addExcerptEntry(entry);
			
			if(!info.isSmallFile() && !excerpt.isEmpty()) {
				final long quota = excerpt.size();
				
				if(quota>=limit) {
					logger.log(Level.SEVERE, String.format("Quota of %d used up on resource %s by user %s", 
							_long(limit), resource, user));
					message(FacesMessage.SEVERITY_ERROR, "welcome.msg.quotaExceeded", 
							_long(quota), copy.getTitle());
					
					return false;
				}
			}
		}
		
		return true;
	}
	
	boolean prepareUI(Context context) {
		if(view.getSelectedCorpus()==null) {
			List<XmpFileInfo> fileInfos = context.fileInfos;
			if(!fileInfos.isEmpty()) {
				final XmpFileInfo info = fileInfos.get(0);
				final XmpResource resource = info.getResource();
				final Corpus corpus = excerptData.findCorpus(resource);
				view.setSelectedCorpus(corpus.getId());
			}
		}
		
		return true;
	}
}
