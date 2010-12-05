Faust.YUI().use("node", "dom", "event", "overlay", "dump", function(Y) {
	Faust.ReadingTextPanel = function(container, graph) {
		this.container = Y.one(container);
		this.graph = graph;
	};
	Faust.ReadingTextPanel.prototype = {
		render: function() {
			this.container.get("children").each(function(e) { e.remove(); e.destroy(true);  });
			Y.each(this.graph.root("f:lines").children, function(l) {
				var p = Y.Node.create("<p></p>");
				p.addClass("line");
				Y.each(l.children, function(t, idx) {
					var tSpan = Y.Node.create("<span></span>");							
					tSpan.append(document.createTextNode(t.node.content.replace(/\u0097/g, "–")));
					p.append(tSpan);
					
					if (t.node.content.replace(/\s+/g, "") == '') return;
					Y.each(t.node.ancestors(), function(a) {
						var el = a.node;
						if (idx == 0) {
							if (el.name == "tei:head") {
								p.addClass("head");
							} else if (el.name == "tei:l") {
								var dramaTree = el.trees[0];
								if (dramaTree.p.node.name == "tei:lg" &&
									dramaTree.p.children[0] === dramaTree) {
										p.addClass("para-start");
									}
							}			
						}
						if (el.name == "tei:stage") {
							tSpan.addClass("stage");
						} else if (el.name == "tei:speaker") {
							p.addClass("para-start");
							tSpan.addClass("speaker");
						}
						
					})
				});
				this.container.append(p);
				p.on("click", function(e) {
					this.select(l, p);
				}, this);	
			}, this);
		},
		select: function(line, p) {
			alert(line.text());
		}		
	};
});