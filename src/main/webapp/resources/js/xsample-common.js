/**
 * Utility functions for communicating with the XSample server
 * and encoding/decoding excerpt data. 
 */

const toFragment = (begin, end) => {
	return {begin:begin, end:end};
}

/**
 * Parses a single fragment
 * @param input raw string encoding the fragment
 * @returns a fragment object with 'begin' and 'end' values
 */
const parseFragment = (input) => {
	var sep = input.indexOf("-");
	if(sep==-1) {
		var num = Number(str);
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
const parseFragments = (input) => {
	return input.split(',').map(parseFragment);
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
const findFragment = (quota, segment, fromIndex, toIndex) => {
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

const OUTLINE_BG = "#3399ff";
const OUTLINE_QUOTA = "#FFAA2A";
const OUTLINE_EXCERPT = "#00FF00";
const OUTLINE_EXCEEDED = "#FF0000";

/**
 * Paint a horizontal outline on the canvas
 * 
 * @param canvas the canvas widget to paint on
 * @param quota array of fragment pairs
 * @param excerpt array of fragment pairs
 * @param range the total number of segments available
 * @param limit maximum number of segments allowed in quota + excerpt
 */
const paintExcerpt = (canvas, quota, excerpt, range, limit) => {;
	var ctx = canvas.getContext("2d");
	//
	ctx.fillStyle = OUTLINE_BG;
	ctx.fillRect(0, 0, canvas.width, canvas.height);

	// Width in pixels of a single segment
	var step = canvas.width / range;
	
	// Utility function to paint a continuous range of segments
	var paintSpan = (begin, end, style) => {
		var x = (begin-1) * step;
		var width = (end - begin + 1) * step;
		ctx.fillStyle = style;
		ctx.fillRect(x, 0, width, c.height);
	}
	
	var count = excerpt.reduce((total, f) => total + (f.end - f.begin + 1), 0);
	var style = (count>limit) ? OUTLINE_EXCEEDED : OUTLINE_EXCERPT;
	excerpt.forEach(f => paintSpan(f.begin, f.end, style));
	//console.log(ctx);
}