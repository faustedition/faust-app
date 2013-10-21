YUI.add('document-app', function (Y) {

	Faust.ENC_EXC_PREF = "ENCODING ERROR: ";

	Y.NavigationModel = Y.Base.create('navigationModel', Y.Model, [], {

	}, {
		ATTRS: {

			numberOfPages: {
				value: 1,
				validator: function (value) {
					return Y.Lang.isNumber(value);
				}
			},

			pagenumber: {
				value: 1,
				validator: function (value) {
					return Y.Lang.isNumber(value) && value > 0 && value <= this.get('numberOfPages');
				}
			},

			viewMode: {
				value: 'transcript',
				validator: function (value) {
					return Y.Lang.isNumber(value) && value in {'transcript': 1, 'facsimile': 1}
				}
			}
		}
	});

	Y.NavigationView = Y.Base.create("navigation-view", Y.View, [], {


		render: function () {

			var model = this.get('model');

			var container = Y.one(this.get('container'));


			container.append('<div style="margin:0em 0em; width:600">' +
				'   <button id="prev_page_button">&lt;</button>' +
				'   ' + Faust.messages["page_abbrev"] +
				'   <input id="pagenum-display" value="1" style="width: 2em; margin-right: 1em" readonly="readonly"/>' +
				'   <span id="pageslider"></span>' +
				'   <button id="next_page_button">&gt;</button>' +
				'   <span><a id="old_version_link" href="" style="margin-left: 2em">' + Faust.messages["document.old_version"] + '</a></span>' +
				'</div>');

			var pageslider = new Y.Slider({
				axis: 'x',
				min: 1,
				max: 1,
				value: 1,
				length: 500
			});

			var pagenumDisplay = container.one('#pagenum-display');
			var pagesliderContainer = container.one('#pageslider');

			pageslider.render(pagesliderContainer);
			pageslider.set('max', model.get('numberOfPages'));

			function updatePagenumDisplay(n) {
				pagenumDisplay.set('value', n);
			}

			pageslider.on('valueChange', function (e) {
				updatePagenumDisplay(e.newVal);
			});

			model.after('pagenumberChange', function (e) {
				updatePagenumDisplay(e.newVal);
				pageslider.set('value', e.newVal);
			});

			pageslider.on('slideEnd', function (e) {

				model.set('pagenumber', e.target.get('value'));
				app.navigate('/' + model.get('pagenumber'));

			});

			new Y.Button({
				srcNode: container.one('#prev_page_button'),
				on: {
					'click': function () {
						app.navigate('/' + (parseInt(model.get('pagenumber')) - 1));
					}
				}
			}).render();

			new Y.Button({
				srcNode: container.one('#next_page_button'),
				on: {
					'click': function () {
						app.navigate('/' + (parseInt(model.get('pagenumber')) + 1));
					}
				}
			}).render();

		}
	});

	Y.DocumentView = Y.Base.create("document-view", Y.View, [], {
		initializer: function () {
			this.get('model').after('change', this.render, this);
			this.get('container').after('change', function () {
			});

			this.on('faust:navigation-done', function () {
				console.log('nav');
			});

		},
		destructor: function () {
			//this.get('diplomaticPanel') && this.get('diplomaticPanel').destroy();
			//this.get('facsimilePanel') && this.get('facsimilePanel').destroy();
			this.get('container').empty();
		},

		panel: function (name, title, attrs) {

			var container = this.get('container');
			var widthAvailable = parseInt(Y.one('#document-app').getComputedStyle('width')) - 40;

			panelContainer = Y.Node.create(
				'<div class="' + name + '-container yui3-panel-loading">' +
					'   <div class="yui3-widget-hd">' + title + '</div>' +
					'   <div class="yui3-widget-bd" style="overflow:hidden">' +
					'   <div class="' + name + 'Content"></div></div>' +
					'   <div class="yui3-widget-ft"></div>' +
					'</div>'
			);

			container.append(panelContainer);


			panelAttrs = Y.merge({
				srcNode: panelContainer,
				width: widthAvailable / 2,
				preventOverlap: true,
				zIndex: 10,
				render: true
			}, attrs);

			var panel = new Y.Panel(panelAttrs);
			panel.plug(Y.Plugin.Drag, {handles: ['.yui3-widget-hd']});
			panel.plug(Y.Plugin.Resize);
			return panel;
		},

		addAjaxLoader: function (element) {
			var tmpHeight = Math.max(100, element.getComputedStyle('height'));
			element.empty();
			element.append('<div class="faust-ajax-loading ajax-placeholder" style="min-height: ' + tmpHeight + '"/>');
		},

		removeAjaxLoader: function (element) {
			element.one('.ajax-placeholder').remove(true);
		},

		updateFacsimileView: function () {
			var facsimileContainer = Y.one('.facsimile-container');
			var facsimileContent = facsimileContainer.one('.yui3-widget-bd');
			var pagenum = this.get('model').get('pagenumber');
			facsimileContent.empty();

			var facsimilePath = this.get('pages')[pagenum - 1].facsimile;

			if (facsimilePath) {
				var facsimileURI = new Y.Faust.URI(facsimilePath);

				facsimileContent.append('<div id="transcript-swf" style="height: 300px"></div>');
				swfobject.switchOffAutoHideShow();
				swfobject.embedSWF(Faust.contextPath + "/static/swf/IIPZoom.swf",
					"transcript-swf",
					"100%", "100%",
					"9.0.0", Faust.contextPath + "/static/swf/expressInstall.swf", {
						server: Faust.FacsimileServer,
						image: facsimileURI.encodedPath() + '.tif',
						navigation: true
						//credit: "Copyright Digitale Faust-Edition"
					}, {
						scale: "exactfit",
						bgcolor: "#000000",
						allowfullscreen: "true",
						allowscriptaccess: "always",
						wmode: "opaque"
					});
			} else {
				facsimileContent.append('<div>(none)</div>');
			}
		},

		updateDiplomaticTranscriptView: function () {
			var pagenum = this.get('model').get('pagenumber');
			var diplomaticContainer = Y.one('.diplomatic-container');

			var diplomaticContent = diplomaticContainer.one('.diplomaticContent');

			this.addAjaxLoader(diplomaticContent);

			var that = this;
			var page = this.get('pages')[pagenum - 1];

			var initTranscriptView = function(transcript) {
					that.removeAjaxLoader(diplomaticContent);
					var diplomaticTranscriptView = new Y.Faust.DiplomaticTranscriptView({
						container: diplomaticContainer.one('.diplomaticContent'),
						visComponent: null,
						transcript: transcript,
						source: source,
						pagenum: pagenum
					});
					diplomaticTranscriptView.render();
			}

			if (page.transcript && page.transcript.source) {
				var source = page.transcript.source;
				page.transcriptionFromRanges(initTranscriptView);
			} else {
				initTranscriptView(null);
			}

		},

		updateStructureView: function () {
			var structureContainer = Y.one('.structure-container');

			var structureView = new Y.Faust.DocumentStructureView({
				container: structureContainer.one('.structureContent'),
				document: this.get('fd')
			});

			structureView.render();


		},

		updateTextView: function () {
			var textContent = Y.one('.textContent');
			this.addAjaxLoader(textContent);
			var that = this;


			function stageNum(name) {
				stageNum.stages = stageNum.stages || {};
				stageNum.stagecount = stageNum.stagecount || 0;
				if (stageNum.stages[name] === undefined)
					stageNum.stages[name] = stageNum.stagecount++;
				return stageNum.stages[name];
			};

			var handleSpecialAnnotations = function (annotation, prefix, partitionNode, lineNode, isFirst, isLast) {

				if (annotation.name.localName == 'stage')
					return [prefix + 'stage-' + stageNum(annotation.data['value'])];

				if (annotation.name.localName == 'l' && isFirst)
					partitionNode.insert('<span class="linenum">' + parseInt(annotation.data['n']) + '</span>', lineNode);

				return [];
			};

			this.get('fd').transcriptionFromRanges(function (t) {
				window.setTimeout(function () {
					that.removeAjaxLoader(textContent);
					//textContent.append(DocumentText.renderText(t));
					var text = Y.Faust.Text.create(t);
					var textDisplay = new Y.Faust.TextDisplayView({
						container: textContent,
						text: text,
						cssPrefix: 'ann-',
						renderCallback: handleSpecialAnnotations});
					textDisplay.render();
				}, 2000);
			});


		},

		render: function () {
			var pagenum = this.get('model').get('pagenumber');
			var container = this.get('container');
			var widthAvailable = parseInt(Y.one('#document-app').getComputedStyle('width')) - 40;


			var facsimileContainer = Y.one('.facsimile-container');
			if (!facsimileContainer) {
				facsimilePanel = this.panel('facsimile', Faust.messages["document.facsimile"], {
					height: 600,
					align: {
						node: '#document-app',
						points: [
							Y.WidgetPositionAlign.TL,
							Y.WidgetPositionAlign.TL
						]
					}
				});
			}

			var diplomaticContainer = Y.one('.diplomatic-container');
			if (!diplomaticContainer) {

				var diplomaticPanel = this.panel('diplomatic', Faust.messages["document.diplomatic_transcript"], {
					align: {
						node: '#document-app',
						points: [
							Y.WidgetPositionAlign.TR,
							Y.WidgetPositionAlign.TR
						]
					}
				});
			}

			var structureContainer = Y.one('.structure-container');
			if (!structureContainer) {

				var diplomaticPanel = this.panel('structure', Faust.messages["document.document_structure"], {
					zIndex: 11,
					align: {
						node: '#document-app',
						points: [
							Y.WidgetPositionAlign.BR,
							Y.WidgetPositionAlign.BR
						]
					}
				});
				this.updateStructureView();

			}

			var textContainer = Y.one('.text-container');
			if (!textContainer) {

				var textPanel = this.panel('text', Faust.messages["document.document_text"], {
					zIndex: 11,
					align: {
						node: '#document-app',
						points: [
							Y.WidgetPositionAlign.BL,
							Y.WidgetPositionAlign.BL
						]
					}
				});


				this.updateTextView();
			}


			this.updateFacsimileView();

			this.updateDiplomaticTranscriptView();


		}

	}, {
		ATTRS: {
			pages: null,
			model: null
		}
	});

	var DocumentApp = {};

	DocumentApp.initializeFn = function(path, documentSource) {
		return	function() {
				  Y.one('#document-app').addClass('faust-ajax-loading');
				  Y.on('faust:document-data-arrives', function(e) {
					  Y.one('#document-app').removeClass('faust-ajax-loading');

					  var navigationModel = new Y.NavigationModel({
						  numberOfPages: e.pages.length
					  });

					  app = new Y.App({
						  container: '#document-app',
						  root: cp +  path.replace('faust://', '/'),
						  //linkSelector: ("#" + this.get("id") + " a"),
						  transitions: false,
						  scrollToTop: false,
						  serverRouting: false,
						  views: {
							  'document-view': {
								  type: "DocumentView",
								  preserve: true
							  }
						  },
						  model: navigationModel
					  });

					  app.route("/", function() {
						  this.navigate("/1");
					  });

					  app.route("/:page", function(req) {
						  var model = this.get('model');
						  var requestPagenum =  parseInt(req.params.page);
						  model.set('pagenumber', requestPagenum);
						  if (model.get('pagenumber') !== requestPagenum)
							  this.navigate("/" + model.get('pagenumber'));
						  else {
							  this.showView("document-view",
											{ pagenum: model.get('pagenumber'),
											  fd: e.fd,
											  pages: e.pages,
											  model: model
											},

											{
												transition: 'slideLeft',
												update: false
											});
							  this.fire('faust:navigation-done');
						  }
					  });

					  app.set('fd', e.fd);

					  var updateOldVersionLink = function() {
						  var old_version_cp = "https://faustedition.uni-wuerzburg.de/dev";
						  var pathname = window.location.pathname;
						  var hash = window.location.hash.replace("/","");
						  var old_version = old_version_cp + pathname.slice(pathname.indexOf('/document')) + hash;
						  Y.one("#old_version_link").setAttribute('href', old_version);
					  };

					   app.after('faust:navigation-done', function() {
					   		  updateOldVersionLink();
					   });

					  var navigationView = new Y.NavigationView({
						  model: navigationModel,
						  container: Y.one('#document-navigation')
					  });



					  navigationView.render();

					  app.render().dispatch();


				  });



				  Faust.Document.load(new Faust.URI(documentSource), function(fd) {

					  var pages = [];
					  var descendants = fd.descendants();
					  for (i=0; i < descendants.length; i++)
						  if (descendants[i].type === 'page' /* && descendants[i].transcript */)
							  pages.push(descendants[i]);

					  Y.fire('faust:document-data-arrives', {
						  fd: fd,
						  pages: pages
					  });
				  });


			  };
	};

	Y.mix(Y.namespace("Faust"), {
		DocumentApp : DocumentApp
	});


}, '0.0', {
	requires: ["app", "node", "event", "slider", "document", "document-yui-view",
		"document-structure-view", "button", "panel", "dd-plugin", "resize-plugin", "util",
		"text-display"]
});