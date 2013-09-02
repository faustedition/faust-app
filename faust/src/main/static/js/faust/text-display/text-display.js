YUI.add('text-display', function (Y) {

	var TextDisplayView = Y.Base.create('textDisplayView', Y.View, [], {
		/**
		 * Outputs an HTML representation of the text to the container
		 */
		render : function() {
			var text = this.get('text');
			var container = this.get('container');

			var partitions = text.applyAnnotations();
			
			Y.Array.each(partitions, function(partition){
				var partitionNode = Y.Node.create('<span></span>')
				container.append(partitionNode);
				var content = partition.range.of(text.content);
				content = content.replace('\n', '<br/>');
				partitionNode.setHTML(content); // fixme: this swallows whitespace
				Y.Array.each(partition.annotations, function(annotation){
					partitionNode.addClass(annotation.name.localName);
				});
			});
		}
    }, {
		ATTRS: {
			container: {}, /// reference to HTML node acting as output container
			text: {}, /// instance of Y.Faust.Text
		}
	});
	
	Y.mix(Y.namespace("Faust"), {
		TextDisplayView: TextDisplayView
	});
}, '0.0', {
	requires: ['base', 'view']
});