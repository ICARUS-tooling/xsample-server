/**
 * Methods for managing the query UI
 */
 
var globalRawHits = [];
var globalMappedHits = [];

function updateExcerpt(_, ui) {
	var begin = ui.values[0];
	var end = ui.values[1];
	refreshExcerpt(begin, end);
}

function refreshExcerpt(begin, end) {
	var excerptCanvas = document.getElementById('excerptCanvas');					
	if(excerptCanvas == null) {
		console.log("refreshExcerpt: missing excerptCanvas");
		return;	
	}
	
	var filter = [toFragment(begin, end)];
	var encodedMappedHits = document.getElementById('form:mappedHits').value;	
	var mappedHits = parseFragments(encodedMappedHits);
	var excerpt = intersect(mappedHits, filter);
	var count = sizeOf(excerpt);
	var segments = Number(document.getElementById('form:excerptSegments').value);
	var percent = count / segments * 100.0;
	var limit = Number(document.getElementById('form:excerptThreshold').value);
	var used = combinedSize(quota, excerpt, limit);
	var exceeded = used>limit;
	var style = exceeded ? OUTLINE_EXCEEDED : OUTLINE_EXCERPT;

	paintExcerpt(excerptCanvas, true, quota, excerpt, segments, style);	
	
	document.getElementById('form:excerptSize').textContent = count + "/" + segments;
	document.getElementById('form:excerptPercent').textContent = percent.toFixed(1) + "%";
	
	if(exceeded) {
		PF('wv_continue').disable();
	} else {
		PF('wv_continue').enable();
	}
	
	console.log("refreshExcerpt: begin=%i end=%i segments=%i percent=%f used=%i limit=%i excerpts=%O quota=%O", 
		begin, end, segments, percent, used, limit, excerpt, quota);
}

function initExcerpt() {					
	var excerptCanvas = document.getElementById('excerptCanvas');					
	if(excerptCanvas == null) {
		console.log("initExcerpt: missing excerptCanvas");
		return;	
	}
		
	resizeCanvas(excerptCanvas);
				
	quota = parseFragments(document.getElementById('form:excerptQuota').value);
	
	var begin = Number(document.getElementById('form:excerptStart').value);
	var end = Number(document.getElementById('form:excerptEnd').value);
	refreshExcerpt(begin, end);
	
	console.log("initExcerpt: begin=%i end=%i quota=%O", begin, end, quota);
}

function refreshResults() {	
	var rawCanvas = document.getElementById('rawHitsCanvas');
	var mappedCanvas = document.getElementById('mappedHitsCanvas');	
	if(rawCanvas == null || mappedCanvas == null) {
		console.log("refreshResults: missing rawHitsCanvas and mappedHitsCanvas");
		return;	
	}
	
	var encodedRawHits = document.getElementById('form:rawHits').value;	
	var encodedMappedHits = document.getElementById('form:mappedHits').value;	
	var rawHits = parseFragments(encodedRawHits);
	var mappedHits = parseFragments(encodedMappedHits);
		
	var rawSegments = Number(document.getElementById('form:rawSegments').value);
	var segments = Number(document.getElementById('form:excerptSegments').value);

	paintExcerpt(rawCanvas, true, [], rawHits, rawSegments, OUTLINE_MATCHES);	
	paintExcerpt(mappedCanvas, true, [], mappedHits, segments, OUTLINE_MATCHES);	
	
	document.getElementById('form:rawHitsCount').textContent = sizeOf(rawHits) + "/" + rawSegments;
	document.getElementById('form:mappedHitsCount').textContent = sizeOf(mappedHits) + "/" + segments;
	
	console.log("refreshResults: encodedRawHits=%O rawHits=%O encodedMappedHits=%O mappedHits=%O rawSegments=%i segments=%i", 
		encodedRawHits, rawHits, encodedMappedHits, mappedHits, rawSegments, segments);	
}
	
function initResults() {	
	var rawCanvas = document.getElementById('rawHitsCanvas');
	var mappedCanvas = document.getElementById('mappedHitsCanvas');	
	if(rawCanvas == null || mappedCanvas == null) {
		console.log("initResults: missing rawHitsCanvas and mappedHitsCanvas");
		return;	
	}
	
	resizeCanvas(rawCanvas);
	resizeCanvas(mappedCanvas);
	
	refreshResults();
	
	console.log("initResults");
}

function refreshGlobalResults() {	
	// IMPORTANT NOTE: we only visualize the mapped segments here, not the raw ones, as the latter could render the UI unreadable 
	
	var canvas = document.getElementById('globalHitsCanvas');				
	if(canvas == null) {
		console.log("refreshGlobalResults: missing globalHitsCanvas");
		return;	
	}
	
	var encodedHits = document.getElementById('form:globalMappedHits').value;	
	var hits = parseFragments(encodedHits);
	//console.log(hits);
	
	var range = Number(document.getElementById('form:globalSegments').value);

	paintExcerpt(canvas, true, [], hits, range, OUTLINE_MATCHES);
	
	console.log("refreshGlobalResults: encodedHits=%O hits=%O range=%i", encodedHits, hits, range);	
}
	
function initGlobalResults() {		
	var canvas = document.getElementById('globalHitsCanvas');				
	if(canvas == null) {
		console.log("initGlobalResults: missing globalHitsCanvas");
		return;	
	}
	
	resizeCanvas(canvas);
				
	globalRawHits = parseFragments(document.getElementById('form:globalRawHits').value);					
	globalMappedHits = parseFragments(document.getElementById('form:globalMappedHits').value);
				
	refreshGlobalResults();
	
	console.log("initGlobalResults: globalRawHits=%O globalMappedHits=%O", globalRawHits, globalMappedHits);
}
