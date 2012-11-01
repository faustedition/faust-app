<#assign archiveId = document.getMetadataValue("archive")>
<#assign callnumber = document.toString()>
<#assign waId = document.getMetadataValue('wa-id')!"">
<#assign title>${callnumber?html}<#if waId?has_content> &ndash; ${waId?html}</#if></#assign>
<#assign imageLinkBase>${cp}/document/imagelink/${document.source?replace('faust://xml/document/', '')}</#assign>

	<@faust.page title=title>
	<div id="document-navigation" >

	<div style="margin:3em 5em; width:600">
	<button id="prev_page_button">&lt;</button>
	${message("page_abbrev")}
	<input id="pagenum-display" value="1" style="width: 2em; margin-right: 1em" readonly="readonly"></input>
	<span id="pageslider"></span>
	<button id="next_page_button">&gt;</button>
	</div>

	</div>
	<div id="document-app" class="yui-u-1"></div>
	<script type="text/javascript">

YUI().use("app", "node", "event", "slider", "document", "document-yui-view", "button", function(Y) {
	Y.DocumentView = Y.Base.create("document-view", Y.View, [], {
		destructor: function() {
			
		},
		render: function() {
			var pagenum = this.get('pagenum');
			var container = this.get('container');
			var transcriptData = this.get('transcriptData');
			var diplomaticTranscriptView = new Y.Faust.DiplomaticTranscriptView({
				container: container,
				visComponent: null,
				transcript: null
			});
			
			var pages = this.get('pages');
			
			//container.append('<img src="${cp}/static/img/spinner.gif"></img>');

			container.addClass('faust-ajax-loading');

			pages[pagenum - 1].transcriptionFromRanges(function(t) {
				//diplomaticTranscriptView.set('visComponent', Faust.DocumentRanges.transcriptVC(t));
				diplomaticTranscriptView.set('transcript', t);
				diplomaticTranscriptView.render();
				container.removeClass('faust-ajax-loading');
			});

		},
		
	} , {
		ATTRS: {
			pagenum: { 
				validator: function(v) { return Y.Lang.isNumber(v) && (v >= 1); },
				value: 1
			},
			transcriptData: null,
			pages: null,
			app: null
		}
	});
	
	Y.on("contentready", function() {
		
		function _createDocumentUI(pages, app) {

			var pageslider = new Y.Slider({
				axis        : 'x',
				min         : 1,
				max         : 1,
				value       : 1,
				length      : 500
			});

			var pagenumDisplay = Y.one('#pagenum-display');
			var pagesliderContainer = Y.one('#pageslider');

			pageslider.render(pagesliderContainer);			
			pageslider.set('max', pages.length);
			


			// function navigateToPage(pagenum) {
			// 	window.location = cp +  '${path}'.replace('faust://', '/') + '/#' + pagenum;
			// }

			function updatePagenumDisplay(e) {
				pagenumDisplay.set('value', e.newVal);
			}
			
			pageslider.on('valueChange', updatePagenumDisplay);

			pageslider.on('slideEnd', function(e){
				//navigateToPage(e.target.get('value'));
				app.navigate('/' + e.target.get('value'));
			});

			


			


			new Y.Button({
				srcNode: '#prev_page_button',
				on: {
					'click': function() {
						app.navigate("/" + (parseInt(app.get('pagenum')) - 1));
					}
				}
			}).render();

			new Y.Button({
				srcNode: '#next_page_button',
				on: {
					'click': function() {
						app.navigate("/" + (parseInt(app.get('pagenum')) + 1));
					}
				}
			}).render();

			

			function updateUI(e) {
				var pagenum = app.get('pagenum');
				pageslider.set('value', pagenum);
				pagenumDisplay.set('value', pagenum);
				
				
			}

			app.after('faust:navigation-done', updateUI);


		};


		Y.on('faust:document-data-arrives', function(e) {



			app = new Y.App({
				container: '#document-app',
				root: cp +  '${path}'.replace('faust://', '/'),
				//linkSelector: ("#" + this.get("id") + " a"),
				transitions: true,
				serverRouting: false,
				views: {
					'document-view': { type: "DocumentView" }
				}
			});
			
			app.route("/", function() {
				this.navigate("/1");
			});
			
			app.route("/:page", function(req) {
				app.set('pagenum', req.params.page);
				this.showView("document-view", 
							  { pagenum: parseInt(req.params.page),
								fd: this.fd,
								pages: e.pages,
							  },
							  
							  { transition: 'fade'});
				this.fire('faust:navigation-done');
			});
			
			app.set('fd', e.fd);

			_createDocumentUI(e.pages, app);

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
