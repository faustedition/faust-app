Faust.YUI().use("node", "dom", "event", "overlay", "dump", function(Y) {
	Faust.ReadingTextPanel = function(container, graph){
		this.container = Y.one(container);
		this.graph = graph;
	};
	Faust.ReadingTextPanel.prototype = {
		render: function() {
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
			}, this);
		},
	};
	
	Faust.GeneticGanttChart = function(container, graph) {
		this.container = container;
		this.gr = graph.geneticRelations;
	};
	Faust.GeneticGanttChart.prototype = {
		render: function() {
			var documents = this.gr.related;
			if (documents.length == 0) return;			
			documents.sort(function(a, b) { 
				if (a.sigil < b.sigil) return -1; 
				else if (a.sigil > b.sigil) return 1; 
				return 0; 
			});
			
			var width = 350;
			var height = 20 * documents.length;
			var lineScale = pv.Scale.linear(this.gr.interval[0], this.gr.interval[1]).range(0, width);
			var barColor = pv.Scale.linear(0, documents.length).range('#805F40', '#999');
			
			var panel = new pv.Panel().canvas(this.container)
				.width(width)
				.height(height)
				.top(25)
				.bottom(25)
				.left(50);
			
			panel.add(pv.Rule)
				.data(pv.range(documents.length))
				.strokeStyle("#eee")
				.top(function(d) { return d * this.parent.height() / documents.length })
				.anchor("left")
					.add(pv.Label)
					.text(function(d) { return documents[d].sigil })
					.textMargin(10);
		
			panel.add(pv.Rule)
				.data(lineScale.ticks(5))
				.strokeStyle("#eee")
				.left(lineScale)
				.anchor("bottom")
					.add(pv.Label)
					.text(lineScale.tickFormat);
					
			panel.add(pv.Panel)
				.data(this.gr.related)
				.height(height / (documents.length * 2))
				.top(function(d) { return  (this.index * this.parent.height() / documents.length) - this.height() / 2 })
					.add(pv.Bar)
					.data(function(d) { return d.intervals })
					.left(function(d) { return lineScale(d[0]) })
					.width(function(d) { return lineScale(d[1]) - lineScale(d[0]) })
					.fillStyle(function() { return barColor(this.parent.index) })
					.event("click", function(n) { 
						var d = this.parent.data();
						alert("==> " + d.document + " [ " + d.sigil + " ]; " + Y.dump(n));
					});
										
			panel.render();			
		}
	}
	Faust.GeneticTree = function(container, graph) {
		this.container = container;
		this.gr = graph.geneticRelations;
	}
	Faust.GeneticTree.prototype = {
		render: function() {
			if (this.gr.related.length == 0) return;
			var maxLines = (this.gr.interval[1] - this.gr.interval[0] + 1);
			var minLines = maxLines;
			
			var nodes = {};			
			Y.each(this.gr.related, function(d) {
				var lines = 0;
				Y.each(d.intervals, function(i) { lines = lines + i[1] - i[0] + 1 });
				minLines = Math.min(minLines, lines);
				nodes[d.sigil] = lines ;
			});

			var panel = new pv.Panel().canvas(this.container)
			    .width(350)
			    .height(400);

			var tree = panel.add(pv.Layout.Tree)
			    	.nodes(pv.dom(nodes).root("Text").nodes())
				.depth(100)
				.breadth(25)
				.group(0)
				.orient("radial");
			

			tree.link.add(pv.Line);

			tree.node.add(pv.Dot)
				.title(function(d) { return d.nodeName; })
				.event("click", function(n) {  alert("==> " + n.nodeName); });
				
				//.event("mousedown", pv.Behavior.drag())
				//.event("drag", force);

			tree.label.add(pv.Label);
			

			panel.render();			
		}
	}
});