/**
 * 
 */
package de.unistuttgart.xsample.mp;

import static de.unistuttgart.xsample.util.XSampleUtils.buffer;
import static de.unistuttgart.xsample.util.XSampleUtils.strictToInt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import de.unistuttgart.xsample.qe.icarus1.StringPrimitives;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Implements an int-limited simple mapping that takes its data from a tabular file.
 * 
 * @author Markus Gärtner
 *
 */
public class TabularMapping implements Mapping {
	
	private int[] begin, end;

	@Override
	public void load(Reader reader) throws IOException {
		List<String> lines = new ObjectArrayList<>();
		try(BufferedReader br = buffer(reader)) {
			String line;
			while((line = br.readLine()) != null) {
				if(!line.isEmpty()) {
					lines.add(line);
				}
			}
		}
		
		int size = lines.size();
		begin = new int[size];
		end = new int[size]; 
		
		for (int i = 0; i < size; i++) {
			String line = lines.get(i);
			int sep1 = line.indexOf('\t');
			int sep2 = line.indexOf('\t', sep1+1);
			
			if(sep1==-1 || sep2==-1)
				throw new IllegalAccessError("Illegal mapping content: "+line);
			
			int index = StringPrimitives.parseInt(line, 0, sep1-1);
			if(index!=i)
				throw new IllegalArgumentException("Inconsistent source index: "+line);
			
			begin[index] = StringPrimitives.parseInt(line, sep1+1, sep2-1);
			end[index] = StringPrimitives.parseInt(line, sep2+1, line.length()-1);
		}
	}

	@Override
	public int map(long sourceIndex, long[] buffer) {
		int s = strictToInt(sourceIndex);
		if(s>=begin.length) {
			return 0;
		}
		int b = begin[s];
		int e = end[s];
		int length = e-b+1;
		if(length>buffer.length) {
			return -1;
		}
		for (int i = 0; i < length; i++) {
			buffer[i] = b+i;
		}
		return length;
	}

}
