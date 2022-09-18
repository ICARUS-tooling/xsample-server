/**
 * Methods for managing the slice UI
 */

var quota = [];
var globalQuota = [];
var globalExcerpt = [];

function updateExcerpt(_, ui) {
	var begin = ui.values[0];
	var end = ui.values[1];
	refreshExcerpt(begin, end);
}

function refreshExcerpt(begin, end) {
	var canvas = document.getElementById('excerptCanvas');					
	if(canvas == null) {
		console.log("refreshExcerpt: missing excerptCanvas");
		return;	
	}
	
	var count = end - begin + 1;
	var segments = Number(document.getElementById('form:excerptSegments').value);
	var percent = count / segments * 100.0;
	var limit = Number(document.getElementById('form:excerptThreshold').value);
	var excerpt = [toFragment(begin, end)];
	var used = combinedSize(quota, excerpt, limit);
	var exceeded = used>limit;
	var style = exceeded ? OUTLINE_EXCEEDED : OUTLINE_EXCERPT;

	paintExcerpt(canvas, true, quota, excerpt, segments, style);	
	
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
	var canvas = document.getElementById('excerptCanvas');				
	if(canvas == null) {
		console.log("initExcerpt: missing excerptCanvas");
		return;	
	}
		
	resizeCanvas(canvas);
				
	quota = parseFragments(document.getElementById('form:excerptQuota').value);
	
	var begin = Number(document.getElementById('form:excerptStart').value);
	var end = Number(document.getElementById('form:excerptEnd').value);
	refreshExcerpt(begin, end);
	
	console.log("initExcerpt: begin=%i end=%i quota=%O", begin, end, quota);
}		

function refreshGlobalExcerpt() {
	var canvas = document.getElementById('globalExcerptCanvas');					
	if(canvas == null) {
		console.log("refreshGlobalExcerpt: missing globalExcerptCanvas");
		return;	
	}
	
	//var offset = Number(document.getElementById('form:excerptOffset').value);
	//var begin = Number(document.getElementById('form:excerptStart').value) + offset;
	//var end = Number(document.getElementById('form:excerptEnd').value) + offset;
	var segments = Number(document.getElementById('form:globalSegments').value);
	var limit = Number(document.getElementById('form:globalThreshold').value);
	var excerpt = globalExcerpt; //.concat([toFragment(begin, end)]);
	var count = sizeOf(excerpt);
	
	var used = combinedSize(globalQuota, excerpt, limit);
	var percent = used / segments * 100.0;

	paintExcerpt(canvas, true, globalQuota, excerpt, segments, OUTLINE_EXCERPT);	
	
	document.getElementById('form:globalSize').textContent = count + "/" + segments;
	document.getElementById('form:globalPercent').textContent = percent.toFixed(1) + "%";
	if(globalQuota.length>0) {				
		var totalPercent = used / segments * 100.0;
		document.getElementById('form:totalPercent').textContent = totalPercent.toFixed(1) + "%";
	}
	
	console.log("refreshGlobalExcerpt: segments=%i percent=%f used=%i limit=%i globalExcerpt=%O globalQuota=%O", 
		segments, percent, used, limit, globalExcerpt, globalQuota);
}

function initGlobalExcerpt() {		
	var canvas = document.getElementById('globalExcerptCanvas');				
	if(canvas == null) {
		console.log("initGlobalExcerpt: missing globalExcerptCanvas");
		return;	
	}
	
	resizeCanvas(canvas);
				
	globalQuota = parseFragments(document.getElementById('form:globalQuota').value);					
	globalExcerpt = parseFragments(document.getElementById('form:globalExcerpt').value);
				
	refreshGlobalExcerpt();
	
	console.log("initGlobalExcerpt: globalExcerpt=%O globalQuota=%O", globalExcerpt, globalQuota);
}
