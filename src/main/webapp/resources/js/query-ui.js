/**
 * Methods for managing the query UI
 */
 
var globalHits = [];
var globalMappedHits = [];

function refreshResults() {	
	var rawCanvas = document.getElementById('form:rawHitsCanvas');
	var mappedCanvas = document.getElementById('form:mappedHitsCanvas');	
	if(rawCanvas == null || mappedCanvas == null) return;
	
	var encodedRawHits = document.getElementById('form:rawHits').value;	
	var encodedMappedHits = document.getElementById('form:mappedHits').value;	
	var rawHits = parseFragments(encodedRawHits);
	var mappedHits = parseFragments(encodedMappedHits);
		
	var rawSegments = Number(document.getElementById('form:rawSegments').value);

	paintExcerpt(rawCanvas, true, [], rawHits, rawSegments, OUTLINE_MATCHES);	
	paintExcerpt(mappedCanvas, true, [], mappedHits, rawSegments, OUTLINE_MATCHES);	
	
	console.log("refreshResults: encodedRawHits=%O rawHits=%O encodedMappedHits=%O mappedHits=%O range=%i", 
		encodedRawHits, rawHits, encodedMappedHits, mappedHits, rawSegments);	
}
	
function initResults() {	
	var rawCanvas = document.getElementById('form:rawHitsCanvas');
	var mappedCanvas = document.getElementById('form:mappedHitsCanvas');	
	if(rawCanvas == null || mappedCanvas == null) return;
	
	resizeCanvas(rawCanvas);
	resizeCanvas(mappedCanvas);
	
	refreshResults();
	
	console.log("initResults");
}

function refreshGlobalResults() {	
	var canvas = document.getElementById('form:globalHitsCanvas');			
	if(canvas == null) return;
	
	var encodedHits = document.getElementById('form:globalMappedHits').value;	
	var hits = parseFragments(encodedHits);
	//console.log(hits);
	
	var range = Number(document.getElementById('form:globalRawSegments').value);

	paintExcerpt(canvas, true, [], hits, range, OUTLINE_MATCHES);
	
	console.log("refreshGlobalResults: encodedHits=%O hits=%O range=%i", encodedHits, hits, range);	
}
	
function initGlobalResults() {		
	var canvas = document.getElementById('form:globalHitsCanvas');				
	if(canvas == null) return;
	
	resizeCanvas(canvas);
				
	globalHits = parseFragments(document.getElementById('form:globalHits').value);					
	globalMappedHits = parseFragments(document.getElementById('form:globalMappedHits').value);
				
	refreshGlobalResults();
	
	console.log("initGlobalResults: globalHits=%O globalMappedHits=%O", globalHits, globalMappedHits);
}
