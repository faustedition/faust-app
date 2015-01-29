/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

YUI.add('document-app', function (Y) {

	var VIEW_MODES = ['facsimile', 'diplomatic', 'text'];

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
				value: VIEW_MODES[0],
				validator: function (value) {
					return VIEW_MODES.indexOf(value) >= 0;
				}
			}
		}
	});

	Y.NavigationView = Y.Base.create("navigation-view", Y.View, [], {


		render: function () {

			var model = this.get('model');

			var container = Y.one(this.get('container'));


			container.append('<div style="margin:0em 0em; width:600">' +
				'   <button  class="button-pure button-opaque" id="prev_page_button">&lt;</button>' +
				'   ' + Faust.messages["page_abbrev"] +
				'   <input id="pagenum-display" value="1" style="width: 2em; margin-right: 1em" readonly="readonly"/>' +
				'   <span id="pageslider"></span>' +
				'   <button class="button-pure button-opaque" id="next_page_button">&gt;</button>' +
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

	Y.ModeView = Y.Base.create("mode-view", Y.View, [], {

		updateButton: function(viewMode) {
			this.button.empty();
			var displayText = {
				facsimile: '<i class="icon-picture"></i> facsimile',
				diplomatic: '<i class="icon-font"></i> transcript',
				text: '<i class="icon-align-left"></i> text'
			};
			this.button.append('<span>' + displayText[viewMode] + '</span>');
		},

		render: function () {
			var model = this.get('model');
			var container = Y.one(this.get('container'));
			this.button = Y.Node.create('<button class="pure-button button-opaque" style="min-width: 10em"></button>');
			this.updateButton(model.get('viewMode'));
			container.append(this.button);

			var that = this;
			model.after('viewModeChange', function (e) {
				that.updateButton(e.newVal);
			});

			this.button.on('click', function() {
				// cycle through mode domain
				var currentIndex = VIEW_MODES.indexOf(model.get('viewMode'));
				var nextIndex = (currentIndex + 1) % VIEW_MODES.length;
				model.set('viewMode', VIEW_MODES[nextIndex]);
			})
		}
	});


	Y.DocumentView = Y.Base.create("document-view", Y.View, [], {
		initializer: function () {
			var that = this;
			this.get('model').after('pagenumberChange', this.render, this);

			this.get('model').after('viewModeChange', function (e) {
				that.updateViewMode(e.newVal);
			});


			// prevent selection when dragging the mouse
			Y.one('body').on('mousedown', function(e) {
				e.preventDefault();
			});
			Y.one('body').on('mousemove', function(e) {
				e.preventDefault();
			});

		},
		destructor: function () {
			//this.get('diplomaticPanel') && this.get('diplomaticPanel').destroy();
			//this.get('facsimilePanel') && this.get('facsimilePanel').destroy();
			this.get('container').empty();
		},

		addAjaxLoader: function (element) {
			var tmpHeight = Math.max(100, element.getComputedStyle('height'));
			element.empty();
			element.append('<div class="faust-ajax-loading ajax-placeholder" style="min-height: ' + tmpHeight + '"/>');
		},

		removeAjaxLoader: function (element) {
			element.one('.ajax-placeholder').remove(true);
		},

		updateViewMode: function (viewMode) {
			Y.all('#document-app .mode-switchable').each(function(node){
				if (node.hasClass(viewMode + '-container')) {
					node.transition('fadeIn');
					node.setStyle('zIndex', '10');
				} else {
					node.transition('fadeOut');
					node.setStyle('zIndex', '9');
				}
			});
		},

		fitOverlay: function () {
			Y.Faust.showingTranscriptOverlay = true;
			Y.each(Y.all('#svgpane .element-line'), function (transcriptLine) {
				var lineNum = Y.SvgUtils.decodeClassValue(transcriptLine.getAttribute('class'), 'lineNumber');
				var imageLinkLine = Y.one('#svgpane .imageannotationLine.linkedto-lineNumber' + lineNum);

				if (imageLinkLine) {
					transcriptLine.setStyle('opacity', '0');
					transcriptLine.setAttribute('transform', 'scale(50,50)');
					Y.SvgUtils.fitTo(transcriptLine.getDOMNode(), imageLinkLine.getDOMNode());
					transcriptLine.transition('fadeIn');

					//var matrix = transcriptLine.getDOMNode().ownerSVGElement.createSVGMatrix();
					//var bbox1 = Y.SvgUtils.boundingBox(imageLinkLine.getDOMNode(), matrix);
					//var bbox2 = Y.SvgUtils.boundingBox(transcriptLine.getDOMNode(), matrix);
					//console.log(bbox1);
					//console.log(bbox2);
					//transcriptLine.transition('fadeOut');
				} else {
					// if the transcript line is not linked to image, remove it
					transcriptLine.remove(true);
				}

			})
		},

		updateFacsimileView: function () {
			var facsimileContainer = Y.one('.facsimile-container');
			var facsimileContent = facsimileContainer.one('.facsimile-content');
			var pagenum = this.get('model').get('pagenumber');
			facsimileContent.empty();

			var facsimilePath = this.get('pages')[pagenum - 1].facsimile;

			if (facsimilePath) {
				var facsimileURI = new Y.Faust.URI(facsimilePath);

				facsimileContent.append('<div id="facsimile-view" style="height: 600px"></div>');
				var viewWidth = parseInt(Y.one('#facsimile-view').getComputedStyle('width'));
				var viewHeight = parseInt(Y.one('#facsimile-view').getComputedStyle('height'));
				var	facsimileViewer = new Y.Faust.FacsimileViewer({
					srcNode: "#facsimile-view",
					src: facsimileURI.components[0].substr(7),
					view: { x: 0, y: 0, width: viewWidth, height: viewHeight }
				});
				facsimileViewer.render();


				// show navigation buttons
				facsimileViewer.plug(Y.Faust.FacsimileNavigationButtons);
				// enable mouse navigation
				facsimileViewer.plug(Y.Faust.FacsimileNavigationKeyboard);
				// enable mouse navigation
				facsimileViewer.plug(Y.Faust.FacsimileNavigationMouse);

				//facsimileViewer.model.fitToView(viewWidth, viewHeight);

				var imageLinkPath = Faust.imageLinkBase + '/' + pagenum;
				// display svg from imageLinkPath
				facsimileViewer.plug(Y.Faust.SvgPane);
				facsimileViewer.svg.loadSvg(imageLinkPath, 'text-image-overlay');
				var that = this;

				Y.Faust.aggregateEvents ('faust:transcript-layout-done', 'faust:facsimile-svg-pane-loaded',
				'faust:transcript-overlay-start');
				Y.on('faust:transcript-overlay-start', function(e){
					var transcriptCopy = Y.one('.diplomatic-content svg.diplomatic').cloneNode(true);
					var textImageSvg = Y.one ('.svgpane-text-image-overlay svg');
					if (textImageSvg) {
						textImageSvg.show();
						transcriptCopy.setAttribute('width', textImageSvg.getAttribute('width'));
						transcriptCopy.setAttribute('height', textImageSvg.getAttribute('height'));
						facsimileViewer.svg.addSvg(transcriptCopy.getDOMNode(), 'transcript-overlay');
						//transcriptCopy.transition({	duration: 3, opacity: 1});
						that.fitOverlay();
						textImageSvg.hide();
						Y.fire('faust:transcript-overlay-done');
					}
				});

			} else {
				facsimileContent.append('<div>(none)</div>');
			}
		},



		updateDiplomaticTranscriptView: function () {
			var pagenum = this.get('model').get('pagenumber');
			var diplomaticContainer = Y.one('.diplomatic-container');

			var diplomaticContent = diplomaticContainer.one('.diplomatic-content');

			diplomaticContent.setStyle('overflow', 'auto');

			this.addAjaxLoader(diplomaticContent);

			var that = this;
			var page = this.get('pages')[pagenum - 1];

			var initTranscriptView = function (transcript) {

				//fade in after loading
				that.removeAjaxLoader(diplomaticContent);
				diplomaticContent.transition({    duration: 0, opacity: 0.2});
				Y.on('faust:transcript-layout-done', function (e) {
					diplomaticContent.transition({    duration: 3, opacity: 1});
				});

				//try to load a cached SVG layout
				var sourceUri = page.transcript.source.components[0];
				var cachedTranscript = sourceUri.substring('faust://xml/transcript'.length, sourceUri.length - '.xml'.length) + '.svg';
				Y.io(cp + '/transcriptcache/' + cachedTranscript, {
						on: {
							success: function (id, o, args) {
								Y.one('.diplomatic-content').append(o.responseText);
								Y.fire('faust:transcript-layout-done', {});
							},

							//if there is no cached version, fall back to layout client-side
							failure: function (id, o, args) {
								//load the layout module dynamically
								Y.use('transcript-view', function() {
									var diplomaticTranscriptView = new Y.FaustTranscript.DiplomaticTranscriptView({
										container: diplomaticContainer.one('.diplomatic-content'),
										transcript: transcript,
										source: page.transcript.source,
										pagenum: pagenum
									});
									diplomaticTranscriptView.render();
								})
							}
						}
					}
				);
				//Y.one('.diplomatic-content').append('<h3>Placeholder</h3>');
			};

			if (page.transcript && page.transcript.source) {
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
			var textContent = Y.one('.text-content');
			this.addAjaxLoader(textContent);
			var that = this;


			function stageNum(name) {
				stageNum.stages = stageNum.stages || {};
				stageNum.stagecount = stageNum.stagecount || 0;
				if (stageNum.stages[name] === undefined)
					stageNum.stages[name] = stageNum.stagecount++;
				return stageNum.stages[name];
			}
			var handleSpecialAnnotations = function (annotation, prefix, partitionNode, lineNode, isFirst, isLast) {

				if (annotation.name.localName == 'stage')
					return [prefix + 'stage-' + stageNum(annotation.data['value'])];

				if (annotation.name.localName == 'l' && isFirst) {
					var parsedLineNumber = parseInt(annotation.data['n']);
					var displayedLineNumber = isNaN(parsedLineNumber) ? '---' : parsedLineNumber;
					partitionNode.insert('<span class="linenum">' + displayedLineNumber + '</span>', lineNode);
				}

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
			var widthAvailable = parseInt(Y.one('#document-app').getComputedStyle('width')) - 40;
			this.updateFacsimileView();
			this.updateDiplomaticTranscriptView();
			this.updateTextView();
			this.updateViewMode(this.get('model').get('viewMode'));

		}

	}, {
		ATTRS: {
			pages: null,
			model: null
		}
	});

	var DocumentApp = {};

	var app;

	DocumentApp.initializeFn = function(path, documentSource) {
		return	function() {
				  Y.one('#document-app').addClass('faust-ajax-loading');
				  Y.on('faust:document-data-arrives', function(e) {
					  Y.one('#document-app').removeClass('faust-ajax-loading');

					  //Y.one('#document-app').setStyle('height',Y.one('#main').get('scrollHeight'));

					  var navigationModel = new Y.NavigationModel({
						  numberOfPages: e.pages.length
					  });

					  app = new Y.App({
						  container: '#document-app',
						  root: cp +  path.replace('faust://', '/'),
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

					  app.route("/:page", function(req, res, next) {
						  var model = this.get('model');
						  var parsedPagenum = parseInt(req.params.page);
						  var requestPagenum =  typeof(parsedPagenum) === "number" ? parsedPagenum : 1;
						  model.set('pagenumber', requestPagenum);
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
					  });


					  app.set('fd', e.fd);



					  var navigationView = new Y.NavigationView({
						  model: navigationModel,
						  container: Y.one('#document-navigation')
					  });
					  navigationView.render();


					  var modeView = new Y.ModeView({
						  model: navigationModel,
						  container: Y.one('#document-mode')
					  });
					  modeView.render();

					  app.render().dispatch();
				  });



			Y.Faust.Document.load(new Faust.URI(documentSource), function(fd) {

					  var pages = [];
					  var descendants = fd.descendants();
					  for (var i=0; i < descendants.length; i++)
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
	requires: ["app", "node", "event", "slider", "document", "text-annotation", "transcript-interaction",
		"document-structure-view", "button", "dd-plugin", "resize-plugin", "util",
		"text-display", "materialunit", "facsimile", "facsimile-svgpane", "facsimile-interaction",
		"facsimile-navigation-buttons", "facsimile-navigation-mouse", "facsimile-navigation-keyboard"]
});