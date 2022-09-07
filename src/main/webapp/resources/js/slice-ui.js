/**
 * Methods for managing the slice UI
 */
 

var quota = [];
var globalQuota = [];
var globalExcerpt = [];

function updateExcerpt(_, ui) {
	var begin = ui.values[0];
	var end = ui.values[1];
	refreshOutline(begin, end);
}

function refreshOutline(begin, end) {
	var count = end - begin + 1;
	var segments = Number(document.getElementById('slice:excerptSegments').value);
	var percent = count / segments * 100.0;
	var limit = Number(document.getElementById('slice:excerptThreshold').value);
	var canvas = document.getElementById('outline');
	var excerpt = [toFragment(begin, end)];
	var used = combinedSize(quota, excerpt, limit);
	var exceeded = used>limit;
	var style = exceeded ? OUTLINE_EXCEEDED : OUTLINE_EXCERPT;

	paintExcerpt(canvas, true, quota, excerpt, segments, style);	
	
	document.getElementById('slice:excerptSize').textContent = count + "/" + segments;
	document.getElementById('slice:excerptPercent').textContent = percent.toFixed(1) + "%";
	
	if(exceeded) {
		PF('wv_continue').disable();
	} else {
		PF('wv_continue').enable();
	}
	
	console.log("refreshOutline: begin=%i end=%i segments=%i percent=%f used=%i limit=%i excerpts=%O quota=%O", 
		begin, end, segments, percent, used, limit, excerpt, quota);
}

function initOutline() {	
	var canvas = document.getElementById('outline');				
	if(canvas == null) return;
		
	resizeCanvas(canvas);
				
	quota = parseFragments(document.getElementById('slice:excerptQuota').value);
	
	var begin = Number(document.getElementById('slice:excerptStart').value);
	var end = Number(document.getElementById('slice:excerptEnd').value);
	refreshOutline(begin, end);
	
	console.log("initOutline: begin=%i end=%i quota=%O", begin, end, quota);
}		

function refreshGlobalOutline() {
	var offset = Number(document.getElementById('slice:excerptOffset').value);
	var begin = Number(document.getElementById('slice:excerptStart').value) + offset;
	var end = Number(document.getElementById('slice:excerptEnd').value) + offset;
	var segments = Number(document.getElementById('slice:globalSegments').value);
	var limit = Number(document.getElementById('slice:globalThreshold').value);
	var canvas = document.getElementById('globalOutline');
	var excerpt = globalExcerpt.concat([toFragment(begin, end)]);
	var count = sizeOf(excerpt);
	
	var used = combinedSize(globalQuota, excerpt, limit);
	var percent = used / segments * 100.0;

	paintExcerpt(canvas, true, globalQuota, excerpt, segments, OUTLINE_EXCERPT);	
	
	document.getElementById('slice:globalSize').textContent = count + "/" + segments;
	document.getElementById('slice:globalPercent').textContent = percent.toFixed(1) + "%";
	if(globalQuota.length>0) {				
		var totalPercent = used / segments * 100.0;
		document.getElementById('slice:totalPercent').textContent = totalPercent.toFixed(1) + "%";
	}
	
	console.log("refreshGlobalOutline: begin=%i end=%i segments=%i percent=%f used=%i limit=%i globalExcerpt=%O globalQuota=%O", 
		begin, end, segments, percent, used, limit, globalExcerpt, globalQuota);
}

function initGlobalOutline() {		
	var canvas = document.getElementById('globalOutline');				
	if(canvas == null) return;
	
	resizeCanvas(canvas);
				
	globalQuota = parseFragments(document.getElementById('slice:globalQuota').value);					
	globalExcerpt = parseFragments(document.getElementById('slice:globalExcerpt').value);
				
	refreshGlobalOutline();
	
	console.log("initGlobalOutline: globalExcerpt=%O globalQuota=%O", globalExcerpt, globalQuota);
}

function initAllOutlines() {
	initOutline();
	initGlobalOutline();
}
	
