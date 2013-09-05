YUI.add('text-display', function (Y) {

	var TextDisplayView = Y.Base.create('textDisplayView', Y.View, [], {
		/**
		 * Outputs an HTML representation of the text to the container
		 */
		render : function() {
			var text = this.get('text');
			var container = this.get('container');
			var prefix = this.get('cssPrefix');

			var partitions = text.applyAnnotations();
			
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
				var content = partition.range.of(text.content);
				content = content.replace('\n', '<br/>');
				partitionNode.setHTML(content); // fixme: this swallows whitespace
				Y.Array.each(partition.annotations, function(annotation){
					
					var name = prefix + annotation.name.localName;
					
					partitionNode.addClass(name);
					
					if(i == 0 || Y.Array.indexOf(partitions[i-1].annotations, annotation) == -1)
						partitionNode.addClass(name + '-first');
					if(i+1 == partitions.length || Y.Array.indexOf(partitions[i+1].annotations, annotation) == -1)
						partitionNode.addClass(name + '-last');
					
					if(annotation.name.localName == 'stage')
						partitionNode.addClass(name + '-' + stageNum(annotation.data['value']));
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