/*
 * ext-imageannotation-linking.js
 *
 * Licensed under the Apache License, Version 2
 *
 * Copyright(c) 2012 Moritz Wissenbach
 *
 */

/* 
 * ext-imageannotation extends svg-edit to act as an annotation
 * tool for images.
 *
 * This file contains model and UI for linking any SVG element
 * with a line of text.
 */


/**
 * This global variable holds an instance of Y.LineList and should be the 
 * the interface for other plugins
 **/

var imageannotationLines;

YUI().use('model', 'model-list', 'view', 'node', 'event', 'io', 'json', function(Y) {

	// MODEL

	Y.LineModel = Y.Base.create ('lineModel', Y.Model, [], {}, {
		ATTRS:  {
			
			id: {value: null},

			info:{value: null},

			linkedWith: {value: null},
			
			text: {value: ' --- line ---'}
		}
	});
	
    Y.LineList = function(config) {

        Y.LineList.superclass.constructor.apply(this, arguments);

		this._linkedWith = {};

		this.linkedWith = function(shapeId) {
			return this._linkedWith[shapeId]
		};

		this.on ('add', function (e) {
			this._linkedWith [e.model.get('linkedWith')] = e.model;
		});
		
		this.on ('*:change', function (e) {
			if (e.changed.linkedWith) {
				delete this._linkedWith[e.changed.linkedWith.prevVal];
				this._linkedWith[e.changed.linkedWith.newVal] 
					= e.target;
			}			
		});
	}
	
	Y.extend (Y.LineList, Y.ModelList, {
		
		name: 'lineList',
		
		model: Y.LineModel,

	});

	// VIEW


	Y.LinesView = Y.Base.create('linesView', Y.View, [], {

		events: {
			'#toggleLink': {click: 'toggleLink'},
			'#linesView' : {change: 'selectLine'}
		},
		
		selectLine: function (e){
			// save the selection for redrawing
			this.lastSelected = this.container.one('#linesView').get('value'); 
			this.getSelectedLineModel()

			var linkedShape = this.getSelectedLineLinkedShape();
			if (linkedShape !== null) {
				svgCanvas.clearSelection();
				svgCanvas.addToSelection([linkedShape]);
			}
			this.adjustUI();
		},
		lastSelected : '',

		getSelectedLineLinkedShape: function() {
			
			var line = this.getSelectedLineModel();
			if (line !== null) {
				var id = line.get('linkedWith');
				if (id) {
					var linkedShape = svgCanvas.getElem(id);
					if (linkedShape) 
						return linkedShape;
					// else 
					// 	// the shape doesn't exist (anymore), remove the link from the model
					// 	line.set('linkedWith', null);
				}
			}
			return null;
		},
		
		adjustUI: function() {
			if (this.getSelectedLineLinkedShape())
				var buttonCaption = 'Unlink';
			else
				var buttonCaption = 'Link';
			
			this.container.one('#toggleLink').set('text', buttonCaption);
		},

		getSelectedLineModel : function() {
			try {
				var id = this.container.one('select').get('value');
			} catch (e) {
				return null;
			}
			var line = this.modelList.getById(id);
			return line;
		},

		toggleLink: function () {
			var line = this.getSelectedLineModel();
			if (line === null)
				return;

			var newLink = null;
			if (!line.get('linkedWith')) {
				var selection = svgCanvas.getSelectedElems();
				if (selection && selection.length > 0 && selection[0] && selection[0].id) {	
					newLink = selection[0].id;
				}

			}
			line.set('linkedWith', newLink);
		},

		initializer: function (config) {


			if (config && config.modelList) {
				this.modelList = config.modelList;
				this.modelList.after(['add', 'remove', 'reset', '*:change'], this.render, this);

				// change model on loading a file
				var that = this;
				

				svgEditor.addExtension("imageannotation-linking", function() {
					return {
						elementChanged: function(opts) {
							if (opts.elems && opts.elems.length > 0 
								&& opts.elems[0].getAttribute('id') == 'svgcontent')
								$('#svgcontent').find('rect').each(function(index, elem){
									var link = elem.getAttributeNS('http://www.w3.org/1999/xlink', 'href');
									if (link.charAt(0) === "#") {
										var id = link.substr(1);
										if (that.modelList.getById(id))
											that.modelList.getById(id).set('linkedWith', elem.getAttribute('id'));
									}
								});

						}
					}
				});
			}
		},

		// (YUI/jQuery can't process SVG elements)
		// pass jQuery elements
		modifyClasses: function (elements, remove, add) {
			$(elements).each(function(i, e) {

				var result = '';
				var classes = $(e).attr('class');
				//var classes = e.attributes['class'];
				if (classes !== null) {
					var split = classes.split(' ');
					for (i=0; i < split.length; i++)
						if (! (split[i] === remove || split[i] === add))
							result += ' ' + split[i];
					result += ' ' + add;
				$(e).attr('class', result);
				}
			});
		},

		render: function () {
			var container = this.container;
			container.empty();
			container.append('<button id="toggleLink">Link</button><br/>');
			
			var size = Math.max(this.modelList.size(), 10);
			var list = Y.Node.create('<select id="linesView" size="' + parseInt(size) + '"></select>');
			container.append(list);

			this.modifyClasses('.imageannotationLinked', 'imageannotationLinked', '');
			
			var that = this;
			this.modelList.each(function(lineModel) {
				var value = lineModel.get('id');
				var linkedWith = lineModel.get('linkedWith');
				if (linkedWith) {
					var linkedWithShape = svgCanvas.getElem(linkedWith);
					if (!linkedWithShape) {
						lineModel.set('linkedWith', null);
						return;						
					} else {
						that.modifyClasses(linkedWithShape, '', 'imageannotationLinked');
						linkedWithShape.setAttributeNS('http://www.w3.org/1999/xlink', 'href', value);
					}

				}
				var cssClass = linkedWith ? 'class="linked"' : '';
				var prefix = linkedWith ? '\u2713 ' : '. ';
				list.append('<option value="' + value + '" ' + cssClass + ' >' + prefix + lineModel.get('text') + '</option>');
			});
			this.container.one('select').set('value', this.lastSelected);
			this.adjustUI();
		}
	});

	imageannotationLines = new Y.LineList();

	var view = new Y.LinesView({
		container: Y.one('#textpanel'),
		modelList: imageannotationLines
	});
	

	

});
