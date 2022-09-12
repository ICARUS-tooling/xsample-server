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
/**
 * 
 */
package de.unistuttgart.xsample.ct;

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.function.LongPredicate;

import de.unistuttgart.xsample.dv.XmpFragment;
import de.unistuttgart.xsample.mp.Mapping;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class CoNLL09Handler implements AnnotationHandler {

	private static final long serialVersionUID = -3603704271020637347L;

	@Override
	public void excerpt(Reader reader, Mapping mapping, List<XmpFragment> fragments, OutputStream out)
			throws IOException {
		requireNonNull(mapping);
		
		try(BufferedReader br = buffer(reader);
				Writer writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
			
			PageFilter filter = new PageFilter(fragments);
			SentenceBuffer buffer = new SentenceBuffer(br);
			int sentenceIndex = 0;
			long targetBegin, targetEnd;
			
			while(buffer.next()) {
				targetBegin = mapping.getTargetBegin(sentenceIndex);
				targetEnd = mapping.getTargetEnd(sentenceIndex);
				if(targetBegin!=-1 && targetEnd!=-1 && targetEnd>=targetBegin) {
					for(long page = targetBegin; page<=targetEnd; page++) {
						// Mapping produces 0-based page indices
						if(filter.test(page+1)) {
							writer.append("# sentenceIndex=").append(String.valueOf(sentenceIndex+1)).append('\n');
							writer.append("# pageIndex=").append(String.valueOf(page+1)).append('\n');
							buffer.print(writer);
							writer.append('\n');
							writer.flush();
						}
					}
				}
				
				sentenceIndex++;
				
				if(filter.isEOS()) {
					break;
				}
			}
		}
	}
	
	private static class SentenceBuffer {
		private final BufferedReader reader;
		private final List<String> lines = new ObjectArrayList<>();
		
		private String line = null;

		public SentenceBuffer(BufferedReader reader) {
			this.reader = reader;
		}
		
		private String line() throws IOException {
			if(line==null) {
				line = reader.readLine();
			}
			return line;
		}
		
		private void consumeLine() {
			line = null;
		}
		
		private static boolean isContentLine(String line) {
			line = line.trim();
			return !line.isEmpty() && !line.startsWith("#");
		}
		
		
		//FIXME needs rework (we keep getting unsplit sentence blocks in excerpt output!)
		boolean next() throws IOException {
			String line;
			
			// Find begin
			while((line=reader.readLine()) != null) {
				if(isContentLine(line)) {
					lines.add(line);
					break;
				}
			}
			
			// Find end
			while((line=reader.readLine()) != null) {
				if(isContentLine(line)) {
					lines.add(line);
				} else {
					break;
				}
			}
				
			return !lines.isEmpty();
		}
		
		void print(Writer writer) throws IOException {
			for (String line : lines) {
				writer.append(line).append('\n');
			}
			lines.clear();
		}
	}

	/** Tests 1-based indices for inclusion in the excerpt. */
	private static class PageFilter implements LongPredicate {
		private final Iterator<XmpFragment> it;
		
		private XmpFragment current;
		private boolean eos = false;

		public PageFilter(List<XmpFragment> fragments) {
			this.it = requireNonNull(fragments).iterator();
			if(it.hasNext()) {
				current = it.next();
			} else {
				eos = true;
			}
		}
		
		boolean isEOS() {
			return eos;
		}

		/** Test given 1-based index for inclusion in the excerpt. */
		@Override
		public boolean test(long sentenceIndex) {
			if(eos) {
				return false;
			}
			
			if(sentenceIndex<current.getBeginIndex()) {
				return false;
			}
			
			while(sentenceIndex>current.getEndIndex()) {
				if(it.hasNext()) {
					current = it.next();
				} else {
					eos = true;
					return false;
				}
			}
			
			return true;
		}
	}
}
