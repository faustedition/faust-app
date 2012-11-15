<#assign archiveId = document.getMetadataValue("archive")>
<#assign callnumber = document.toString()>
<#assign waId = document.getMetadataValue('wa-id')!"">
<#assign title>${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
<#assign imageLinkBase>${cp}/document/imagelink/${document.source?replace('faust://xml/document/', '')}</#assign>
<#assign header>
	<link rel="stylesheet" type="text/css" href="${cp}/static/js/imageviewer/css/iip.css" />
	<script type="text/javascript" src="${cp}/static/js/swfobject.js"></script>
</#assign>




	<@faust.page title=title header=header>
	<div id="document-navigation" style="height: 50px;">

	</div>
	<div id="document-app" class="yui-u-1" style="min-height: 600px;"></div>
	<script type="text/javascript">

YUI().use("app", "node", "event", "slider", "document", "document-yui-view", 
		  "button","panel", "dd-plugin", "resize-plugin", "util",
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
					  container.append('<div style="margin:3em 5em; width:600">' +
									   '   <button id="prev_page_button">&lt;</button>' +
									   '   ${message("page_abbrev")}' +
									   '   <input id="pagenum-display" value="1" style="width: 2em; margin-right: 1em" readonly="readonly"></input>' +
									   '   <span id="pageslider"></span>' +
									   '   <button id="next_page_button">&gt;</button>' +
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
				  destructor: function() {
					  
					  this.get('diplomaticPanel') && this.get('diplomaticPanel').destroy();
					  this.get('facsimilePanel') && this.get('facsimilePanel').destroy();
					  this.get('container').empty();
				  },
				  render: function() {

					  var pagenum = this.get('model').get('pagenumber');
					  var container = this.get('container');
					  var widthAvailable = parseInt(Y.one('#document-app').getComputedStyle('width')) - 40;
					  var facsimileContainer = container.one('.facsimile-container');
					  if (!facsimileContainer) {
						  facsimileContainer = Y.Node.create(
							  '<div class="facsimile-container yui3-panel-loading">' + 
								  '   <div class="yui3-widget-hd">Facsimile</div>' +
								  '   <div class="yui3-widget-bd" style="overflow:hidden">' +
								  '   <div class="facsimileContent"></div></div>' +
								  '   <div class="yui3-widget-ft"></div>' +
								  '</div>' 
						  );
						  
						  container.append(facsimileContainer);
						  
						  var facsimilePanel = new Y.Panel({
						  	  srcNode : facsimileContainer,
						  	  width   : widthAvailable / 2,
							  height  : 500,
							  preventOverlap: true,
						  	  align: {
								  node: '#document-app',
								  points: [
									  Y.WidgetPositionAlign.TL,
									  Y.WidgetPositionAlign.TL
								  ]
							  },
							  zIndex: 100,
						  	  render  : true
						  });

						  facsimilePanel.plug(Y.Plugin.Drag);
						  facsimilePanel.plug(Y.Plugin.Resize);

						  this.set('facsimilePanel', facsimilePanel);

					  }

					  
					  var diplomaticContainer = container.one('.diplomatic-container');
					  if (!diplomaticContainer) {
						  diplomaticContainer = Y.Node.create(
							  '<div class="diplomatic-container yui3-panel-loading" >' + 
								  '   <div class="yui3-widget-hd">Transcript</div>' +
								  '   <div class="yui3-widget-bd" style="overflow:auto">' +
								  '   <div class="diplomaticContent"></div></div>' +
								  '   <div class="yui3-widget-ft"></div>' +
								  '</div>' 
						  );
						  
						  container.append(diplomaticContainer);
						  
						  var diplomaticPanel = new Y.Panel({
						  	  srcNode : diplomaticContainer,
						  	  width   : widthAvailable / 2,
							  preventOverlap: true,
							  align: {
								  node: '#document-app',
								  points: [
									  Y.WidgetPositionAlign.TR,
									  Y.WidgetPositionAlign.TR
								  ]
							  },
							  zIndex: 100,
						  	  render  : true
						  });

						  diplomaticPanel.plug(Y.Plugin.Drag);
						  diplomaticPanel.plug(Y.Plugin.Resize);

						  this.set('diplomaticPanel', diplomaticPanel);
					  }

					  function createDiplomaticTranscriptView() {
					  
						  diplomaticContainer.one('.diplomaticContent').empty();
						  var diplomaticTranscriptView = new Y.Faust.DiplomaticTranscriptView({
							  container: diplomaticContainer.one('.diplomaticContent'),
							  visComponent: null,
							  transcript: null
						  });
						  
						  //container.append('<img src="${cp}/static/img/spinner.gif"></img>');

						  container.addClass('faust-ajax-loading');

						  this.get('pages')[pagenum - 1].transcriptionFromRanges(function(t) {
							  //diplomaticTranscriptView.set('visComponent', Faust.DocumentRanges.transcriptVC(t));
							  diplomaticTranscriptView.set('transcript', t);
							  diplomaticTranscriptView.render();
							  container.removeClass('faust-ajax-loading');
						  });
					  };
					  createDiplomaticTranscriptView.call(this);

					  function createFacsimileView() {
						  var facsimileContent = facsimileContainer.one('.facsimileContent');
						  facsimileContent.empty();

						  var facsimilePath = new Y.Faust.URI(this.get('pages')[pagenum-1].facsimile);						  

						  facsimileContent.append('<div id="transcript-swf"></div>');
						  swfobject.embedSWF(Faust.contextPath + "/static/swf/IIPZoom.swf", 
						  	"transcript-swf",
						  	"100%", "100%",
						  	"9.0.0", Faust.contextPath + "/static/swf/expressInstall.swf", {
						  		server: Faust.FacsimileServer,
						  		image: facsimilePath.encodedPath() + '.tif',
						  		navigation: true,
						  		//credit: "Copyright Digitale Faust-Edition"
						  	}, {
						  		scale: "noscale",
						  		bgcolor: "#000000",
						  		allowfullscreen: "true",
						  		allowscriptaccess: "always",
						  		wmode: "opaque"
						  	});



					  }
					  createFacsimileView.call(this);

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
						  transitions: true,
						  serverRouting: false,
						  views: {
							  'document-view': { type: "DocumentView"}
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
											  fd: this.fd,
											  pages: e.pages,
											  model: model,
											  
											},
											
											{ 
												transition: 'fade',
												update: true
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
