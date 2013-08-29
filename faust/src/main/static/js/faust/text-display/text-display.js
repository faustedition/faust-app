YUI.add('text-display', function (Y) {

    var TextDisplayView = Y.Base.create('textDisplayView', Y.View, [], {
		render : function() {
			this.get('container').append(this.get('text').content);
		}
    }, {
		ATTRS: {
			container: {},
			text: {}
		}
	});
	
    Y.mix(Y.namespace("Faust"), {
        TextDisplayView: TextDisplayView
    });
}, '0.0', {
    requires: ['base', 'view']
});
