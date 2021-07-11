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
package de.unistuttgart.xsample.qe.icarus1.match;

import static de.unistuttgart.xsample.util.XSampleUtils.checkState;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.unistuttgart.xsample.qe.Result;
import de.unistuttgart.xsample.qe.icarus1.CompactProperties;
import de.unistuttgart.xsample.qe.icarus1.Options;
import de.unistuttgart.xsample.qe.icarus1.SentenceData;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Search {

	private SearchState state = SearchState.BLANK;

	private Object lock = new Object();

	private final List<SentenceData> target;
	private final SearchQuery query;

	private AtomicBoolean cancelled = new AtomicBoolean();

	private CompactProperties properties;

	private int progress = 0;

	private final Options parameters;

	private Instant beginTimestamp, endTimestamp;

	private Matcher rootMatcher;
	
	private Result result = new Result();

	public Search(SearchQuery query, Options parameters, List<SentenceData> target) {
		if(query==null)
			throw new NullPointerException("Invalid query"); //$NON-NLS-1$
		if(target==null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$

		if(parameters==null) {
			parameters = Options.emptyOptions;
		}

		this.query = query;
		this.target = target;
		this.parameters = parameters.clone();
	}

	public boolean isSerializable() {
		return true;
	}

	public SearchGraph getSearchGraph() {
		return getQuery().getSearchGraph();
	}

	public boolean init() {
		if(SearchUtils.isEmpty(getSearchGraph()))
			throw new IllegalStateException("Graph is empty"); //$NON-NLS-1$
		
		initEngine();
		
		return true;
	}

	private void initEngine() {
		rootMatcher = new MatcherBuilder(this).createRootMatcher();
		if(rootMatcher==null)
			throw new IllegalStateException("Invalid root matcher created"); //$NON-NLS-1$

		rootMatcher.setLeftToRight(SearchUtils.isLeftToRightSearch(this));
	}

	private final void setState(SearchState state) {
		SearchState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			this.state = state;
		}
	}

	final void setState(SearchState expected, SearchState state) {
		SearchState oldValue;
		synchronized (lock) {
			oldValue = this.state;
			if(oldValue!=expected)
				throw new IllegalStateException();
			this.state = state;
		}
	}

	public final SearchState getState() {
		synchronized (lock) {
			return state;
		}
	}

	public final SearchQuery getQuery() {
		return query;
	}

	public final List<SentenceData> getTarget() {
		return target;
	}

	public final Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	public final void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}

		properties.put(key, value);
	}

	public final Object getParameter(String key) {
		return parameters.get(key);
	}

	public Options getParameters() {
		return parameters;
	}

	public final boolean isCancelled() {
		return cancelled.get();
	}

	public final boolean isDone() {
		synchronized (lock) {
			return state==SearchState.DONE || state==SearchState.CANCELLED;
		}
	}

	public final boolean isRunning() {
		synchronized (lock) {
			return state==SearchState.RUNNING;
		}
	}

	/**
	 * Attempts to cancel this search by setting the internal {@code cancelled}
	 * flag to {@code true}.
	 * This method will throw an {@link IllegalArgumentException} if the
	 * search is not yet running or has already been finished or cancelled.
	 */
	public final void cancel() {
		synchronized (lock) {
			/*SearchState state = getState();
			if(state==SearchState.BLANK)
				throw new IllegalStateException("Search not started yet!"); //$NON-NLS-1$
			if(state!=SearchState.RUNNING)
				throw new IllegalStateException("Search not running!"); //$NON-NLS-1$*/
			if(!cancelled.compareAndSet(false, true))
				throw new IllegalStateException("Search already cancelled!"); //$NON-NLS-1$

			setState(SearchState.CANCELLED);

			innerCancel();
		}
	}

	/**
	 * Callback for subclasses to perform proper cleanup of their
	 * resources
	 */
	private void innerCancel() {
		// no-op
	}

	public final void finish() {
		setState(SearchState.RUNNING, SearchState.DONE);

		endTimestamp = Instant.now();
	}

	/**
	 * Runs the search and constructs the internal {@code SearchResult} object.
	 * Note that an implementation should regularly check for user originated
	 * cancellation by invoking {@link #isCancelled()}.
	 */
	public final void execute() {
		if(isDone())
			throw new IllegalStateException("Cannot reuse search instance"); //$NON-NLS-1$

		setState(SearchState.BLANK, SearchState.RUNNING);

		beginTimestamp = Instant.now();

		if(!innerExecute() && isRunning()) {
			setState(SearchState.RUNNING, SearchState.DONE);
		}
	}

	/**
	 * Performs the implementation specific scheduling of
	 * the search operation. If an implementation realizes that
	 * the supplied data does not allow for a regular search
	 * execution to take place it can immediately return a
	 * value of {@code false} to signal an early exit. The search
	 * will then set its state to {@value SearchState#DONE}.
	 * @return {@code true} if and only if the search operation
	 * was successfully scheduled.
	 */
	private boolean innerExecute() {
		
		final TargetTree targetTree = new TargetTree();
		final LongList matches = new LongArrayList(target.size());
		
		rootMatcher.setTargetTree(targetTree);
		rootMatcher.setSearchMode(SearchMode.MATCHES);
		rootMatcher.setLeftToRight(true);
		
		for (int i=0; i<target.size(); i++) {
			SentenceData sentence = target.get(i);
			targetTree.reload(sentence, Options.emptyOptions);
			
			if(rootMatcher.matches()) {
				matches.add(i);
			}
		}
		
		result.setHits(matches.toLongArray());
		
		setState(SearchState.RUNNING, SearchState.DONE);
		
		return !matches.isEmpty();
	}
	
	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}
	
	public Duration getDuration() {
		checkState("not started", beginTimestamp!=null);
		checkState("not finished", endTimestamp!=null);
		return Duration.between(beginTimestamp, endTimestamp);
	}

	/**
	 * Returns the (estimated) progress of the search in the range
	 * 0 to 100.
	 */
	public int getProgress() {
		return progress;
	}

	private void setProgress(int newProgress) {
		if(newProgress==progress) {
			return;
		}
		if(newProgress<progress)
			throw new IllegalArgumentException("Cannot decrease progress field"); //$NON-NLS-1$

		int oldProgress = progress;
		progress = newProgress;
	}
}
