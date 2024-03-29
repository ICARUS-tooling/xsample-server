/**
 * Utility functions for communicating with the XSample server
 * and encoding/decoding excerpt data. Note that all functions
 * here expect fragment arrays to be sorted!!
 */

function getCSSVariable(varName) {
  return getComputedStyle(document.documentElement).getPropertyValue(varName);
}

/**
 * Wrap begin and end values into a fragment object with matching fields.
 * @param begin the 1-based begin value
 * @param end the 1-based end value 
 * @returns a fragment object
 */
function toFragment(begin, end) {
	return {begin:begin, end:end};
}

/**
 * Parses a single fragment
 * @param input raw string encoding the fragment
 * @returns a fragment object with 'begin' and 'end' values
 */
function parseFragment(input) {
	var sep = input.indexOf("-");
	if(sep==-1) {
		var num = Number(input);
		return toFragment(num, num);
	} else {
		return toFragment(Number(input.slice(0, sep)), Number(input.slice(sep+1, input.length)));
	}
}

/**
 * Parses a fragment list, i.e. a sequence of fragments
 * separated by commas. 
 * 
 * @param input raw string encoding the list of fragments
 * @returns an array of integer values, denoting pairs of fragment bounds
 */
function parseFragments(input) {
	return input.length==0 ? [] : input.split(',').map(parseFragment);
}

/**
 * Runs a binary search in the given quota array to find
 * a fragment that contains the supplied segment.
 * If no such fragment exists, -1 is returned.
 * 
 * @param quota array of fragment objects
 * @param segment the segment slot to search for
 * @param fromIndex the index of the first element (inclusive) to be searched
 * @param toIndex the index of the last element (exclusive) to be searched
 */
function findFragment(quota, segment, fromIndex, toIndex) {
    var low = fromIndex;
    var high = toIndex - 1;

    while (low <= high) {
        var mid = (low + high) >>> 1;
        var fragment = quota[mid];

        if (fragment.end < segment)
            low = mid + 1;
        else if (fragment.begin > segment)
            high = mid - 1;
        else
            return mid; // segment found
    }
    return -1;  // segment not found.
}

/**
 * Helper function to compute the size of a fragment span
 * @param fragment the fragment span to process
 * @returns the size (at least 1) of the span
 */
function sizeOf(fragment) {
	if(Array.isArray(fragment)) {
		return fragment.reduce((sum, f) => sum+f.end-f.begin+1, 0);
	} else {
		return fragment.end - fragment.begin + 1;
	}
}

/**
 * Helper function to check if the combined coverage of two fragments arrays
 * exceeds a given limit.
 * @param a1 first array of fragment objects
 * @param a2 second array of fragment objects
 * @param limit threshold on which to return true
 * @returns true iff the limit was exceeded
 */
function exceedsLimit(a1, a2, limit) {
	var i1 = 0;
	var i2 = 0; 
	var size = 0;
	for (; i1 < a1.length && i2 < a2.length; ) {
		var f1 = a1[i1];
		var f2 = a2[i2];
		
		if(f1.begin > f2.end) { // no overlap, f1 > f2
			size += sizeOf(f2);
			i2++;
		} else if(f2.begin > f1.end) { // no overlap, f2 > f1
			size += sizeOf(f1);
			i1++;
		} else { // overlap
			var left = Math.min(f1.begin, f2.begin);
			var right = Math.max(f1.end, f2.end);
			size += (right - left + 1);
			i1++;
			i2++;
		}
		
		if(size > limit) {
			return true;
		}
	}
	
	// Handle leftovers from first array
	for (; i1 < a1.length; i1++) {
		size += sizeOf(a1[i1]);
		if(size > limit) {
			return true;
		}
	}
	// Handle leftovers from second array
	for (; i2 < a2.length; i2++) {
		size += sizeOf(a2[i2]);
		if(size > limit) {
			return true;
		}
	}
	
	// We're clear and still inside the excerpt limit!
	return false;
}

function fragmentsSize(a) {
	var size = 0;
	for (var i=0; i < a.length; i++) {
		size += sizeOf(a[i]);
	}
	return size;
}

/**
 * Helper function to compute the overlapping size of two fragment arrays.
 * @param a1 first array of fragment objects
 * @param a2 second array of fragment objects
 * @returns the total number of slots covered by the two fragment arrays
 */
function combinedSize(a1, a2) {
	var i1 = 0;
	var i2 = 0; 
	var size = 0;
	for (; i1 < a1.length && i2 < a2.length; ) {
		var f1 = a1[i1];
		var f2 = a2[i2];
		
		if(f1.begin > f2.end) { // no overlap, f1 > f2
			size += sizeOf(f2);
			i2++;
		} else if(f2.begin > f1.end) { // no overlap, f2 > f1
			size += sizeOf(f1);
			i1++;
		} else { // overlap
			var left = Math.min(f1.begin, f2.begin);
			var right = Math.max(f1.end, f2.end);
			size += (right - left + 1);
			i1++;
			i2++;
		}
	}
	
	// Handle leftovers from first array
	for (; i1 < a1.length; i1++) {
		size += sizeOf(a1[i1]);
	}
	// Handle leftovers from second array
	for (; i2 < a2.length; i2++) {
		size += sizeOf(a2[i2]);
	}
	
	return size;
}

/**
 * Helper function to intersect two arrays of fragments.
 * @param a1 first array of fragment objects
 * @param a2 second array of fragment objects
 * @returns a list of fragments that only cover intersecting slots from a1 and a2
 */
function intersect(a1, a2) {
	var res = [];

	var i1 = 0;
	var i2 = 0; 
	
	for (; i1 < a1.length && i2 < a2.length; ) {
		var f1 = a1[i1];
		var f2 = a2[i2];
		
		if(f1.begin > f2.end) { // no overlap, f1 > f2
			i2++;
		} else if(f2.begin > f1.end) { // no overlap, f2 > f1
			i1++;
		} else { // overlap
			var left = Math.max(f1.begin, f2.begin);
			var right = Math.min(f1.end, f2.end);
			res[res.length] = toFragment(left, right);
			if(f1.end >= f2.end) {
				i2++;
			} 
			if(f2.end >= f1.end) {
				i1++;
			}
		}
	}
	
	return res;
}
