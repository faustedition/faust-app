YUI.add('paged-text-display', function(Y){

		/**
		 * An area in which paginated text is displayed.
		 * @extends TextDisplayView
		 * 
		 * @param linesPerPage Integer Lines of text per page. Default is 0 or Infinity
		 * @param currentPage Currently displayed page number (0-based)
		 */
		var PagedTextDisplayView = Y.Base.create('pagedTextDisplayView', Y.Faust.TextDisplayView, [], {
			
			initializer: function(config){
				this._setNewlines();
				
				this.after('linesPerPageChange', this._setPages, this);
				this.after('textChange', this._setNewlines, this);
			},
			
			_setNewlines : function(){
				var text = this.get('text');
				
				this.newlines = [-1];
				var i = -1;
				while((i = text.content.indexOf('\n', i+1)) != -1)
				{
					this.newlines.push(i);
				}
				this.newlines.push(text.contentLength);
				this._setPages();
			},
			
			_setPages : function(){
				var linesPerPage = this.get('linesPerPage');
				var text = this.get('text');

				var pagebreaks = this.newlines;
				pagebreaks.pop(); //text.contentLength
				pagebreaks = Y.Array.filter(pagebreaks, function(item, i){
						return i % linesPerPage === 0;
				});
				pagebreaks.push(text.contentLength);
				
				this.pages = [];
				
				for(var i = 1; i < pagebreaks.length; ++i)
				{
					this.pages.push(new Y.Faust.Range(pagebreaks[i-1], pagebreaks[i]));
				}
			},
			
			/**
			 * Output HTML representation of the current page.
			 * @param page New current page number. Defaults to the current page.
			 */
			render: function(page){
				page = (page !== undefined) ? page : this.get('currentPage');
				if(page >= this.pages.length)
					throw new RangeError('page number out of range');
				this.set('currentPage', page);
				PagedTextDisplayView.superclass.render.call(this, this.pages[page]);
			},
			
			/**
			 * Renders the next page, if existing.
			 */
			nextPage: function(){
				var currentPage = this.get('currentPage');
				if(currentPage + 1 < this.pages.length)
				{
					this.set('currentPage', ++currentPage);
					this.render(currentPage);
				}
			},
			
			/**
			 * Renders the previous page, if existing
			 */
			prevPage: function(){
				var currentPage = this.get('currentPage');
				if(currentPage > 0)
				{
					this.set('currentPage', --currentPage);
					this.render(currentPage);
				}
			},
			
			
			
			
		}, {
			ATTRS: {
				linesPerPage : {value: Infinity},
				currentPage : {value: 0},
			}
		});
		
		Y.mix(Y.namespace('Faust'), {
			PagedTextDisplayView: PagedTextDisplayView,
		});
	
	},
	'0.0',
	{
		requires: ['text-display', 'text-annotation'],
	}
);