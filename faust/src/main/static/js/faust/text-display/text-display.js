YUI.add('text-display', function (Y) {

	var TextDisplayView = Y.Base.create('textDisplayView', Y.View, [], {
		/**
		 * Outputs an HTML representation of the text to the container
		 */
		render : function(start, end) {
			var text = this.get('text');
			var container = this.get('container');
			var prefix = this.get('cssPrefix');
			
			start = start || 0;
			end = end || text.contentLength;

			var partitions = text.applyAnnotations(null, start, end);
			
			function stageNum(name){
				stageNum.stages = stageNum.stages || {};
				stageNum.stagecount = stageNum.stagecount || 0;
				if(stageNum.stages[name] === undefined)
					stageNum.stages[name] = stageNum.stagecount++;
				return stageNum.stages[name];
			}
			
			Y.Array.each(partitions, function(partition, i, partitions){
				var partitionNode = Y.Node.create('<span></span>')
				container.append(partitionNode);
				var lineNumNode = null;
				
				function isFirst(annotation)
				{
					return i == 0 || Y.Array.indexOf(partitions[i-1].annotations, annotation) == -1;
				}
				function isLast(annotation)
				{
					return i+1 == partitions.length || Y.Array.indexOf(partitions[i+1].annotations, annotation) == -1;
				}
				
				Y.Array.each(partition.range.of(text.content).split('\n'), function(line, n) {
					if(n > 0)
						partitionNode.append('<br>');
					
					var lineNode = Y.config.doc.createTextNode(line);
					partitionNode.append(lineNode);
					
					var classes = [];
					Y.Array.each(partition.annotations, function(annotation, annotationNum){
						
						var name = prefix + annotation.name.localName;
						
						classes.push(name);
						classes.push(prefix + annotation.id);
						
						if(isFirst(annotation))
							classes.push(name + '-first');
						if(isLast(annotation))
							classes.push(name + '-last');
						
						if(annotation.name.localName == 'stage')
							classes.push(name + '-' + stageNum(annotation.data['value']));
						
						if(annotation.name.localName == 'l' && isFirst(annotation))
							partitionNode.insert('<span class="linenum">'+parseInt(annotation.data['n'])+'</span>', lineNode);

					});
					partitionNode.addClass(classes.join(' '));
				});
			});
		}
    }, {
		ATTRS: {
			container: {}, /// reference to HTML node acting as output container
			text: {}, /// instance of Y.Faust.Text
			cssPrefix: {value: ""}, /// CSS prefix string
		}
	});
	
	Y.mix(Y.namespace("Faust"), {
		TextDisplayView: TextDisplayView
	});
}, '0.0', {
	requires: ['base', 'view']
});