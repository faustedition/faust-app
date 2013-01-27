YUI.add('document-text', function (Y) {		

	DocumentText = {
		
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
			transcript = Y.Faust.Text.create(t);
			var textNode = Y.Node.create('<div></div>');

			stageColors = {
				colors: ["red", "blue", "green", "yellow", "purple", "grey", "magenta", "cyan"],
				map: {},
				uniqueStageCount: 0
			}
			
			function stageColor(stageName) {
				if (stageColors.map[stageName] !== undefined) {

					return stageColors.map[stageName];
				}
				else {
					stageColors.uniqueStageCount++;
					console.log("Number of stages:" + stageColors.uniqueStageCount);
					var index = stageColors.uniqueStageCount % stageColors.colors.length;
					console.log('index: ' + index);
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
						//partitionNode.insert('</sup>', 'after');
						
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
						// partitionNode.insert('<', 'before');
						// partitionNode.insert('>', 'after');
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
        DocumentText : DocumentText,
	});
	
}, '0.0', {
	requires: ['node']
});
