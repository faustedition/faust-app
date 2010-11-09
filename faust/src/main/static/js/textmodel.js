Faust.YUI().use("oop", function(Y) {
	Faust.ViewComponent = function() {};
	Faust.ViewComponent.prototype = {
		initViewComponent: function(node) {
			this.parent = null;
			this.pos = -1;
			this.children = [];
			this.node = node;					
		},
		add: function(vc) {
			vc.parent = this;
			vc.pos = this.children.length;
			this.children.push(vc);
		},
		previous: function() {
			return (this.parent == null || this.pos <= 0) ? null : this.parent.children[this.pos - 1];
		},
		next: function() {
			return (this.parent == null || (this.pos + 1) >= this.parent.children.length) ? null : this.parent.children[this.pos + 1];			
		}
	};
	
	Faust.Surface = function(node) {
		this.initViewComponent(node);
	};
	Y.augment(Faust.Surface, Faust.ViewComponent);
	
	Faust.Zone = function(node) {
		this.initViewComponent(node);
	};
	Y.augment(Faust.Zone, Faust.ViewComponent);
	
	Faust.Line = function(node) {
		this.initViewComponent(node);
	};
	Y.augment(Faust.Line, Faust.ViewComponent);
	
	Faust.Text = function(node) {
		this.initViewComponent(node);
		this.text = node.text().replace(/\s+/, " ");
	};
	Y.augment(Faust.Text, Faust.ViewComponent);
});
