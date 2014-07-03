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

YUI.add('document-text', function (Y) {

	var DocumentText = {
		
		forAnnotationDo: function (annotationName, partition, f) {
			var annotations = transcript.find(partition.start, partition.end, [annotationName]);
			if (annotations.length > 0)  {
				f(annotations[0]);
			}
		},

		forAnnotationStartDo: function (annotationName, partition, f) {
			var annotations = transcript.find(partition.start, partition.end, [annotationName]);
			if (annotations.length > 0)  {
				if (annotations[0].targets[0].range.start === partition.start) {
					f(annotations[0]);
				}
			}
		},


		renderText : function(t) {
			var transcript = Y.Faust.Text.create(t);
			var textNode = Y.Node.create('<div></div>');

			var stageColors = {
				colors: ["red", "blue", "green", "yellow", "purple", "grey", "magenta", "cyan"],
				map: {},
				uniqueStageCount: 0
			};
			
			function stageColor(stageName) {
				if (stageColors.map[stageName] !== undefined) {

					return stageColors.map[stageName];
				}
				else {
					stageColors.uniqueStageCount++;
					var index = stageColors.uniqueStageCount % stageColors.colors.length;
					stageColors.map[stageName] = stageColors.colors[index];
					return stageColors.map[stageName];
				}
			}

			Y.each(transcript.partition(), function(p) {
							
				var partitionNode = Y.Node.create('<span></span>');
				textNode.append(partitionNode);
				Y.Array.each(p.of(t.textContent).split("\n"), function(line, n) {
					if (n > 0) {
						partitionNode.append("<br>");
					}
					partitionNode.append(Y.config.doc.createTextNode(line));
					//partitionNode.append(Y.config.doc.createTextNode(' --- '));
					

					var forAnnotationDo = DocumentText.forAnnotationDo;
					var forAnnotationStartDo = DocumentText.forAnnotationStartDo;

					forAnnotationDo('del', p, function() {
						partitionNode.setStyle('text-decoration', 'line-through');
					});

					forAnnotationDo('add', p, function() {
						partitionNode.wrap('<sup/>');						
					});


					forAnnotationDo('speaker', p, function() {
						partitionNode.setStyle('font-weight', 'bold');
					});

					forAnnotationDo('expan', p, function() {
						partitionNode.insert('[', 'before');
						partitionNode.insert(']', 'after');
					});

					forAnnotationDo('ex', p, function() {
						partitionNode.insert('[', 'before');
						partitionNode.insert(']', 'after');
					});

					forAnnotationDo('stage', p, function(a) {
						var stageName = a.data['value'];
						partitionNode.setStyle('color', stageColor(stageName));
						partitionNode.setAttribute('title', stageName);
					});

					forAnnotationStartDo('l', p, function(a) {
						var lineNum = a.data['n'];
						partitionNode.insert('<small style="color: grey;"> ' + parseInt(lineNum) + ' </small>', 'before');

					});
				
				});
			});
			return textNode;
		}
	};

	Y.mix(Y.namespace("Faust"), {
        DocumentText : DocumentText
	});
	
}, '0.0', {
	requires: ['node']
});
