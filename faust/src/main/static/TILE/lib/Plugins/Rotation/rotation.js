// Dashboard Plugin
// author: Grant Dickie

// Makes use of Googles OpenSocial scheme for adding widgets
// Displays global information about the user's current TILE session

var ImageRotation = {

	
	start:function(engine){
	
		var self=this;
		
		var name = "rotation_"; // namespace
		var args = { // defaults
				name: name,
				location: name+'workspace',
				// tools
				selectedColor: 'mosaic',
				selectedColor: 'mosaic',
				selectedSize: 4, 
				selectedTool: 'pencil',
				line:  null,
				path:  null,
				color: null,
				size: null,
				// img
				image: $('#srcImageForCanvas'),
				imageWidth: $('#srcImageForCanvas').width(),
				imageHeight: $('#srcImageForCanvas').height(),
				imageCenter: 0,
				wPixelsIn: 0,
				hPixelsIn: 0,
				angle: 0
		};	
		
		// holder for other mode data
		self.modeData={};
		// copy of the core data
		self.data=engine.getJSON();
		
		self.IR_RightSideToolbar=
			"<ul class=\"menuitem pluginTitle\">Tools &raquo;</ul>"+
				"<ul class=\"menuitem\" id=\""+name+"tools\">"+
				"</ul>";

			self.IR_LeftSideContent=
			'<div id="'+name+'log" class="az tool imagerotation">'+
				'<div id="'+name+'area" class="az inner imagerotation">'+
					'<div class="'+name+'toolbar">'+
						'<div class="toolbar">Image Rotation'+
							'<div class="menuitem">'+
								'<span class="button" id=\"#cancelButton\">Cancel</span>'+
							'</div>'+
						'</div>'+
					'</div>'+
					'<div id="content" class="az">'+
						'<div id="'+name+'selection">'+
							'<div id="'+name+'slider" style="width:360px;"></div>'+ // rotation slider
							'<br />'+
							'<div id="'+name+'amountHolder">'+
								'<input type="text" id="'+name+'amount" value="" size="3" />&deg; <button id="'+name+'button">Rotate</button>'+
							'</div>'+ // rotation box
						'</div>'+
						
						'<br />'+
						'<br />'+
						'<div id="'+args.name+'picker"></div>'+ // color squares				
					'</div>'+
				'</div>'+
			'</div>';
				
			self.IR_RightSideHolder = "<div id=\""+name+"workspace\" class=\"workspace\"></div>";		
		

		
		// create mode with a callback
		var onActive=function(e){
				self.setUp(args);
				self.addTools(args);	
				self.addColors(args);
				self._raphaelCanvas(args);
				self.addRotationTools(args);
		};
		
		// insert HTML into TILE template
		engine.insertMode('Image Rotation', onActive);
		engine.insertModeHTML(self.IR_RightSideHolder,'contentarea','Image Rotation');  // right side <div> container
		engine.insertModeButtons(self.IR_RightSideToolbar,'contentarea','Image Rotation'); // right side toolbar
		engine.insertModeHTML(self.IR_LeftSideContent,'main','Image Rotation'); // left side content 
	
		
	},
	
	// stupid chrome has a repaint bug
	_forcePaint: function(){
		
		var self=this;
		
		window.setTimeout(function(){
			var rect = R.rect(-99, -99, R.width + 99, R.height + 99).attr({stroke: "none"});
			setTimeout(function() {rect.remove();});
		},1);
		
	},
            
	_draw: function(path, color, size) {
		var self=this;
		
		var result = R.path(path);
		result.attr({ stroke: color, 'stroke-width': size, 'stroke-linecap': 'round' });
		
		_forcePaint();
		
		return result;
	},
            
	_clear: function() {
		var self=this;
		
		return R.clear();
	},

	_raphaelCanvas: function(args) {
		var self=this;
		
		if(__v) console.log('RAPHAEL width' + args.imageWidth);
		R = Raphael(args.location, args.imageWidth, args.imageHeight);
		set = R.set(); // create a Raphael set
		
		//s.push(R.image(args.image[0], args.wPixelsIn, args.hPixelsIn, args.imageWidth, args.imageHeight)); 
		
	},

	capitaliseFirstLetter: function(string) {
    	return string.charAt(0).toUpperCase() + string.slice(1);
	},

	uncapitaliseFirstLetter: function(string) {
    	return string.charAt(0).toLowerCase() + string.slice(1);
	},

	   
	setUp: function(args) {
		var self=this;
		 
		$('#'+args.location).mousedown(function(e) {
			
			var pos = position(e);
			if(__v) console.log('MOUSE DOWN: '+ position); 
			
			var path = [['M', pos.x, pos.y]];
			var color = (args.selectedTool == 'eraser' ? '#000' : (args.selectedColor == 'mosaic') ? Raphael.getColor() : args.selectedColor);
			var size = args.selectedSize;
			
			line = _draw(path, color, size);
			$('#'+args.location).bind('mousemove', mouseMove);
		});
		
		$('#'+args.location).mouseup(function() {
			$('#'+args.location).unbind('mousemove', mouseMove);
			if (line) {
				line = null;
			}
		});
		
		
		
		
		
					
		$('<td class="divider"></td>').appendTo('.picker tr');
		
		
		
		for (var i = 2; i <= 12; i+=2) {
			var percent = Math.round(((i - 1) / 11) * 100);
			var td = '<td align="center"><div class="size' + (args.selectedSize == i ? ' selected' : '') + '" title="' + i + '"><div style="background-color: #000; width: ' + percent + '%; height: ' + percent + '%"></div></div></td>';
			
			$(td).appendTo('.picker tr').click(function() {
				$('#'+args.container).removeClass("size-" + args.selectedSize);
				args.selectedSize = $(this).find('.size').attr('title');
				$('#'+args.container).addClass("size-" + args.selectedSize);
			});
		}
		
		
	},
	
	
	
	
	addTools: function(args) {
		var self=this;
		
		$.each([
			'pencil',
			'circle',
			'square',
			'line',
			'spacer',
			'prev',
			'next',
			'list'
		], function(key, val){
			
			switch(val){
				case 'spacer':
					$('<li class="'+args.name+'pluginIcon spacer"></li>').appendTo('#'+args.name+'tools');
					break;
				case 'prev':
					$('<li><a href="#" id="pgPrev" class="button" title="Go One Image Back">Prev</a></li>').appendTo('#'+args.name+'tools');
					break;
				case 'next':
					$('<li><a href="#" id="pgNext" class="button" title="Go One Image Forward">Next</a></li>').appendTo('#'+args.name+'tools');
					break;                        
				case 'list':
					$('<li><a href="#" class="button" title="See a List of All Images"><span id="listView" class="listView">List View</span></a></li>').appendTo('#'+args.name+'tools');
					break;
				default:
					var list = $('<li></li>')
					list.appendTo('#'+args.name+'tools');
					$('<a href="#" id="'+val+'Tool" title="'+self.capitaliseFirstLetter(val)+'" class="'+args.name+'pluginIcon '+ (val == 'pencil' ? ' active' : '') + '">&nbsp;</a></li>').appendTo(list)
					.click(function(){
						
						// first add and remove class from raphael div
						$('#'+args.location).removeClass(args.selectedTool);
						args.selectedTool = val;
						$('#'+args.location).addClass(args.selectedTool);
						
						// add selected to tool
    		            $('#'+args.name+"tools").find('li a.active').removeClass('active').addClass('inactive');
            		    $(this).addClass('active');
			
						
					});
					break;
			};					
		
		}); // end each	
	},
	
	
	addColors: function(args) {
		
		var self=this;
			
		$.each([
			'#000', // black (eraser)
			'#fff', // white
			'#f00', // red
			'#0f0', // lime
			'#00f', // blue
			'#ff0', // yellow
			'#f0f', // fuschia
			'#0ff', // cyan
			'#800000', // maroon
			'#008000', // green
			'#000080', // navy
			'#808000', // olive
			'#800080', // purple
			'#008080',  // teal
			'mosaic'
		], function(key, val) {
			if(this == 'mosaic'){
				$('<div class="colorPicker active" id="mosaicColorPicker" title="' + val + '">&nbsp;</div>')
				.appendTo('div#'+args.name+'picker')
				.click(function() {
					args.selectedColor = $(this).find('.color').attr('title');
				});
			}else{
				$('<div class="colorPicker inactive" style="background-color: ' + val + '" title="' + val + '">&nbsp;</div>')
				.appendTo('div#'+args.name+'picker')
				.click(function() {
					args.selectedColor = $(this).find('.color').attr('title');
				});
			}
		});
		
		$('.colorPicker').click(function() {
			$('div#'+args.name+'picker').find('div.colorPicker').removeClass('active').addClass('inactive');
			$(this).addClass('active');
		});		
		
		
	},

	addSizes: function(args) {
		
		var self=this;
			
		$.each([
			'#000', // black (eraser)
			'#fff', // white
			'#f00', // red
			'#0f0', // lime
			'#00f', // blue
			'#ff0', // yellow
			'#f0f', // fuschia
			'#0ff', // cyan
			'#800000', // maroon
			'#008000', // green
			'#000080', // navy
			'#808000', // olive
			'#800080', // purple
			'#008080',  // teal
			'mosaic'
		], function(key, val) {
			if(this == 'mosaic'){
				$('<div class="colorPicker active" id="mosaicColorPicker" title="' + val + '">&nbsp;</div>')
				.appendTo('div#'+args.name+'picker')
				.click(function() {
					args.selectedColor = $(this).find('.color').attr('title');
				});
			}else{
				$('<div class="colorPicker inactive" style="background-color: ' + val + '" title="' + val + '">&nbsp;</div>')
				.appendTo('div#'+args.name+'picker')
				.click(function() {
					args.selectedColor = $(this).find('.color').attr('title');
				});
			}
		});
		
		$('.colorPicker').click(function() {
			$('div#'+args.name+'picker').find('div.colorPicker').removeClass('active').addClass('inactive');
			$(this).addClass('active');
		});		
		
		
	},
	
	addRotationTools: function(args) {
		$("#"+args.name+"slider" ).slider({
			value:0,
			min: 0,
			max: 360,
			step: 1,
			slide: function( event, ui ) {
				set.animate({rotation: ui.value+","+args.imageWidth+","+args.imageHeight}, 1000, "<>");
				$("#"+args.name+"amount").val( ui.value );
			}
		});
		
		
		$("#"+args.name+"rotateMe").click(function() {
			var val = $("#"+args.name+"amount").val();
			set.animate({rotation: val+","+args.imageWidth+","+args.imageHeight}, 1000, "<>");
		});		
		
	},
	
	
	
	toolBindings: function() {
		var self=this;

		var position = function(e) {
			return {
				x: e.pageX - offset.left,
				y: e.pageY - offset.top
			};
		};
		
		var mouseMove = function(e) {
			var pos = position(e),
				x = path[0][1],
				y = path[0][2],
				dx = (pos.x - x),
				dy = (pos.y - y);
			
			//console.log(x);
			//console.log(dx);
			
			
			switch(selectedTool){
				case 'pencil':
					path.push(['L', pos.x, pos.y]);
					break;
				case 'rectangle':
					path[1] = ['L', x + dx, y     ];
					path[2] = ['L', x + dx, y + dy];
					path[3] = ['L', x     , y + dy];
					path[4] = ['L', x     , y     ];
					path[5] = ['L', x,      y     ];
					break;
				case 'line':
					path[1] = ['L', pos.x, pos.y];
					break;                        
				case 'circle':
					path[1] = ['A', (dx / 2), (dy / 2), 0, 1, 0, pos.x, pos.y];
					path[2] = ['A', (dx / 2), (dy / 2), 0, 0, 0, x, y];
					break;
				case 'eraser':
					path.push(['L',pos.x, pos.y]);
					break;
			};
			
			
			line.attr({ path: path });
			
			
			/**********************************************/
			s.push(line); // add to set
			/**********************************************/				
		};
            

		
		$('.color').click(function() {
			$(this).closest('table').find('.color').removeClass('selected');
			$(this).addClass('selected');
		});
		
		
		$('.size').click(function() {
			$(this).closest('table').find('.size').removeClass('selected');
			$(this).addClass('selected');
		});
		
		
		$('.tool').click(function() {
			$(this).closest('table').find('.tool').removeClass('selected');
			$(this).addClass('selected');
		});
	}
		   
		   
		   
	
	
};