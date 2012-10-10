SVG_NS = "http://www.w3.org/2000/svg";

YUI.add('document-ranges', function (Y) {		
	console.log('document-ranges');

	
	Faust.DocumentRanges = {
			buildVC: function(text) {

				var vc = new Faust.Surface();
				var zone = new Faust.Zone();
				vc.add(zone);
				
				var createText = function(content, node){
					var textAttrs = {};
					return new Faust.Text(content, textAttrs);				
				};
				
				
				Y.Array.each(text.textContent.split("\n"), function(line, n) {
					var linetext = createText(line);
					var line = new Faust.Line({});
					line.add(linetext);
					
					zone.add(line);					
					
				});				
				
				return vc;
			}
	};
	
}, '0.0', {
	requires: ["document-model"]
});


