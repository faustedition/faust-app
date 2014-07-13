<@faust.page title="Genetic Analysis" menuhighlight="genesis"  layout="wide">

<style type="text/css">
	#main {position:  static !important;}
	#genetic-analysis-app { margin-top: 4em; padding: 1em; }
	.yui3-app-views { overflow: hidden; }
	.title { text-align: center; font-size: 108%; }
	.title li { display: inline-block; margin: 0 2em }
	.scene-statistics { position: relative; width: 800px; height: 400px; }
	.verse-chart { overflow: auto; }
</style>


<div id="genetic-analysis-app">

	<div id="navigation" style="text-align: center">
		<button class="pure-button" id="button_left"><i class="icon-arrow-left"></i></button>
		<button class="pure-button" id="button_zoom_in"><i class="icon-zoom-in"></i></button>
		<button class="pure-button" id="button_zoom_out"><i class="icon-zoom-out"></i></button>
		<button class="pure-button" id="button_right"><i class="icon-arrow-right"></i></button>
	</div>

</div>

<script type="text/javascript">
	YUI().use("app", "node", "event", "json", "io", "charts", "array-extras", "substitute", "protovis", "interaction", function(Y) {

		var FAUST_MIN_LINE_NUM = 1;
		var FAUST_MAX_LINE_NUM = 12500;

		var partTitles = [ "Faust - Prolog", "Faust I", "Faust II"];

		Y.VerseView = Y.Base.create("genetic-anaylsis-verse-view", Y.View, [], {
			render: function() {
				var container = this.get("container");
				this._chartNode = container.appendChild("<div/>").addClass("verse-chart");
				Y.io(cp + Y.substitute("/transcript/by-verse/{from}/{to}", this.getAttrs()), {
					headers: {
						Accept: "application/json"
					},
					on: {
						success: Y.bind(this._verseDataReceived, this)
					}
				});
			},
			_verseDataReceived: function(id, response) {
				var data = [
					{ sigil: "V.H15", intervals : [
						{ url: "faust/2.5/gsa_391506.xml#1", interval: [11519, 11526] }
					] },
					{ sigil: "V.H13v", intervals: [
						{ url: "faust/2.5/gsa_391027.xml#3", interval: [11511, 11530] }
					] },
					{ sigil: "V.H14", intervals: [
						{ url: "faust/2.5/gsa_391505.xml#1", interval: [11511, 11530] }
					] },
					{ sigil: "V.H18", intervals: [
						{ url: "faust/2.5/gsa_390757.xml#1", interval: [11595, 11603] }
					] },
					{ sigil: "V.H17r", intervals: [
						{ url: "faust/2.5/gsa_391510.xml#1", interval: [11593, 11595] }
					] },
					{ sigil: "V.H2", intervals: [
						{ url: "faust/2.5/gsa_390883.xml#12", interval: [11511, 11530] },
						{ url: "faust/2.5/gsa_390883.xml#13", interval: [11539, 11590] },
						{ url: "faust/2.5/gsa_390883.xml#14", interval: [11593, 11603] }
					] },
					{ sigil: "V.H16", intervals: [
						{ url: "faust/2.5/gsa_391507.xml#1", interval: [11573, 11576] }
					] },
					{ sigil: "V.H", intervals: [
						{ url: "faust/2/gsa_391098.xml#360", interval: [11511, 11522] },
						{ url: "faust/2/gsa_391098.xml#415", interval: [11523, 11543] },
						{ url: "faust/2/gsa_391098.xml#416", interval: [11544, 11562] },
						{ url: "faust/2/gsa_391098.xml#417", interval: [11563, 11586] },
						{ url: "faust/2/gsa_391098.xml#418", interval: [11587, 11593] },
						{ url: "faust/2/gsa_391098.xml#419", interval: [11594, 11619] }
					] }
				];
				var verseData = Y.JSON.parse(response.responseText);


				minLine = this.get('from');
				maxLine = this.get('to');
			    var chartWidth = parseInt(Y.one("#genetic-analysis-app").getComputedStyle('width')) - 200;
				var width = (maxLine - minLine) * 5;
				var height = verseData.length * 20;
				var lineScale = pv.Scale.linear(minLine, maxLine).range(0, chartWidth);

				var panel = new pv.Panel().canvas(this._chartNode.getDOMNode())
					.width(chartWidth)
					.height(height)
					.top(25)
					.bottom(25)
					.left(100);

				panel.add(pv.Rule)
					.data(pv.range(verseData.length))
					.strokeStyle("#eee")
					.top(function(d) { return d * this.parent.height() / verseData.length })
					.anchor("left")
					.add(pv.Label)
					.text(function(d) { return verseData[d].sigil; })
					.textMargin(10);

				panel.add(pv.Rule)
					.data(lineScale.ticks())
					.strokeStyle("#eee")
					.left(lineScale)
					.anchor("bottom")
					.add(pv.Label)
					.text(lineScale.tickFormat);

				panel.add(pv.Panel)
					.data(verseData)
					.height(height / (verseData.length * 2))
					.top(function(d) { return  (this.index * this.parent.height() / verseData.length) - this.height() / 2 })
					.event("click", function(d) { Y.getLocation().assign(cp + verseData[this.index].source.replace('faust://xml', '', '')); })
					.add(pv.Bar)
					.data(function(m) { return m.intervals })
					.left(function(d) { return lineScale(d.start) })
					// intervals are "closed", i.e. they include end and start line, thus width = end - start + 1
					.width(function(d) { return (lineScale(d.end + 1) - lineScale(d.start)) })
					.title(function (d) {return '[' + d.start + ', ' + d.end + ']'})
					.fillStyle(function() { return '#000'});

				panel.render();

				this._chartNode.all('svg a').on('mousemove', function(e) {
					Y.fire('faust:mouseover-info',
							{
								info: e.currentTarget.getAttribute("title"),
								mouseEvent: e
							});
				});

				this._chartNode.all('svg a').on('mouseleave', function(e) {
					Y.fire('faust:mouseover-info-hide', {});
				});

			}
		}, {
			ATTRS: {
				from: {
					validator: function(v) { return Y.Lang.isNumber(v) && (v >= 0); },
					setter: function(v) {return Math.max(FAUST_MIN_LINE_NUM, v);}
				},
				to: {
					validator: function(v) { return Y.Lang.isNumber(v) && (v >= 0); },
					setter: function(v) {return Math.min(FAUST_MAX_LINE_NUM, v);}
				}
			}
		});

		Y.PartView = Y.Base.create("genetic-analysis-part-view", Y.View, [], {
			destructor: function() {
				this._chart && this._chart.destroy();
			},
			render: function() {
				var container = this.get("container"), part = this.get("part");

				Y.Array.each(partTitles, function(title, i) {
					var element = this.appendChild("<li/>");
					if (part != i) {
						element = element.appendChild("<a/>").setAttrs({
							href: "/" + i,
							title: partTitles[i]
						});
					}
					element.set("text", ">> " + partTitles[i]);
				}, container.appendChild("<ul/>").addClass("title"));

				this._sceneStatistics = container.appendChild("<div/>").addClass("scene-statistics");
				Y.io(cp + "/transcript/by-scene/" + part, {
					headers: {
						Accept: "application/json"
					},
					on: {
						success: Y.bind(this._renderChart, this)
					}
				});
			},
			_renderChart: function(id, resp) {
				this._chart = new Y.Chart({
					render: this._sceneStatistics,
					type: "bar",
					dataProvider: Y.JSON.parse(resp.responseText),
					categoryKey: "scene",
					styles: {
						graph: {
							background: {
								fill: {
									color: "#fff"
								}
							}
						},
						series: {
							documents: {
								marker: {
									fill: {
										color: "#805F40"
									},
									over: {
										fill: {
											color: "#000"
										}
									}
								}

							}
						}
					},
					horizontalGridlines: true,
					verticalGridlines: true
				});
			}
		}, {
			ATTRS: {
				part: {
					validator: function(v) {
						return Y.Lang.isNumber(v) && (v < partTitles.length);
					}
				}
			}
		});

		Y.on("contentready", function() {
			var app = new Y.App({
				container: this,
				root: cp + "/genesis/app/",
				linkSelector: ("#" + this.get("id") + " a"),
				transitions: true,
				serverRouting: false,
				views: {
					verse: { type: "VerseView" },
					part: { type: "PartView" }
				}
			});

			Y.one('#button_zoom_in').on('click', function(e){
				var view = app.get('activeView'); var from = view.get("from");
				var to = view.get("to"); var change = Math.ceil((to - from) * 0.1);
				app.navigate("/" + (from + change) + "/" +  (to - change))
			});

			Y.one('#button_zoom_out').on('click', function(e){
				var view = app.get('activeView'); var from = view.get("from");
				var to = view.get("to"); var change = Math.ceil((to - from) * 0.1);
				app.navigate("/" + (from - change) + "/" +  (to + change))
			});

			Y.one('#button_left').on('click', function(e){
				var view = app.get('activeView'); var from = view.get("from");
				var to = view.get("to"); var change = Math.ceil((to - from) * 0.1);
				app.navigate("/" + (from - change) + "/" +  (to - change))

			});

			Y.one('#button_right').on('click', function(e){
				var view = app.get('activeView'); var from = view.get("from");
				var to = view.get("to"); var change = Math.ceil((to - from) * 0.1);
				app.navigate("/" + (from + change) + "/" +  (to + change))

			});


			app.route("/", function() {
				this.navigate("/5000/7000");
			});

			app.route("/:from/:to", function(req) {
				var from = parseInt(req.params.from);
				var to = parseInt(req.params.to);
				if (from < FAUST_MIN_LINE_NUM || to > FAUST_MAX_LINE_NUM || from >= to) {
					var correctedFrom = Math.min(Math.max(FAUST_MIN_LINE_NUM, from), to - 1);
					var correctedTo = Math.max(Math.min(to, FAUST_MAX_LINE_NUM), from + 1);
					this.navigate("/" + correctedFrom +
							"/" + correctedTo);
					return;
				}
				this.showView("verse", { from: from, to: to });
			});
			app.route("/:part", function(req) {
				this.showView("part", { part: parseInt(req.params.part) });
			});
			app.render().dispatch();
		}, "#genetic-analysis-app");
	});
</script>

</@faust.page>