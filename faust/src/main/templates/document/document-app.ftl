<#assign archiveId = document.getMetadataValue("archive")>
<#assign callnumber = document.toString()>
<#assign waId = document.getMetadataValue('wa-id')!"">
<#assign title>${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
<#assign imageLinkBase>${cp}/document/imagelink/${document.source?replace('faust://xml/document/', '')}</#assign>
<#assign header>
	<link rel="stylesheet" type="text/css" href="${cp}/static/js/imageviewer/css/iip.css" />
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
	<script type="text/javascript" src="${cp}/static/js/raphael-min.js"></script>
</#assign>




	<@faust.page title=title header=header layout="wide">
	<div id="document-navigation" style="height: 50px;">

	</div>
	<div id="document-app" class="yui-u-1" style="min-height: 600px;"></div>
	<script type="text/javascript">

YUI().use("app", "node", "event", "slider", "document", "document-yui-view",
		  "document-structure-view", "button","panel", "dd-plugin", "resize-plugin", "util",
		  function(Y) {
			  
			  Y.NavigationModel = Y.Base.create('navigationModel', Y.Model, [], {
				  
			  }, {
				  ATTRS: {
					  
					  numberOfPages: {
						  value: 1,
						  validator: function(value) {
							  return Y.Lang.isNumber(value);
						  }
					  },

					  pagenumber: {
						  value: 1,
						  validator: function(value) {
							  return Y.Lang.isNumber(value) && value > 0 && value <= this.get('numberOfPages');
						  }
					  },

					  viewMode: {
						  value: 'transcript',
						  validator: function(value) {
							  return Y.Lang.isNumber(value) && value in {'transcript':1, 'facsimile':1}
						  }
					  }
				  }
			  });

			  Y.NavigationView = Y.Base.create("navigation-view", Y.View, [], {
				  render: function() {
					  
					  var model = this.get('model');

					  var container = Y.one(this.get('container'));

					  var old_version_cp = "https://faustedition.uni-wuerzburg.de/dev";
					  var pathname = window.location.pathname;
					  var old_version = old_version_cp + pathname.slice(pathname.indexOf('/document'))

					  container.append('<div style="margin:3em 5em; width:600">' +
									   '   <button id="prev_page_button">&lt;</button>' +
									   '   ${message("page_abbrev")}' +
									   '   <input id="pagenum-display" value="1" style="width: 2em; margin-right: 1em" readonly="readonly"></input>' +
									   '   <span id="pageslider"></span>' +
									   '   <button id="next_page_button">&gt;</button>' +
									   '   <a href="' + old_version + '" style="margin-left: 100px">${message("document.old_version")}</a>' +
									   '</div>');
					  
					  var pageslider = new Y.Slider({
						  axis        : 'x',
						  min         : 1,
						  max         : 1,
						  value       : 1,
						  length      : 500
					  });

					  var pagenumDisplay = container.one('#pagenum-display');
					  var pagesliderContainer = container.one('#pageslider');

					  pageslider.render(pagesliderContainer);
					  pageslider.set('max', model.get('numberOfPages'));

					  function updatePagenumDisplay(n) {
						  pagenumDisplay.set('value', n);
					  }
					  
					  pageslider.on('valueChange', function(e) {
						  updatePagenumDisplay(e.newVal);
					  });
					  
					  model.after('pagenumberChange', function(e) {
						  updatePagenumDisplay(e.newVal);
						  pageslider.set('value', e.newVal);
					  });

					  pageslider.on('slideEnd', function(e){
						  
						  model.set('pagenumber', e.target.get('value'));
						  app.navigate('/' + model.get('pagenumber'));

					  });

					  new Y.Button({
						  srcNode: container.one('#prev_page_button'),
						  on: {
							  'click': function() {
								  app.navigate('/' + (parseInt(model.get('pagenumber')) - 1));
							  }
						  }
					  }).render();

					  new Y.Button({
						  srcNode: container.one('#next_page_button'),
						  on: {
							  'click': function() {
								  app.navigate('/' + (parseInt(model.get('pagenumber')) + 1));
							  }
						  }
					  }).render();

				  }
			  });

			  Y.DocumentView = Y.Base.create("document-view", Y.View, [], {
				  initializer: function() {
					  this.get('model').after('change', this.render, this);
					  this.get('container').after('change', function() {
					  });
				  },
				  destructor: function() {
					  //this.get('diplomaticPanel') && this.get('diplomaticPanel').destroy();
					  //this.get('facsimilePanel') && this.get('facsimilePanel').destroy();
					  this.get('container').empty();
				  },

				  panel: function(name, title, attrs) {

					  var container = this.get('container');
					  var widthAvailable = parseInt(Y.one('#document-app').getComputedStyle('width')) - 40;

 					  panelContainer = Y.Node.create(
						  '<div class="' + name + '-container yui3-panel-loading">' + 
							  '   <div class="yui3-widget-hd">'+ title +'</div>' +
							  '   <div class="yui3-widget-bd" style="overflow:hidden">' +
							  '   <div class="'+ name +'Content"></div></div>' +
							  '   <div class="yui3-widget-ft"></div>' +
							  '</div>' 
					  );

					  container.append(panelContainer);
					  
					  
					  panelAttrs = Y.merge({
						  srcNode : panelContainer,
						  width   : widthAvailable / 2,
						  preventOverlap: true,
						  zIndex: 10,
						  render  : true
					  }, attrs);

					  var panel = new Y.Panel(panelAttrs);
					  panel.plug(Y.Plugin.Drag, {handles: ['.yui3-widget-hd']});
					  panel.plug(Y.Plugin.Resize);
					  return panel;
				  },

				  updateFacsimileView: function(){
					  var facsimileContainer = Y.one('.facsimile-container');
					  var facsimileContent = facsimileContainer.one('.yui3-widget-bd');
					  var pagenum = this.get('model').get('pagenumber');
					  facsimileContent.empty();

					  var facsimilePath = this.get('pages')[pagenum-1].facsimile;

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
						  						 navigation: true,
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
				  
				  updateDiplomaticTranscriptView: function() {
					  var pagenum = this.get('model').get('pagenumber');
					  var diplomaticContainer = Y.one('.diplomatic-container');

					  var diplomaticContent = diplomaticContainer.one('.diplomaticContent');

					  var tmpHeight = diplomaticContent.getComputedStyle('height');
					  
					  diplomaticContent.empty();

					  diplomaticContent.append('<div class="faust-ajax-loading ajax-placeholder" style="min-height: ' + tmpHeight + '"/>');

					  this.get('pages')[pagenum - 1].transcriptionFromRanges(function(t) {


						  diplomaticContent.one('.ajax-placeholder').remove(true);
						  var diplomaticTranscriptView = new Y.Faust.DiplomaticTranscriptView({
							  container: diplomaticContainer.one('.diplomaticContent'),
							  visComponent: null,
							  transcript: t
						  });

						  diplomaticTranscriptView.render();

					  });
				  },

				  updateStructureView: function() {
					  var structureContainer = Y.one('.structure-container');					  
					  
					  var structureView =  new Y.Faust.DocumentStructureView({
						  container: structureContainer.one('.structureContent'),
						  document: this.get('fd')
					  });

					  structureView.render();

					  
				  },

				  updateTextView: function() {
					  var textContent = Y.one('.textContent');
					  textContent.empty();
					  this.get('fd').transcriptionFromRanges(function(t) {
						  console.log(t);				
						  var plainTextNode = textContent.append('<p></p>');
						  Y.Array.each(t.textContent.split("\n"), function(line, n) {
							  if (n > 0) {
								  plainTextNode.append("<br>");
							  }
							  plainTextNode.append(Y.config.doc.createTextNode(line));
						  });
						  
					  });
						  

				  },


				  render: function() {
					  var pagenum = this.get('model').get('pagenumber');
					  var container = this.get('container');
					  var widthAvailable = parseInt(Y.one('#document-app').getComputedStyle('width')) - 40;


					  var facsimileContainer = Y.one('.facsimile-container');
					  if (!facsimileContainer) {
						  facsimilePanel = this.panel('facsimile', '${message("document.facsimile")}', {
							  height  : 600,
							  align: {
								  node: '#document-app',
								  points: [
									  Y.WidgetPositionAlign.TL,
									  Y.WidgetPositionAlign.TL
								  ]
							  },
						  });
					  }

 					  var diplomaticContainer = Y.one('.diplomatic-container');
					  if (!diplomaticContainer) {
						  						  
						  var diplomaticPanel = this.panel('diplomatic', '${message("document.diplomatic_transcript")}', {
							  align: {
								  node: '#document-app',
								  points: [
									  Y.WidgetPositionAlign.TR,
									  Y.WidgetPositionAlign.TR
								  ]
							  },
						  });
					  }

					  var structureContainer = Y.one('.structure-container');
					  if (!structureContainer) {
						  						  
						  var diplomaticPanel = this.panel('structure', '${message("document.document_structure")}', {
							  zIndex: 11,
							  align: {							
							  	  node: '#document-app',
							  	  points: [
							  		  Y.WidgetPositionAlign.BR,
							  		  Y.WidgetPositionAlign.BR
							  	  ]
							  },
						  });
						  this.updateStructureView();

					  }
					  
					  var textContainer = Y.one('.text-container');
					  if (!textContainer) {
						  						  
						  var textPanel = this.panel('text', '${message("document.document_text")}', {
							  zIndex: 11,
							  align: {							
							  	  node: '#document-app',
							  	  points: [
							  		  Y.WidgetPositionAlign.BL,
							  		  Y.WidgetPositionAlign.BL
							  	  ]
							  },
						  });
						  
						  this.updateTextView();
					  }


					  this.updateFacsimileView();

					  this.updateDiplomaticTranscriptView();


					  
				  },
				  
			  } , {
				  ATTRS: {
					  pages: null,
					  model: null
				  }
			  });
			  
			  Y.on("contentready", function() {

				  Y.one('#document-app').addClass('faust-ajax-loading');
				  Y.on('faust:document-data-arrives', function(e) {
					  Y.one('#document-app').removeClass('faust-ajax-loading');

					  var navigationModel = new Y.NavigationModel({
						  numberOfPages: e.pages.length	
					  });

					  app = new Y.App({
						  container: '#document-app',
						  root: cp +  '${path}'.replace('faust://', '/'),
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
											  model: model,
											  
											},
											
											{ 
												transition: 'slideLeft',
												update: false
											});
							  this.fire('faust:navigation-done');
						  }
					  });
					  
					  app.set('fd', e.fd);

					  var navigationView = new Y.NavigationView({
						  model: navigationModel,
						  container: Y.one('#document-navigation')
					  });
					  
					  navigationView.render();

					  app.render().dispatch();
				  });

				  Faust.Document.load(new Faust.URI("${document.source?js_string}"), function(fd) {

					  var pages = [];
					  var descendants = fd.descendants();
					  for (i=0; i < descendants.length; i++)
						  if (descendants[i].type === 'page' && descendants[i].transcript)
							  pages.push(descendants[i]);

					  Y.fire('faust:document-data-arrives', {
						  fd: fd,
						  pages: pages
					  });
				  });
				  

			  }, '#document-app');
		  });


</script>
	</@faust.page>
