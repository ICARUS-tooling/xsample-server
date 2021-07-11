
var OUTLINE_BG = getCSSVariable('--outline-bg');
var OUTLINE_QUOTA = getCSSVariable('--outline-quota');
var OUTLINE_EXCERPT = getCSSVariable('--outline-excerpt');
var OUTLINE_EXCEEDED = getCSSVariable('--outline-exceeded');
var OUTLINE_MATCHES = getCSSVariable('--outline-matches');

/**
 * Paint a horizontal outline on the canvas
 * 
 * @param canvas the canvas widget to paint on
 * @param quota array of fragment pairs
 * @param excerpt array of fragment pairs
 * @param range the total number of segments available
 * @param style the style to  use for the excerpt fragments
 */
function paintExcerpt(canvas, quota, excerpt, range, style){
	var ctx = canvas.getContext("2d");
	// Reset canvas for new paint pass
	ctx.clearRect(0, 0, canvas.width, canvas.height);
	ctx.globalCompositeOperation = 'source-over';

	// Width in pixels of a single segment
	var step = canvas.width / range;
	
//	console.log("width=%d range=%d step=%f", canvas.width, range, step);
	
	// Utility function to paint a continuous range of segments
	var paintSpan = (begin, end, style) => {
		var x = (begin-1) * step;
		var width = (end - begin + 1) * step;
		ctx.fillStyle = style;
		ctx.fillRect(x, 0, width, canvas.height);
		
//		console.log("(%d, %d) -> [%d, %d]", begin, end, x, x+width);
	}

	excerpt.forEach(f => paintSpan(f.begin, f.end, style));
	
	/* globalCompositeOperation :
	  normal | multiply | screen | overlay | 
	  darken | lighten | color-dodge | color-burn | hard-light | 
	  soft-light | difference | exclusion | hue | saturation | 
	  color | luminosity
	*/
	ctx.globalCompositeOperation = 'multiply';
	quota.forEach(f => paintSpan(f.begin, f.end, OUTLINE_QUOTA));
}


function resizeCanvas(canvas) {
	//canvas.width = canvas.parentNode.offsetWidth * 0.9;
	canvas.width = canvas.offsetWidth;
}