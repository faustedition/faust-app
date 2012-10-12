SVG_NS = "http://www.w3.org/2000/svg";

YUI.add('document-ranges', function (Y) {		
	console.log('document-ranges');

	
	Faust.DocumentRanges = {
			
			
			transcriptVC: function(jsonRepresentation) {

				function textVC(content, node){
					var textAttrs = {};
					return new Faust.Text(content, textAttrs);		
				};

				
				transcript = Y.Faust.Text.create(jsonRepresentation);
				
				var surfaceVC = new Faust.Surface();
				var zoneVC = new Faust.Zone();
				surfaceVC.add(zoneVC);

				//for all lines
				
				Y.Array.each(transcript.find(null, null, ["line"]), function(line) {				
					var linetextVC = textVC(line.target().textContent());
					var lineVC = new Faust.Line({});
					
					//find the zone of the line
					
					var linestart = line.target().range.start;
					var lineend = line.target().range.end;
					
					zones = transcript.find()
					
					lineVC.add(linetextVC);
					zoneVC.add(lineVC);
				});				
				

				return surfaceVC;
			}
	};
	
}, '0.0', {
	requires: ["document-model", "text-annotation"]
});


