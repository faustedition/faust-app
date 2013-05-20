YUI.add('facsimile-svgpane', function (Y) {

	var svg = Y.SvgUtils.svg;

    var SvgPane = Y.Base.create("svg-pane", Y.Base, [], {

        loadSvg : function(svgSrc){
			var that = this;
			if (svgSrc) {
				Y.io(svgSrc, {
					method: "GET",
					xdr: { responseXML: false },
					headers: { "Accept": "image/svg" },
					on: {
						success: function(id, o, a) {
							// FIXME this is a silly hack, use a different library
							that.svgContainer.innerHTML = o.responseText;
						}, 
						failure: function(id, o, a) { 
							Y.log("ERROR " + id + " " + a, "info", "Faust") }
					}
				});
			}
		},

		adjustTransform: function() {
			var svgContainer = this.svgContainer;
			var createTransform = function(){
				return svgContainer.viewportElement.createSVGTransform();
			};

			var transforms = this.svgContainer.transform.baseVal;
			transforms.clear();
			var view = this.host.model.get("view");
			var image = this.host.model.get("image");

			// Set position
			var translateTransform = createTransform();
			translateTransform.setTranslate(-view.x, -view.y);
			transforms.appendItem(translateTransform);
			
			// Set scale
			var scale = view.imageHeight / image.height;
			var zoomTransform = createTransform();
			zoomTransform.setScale(scale, scale);
			transforms.appendItem(zoomTransform);

		},
		initializer: function(config) {
			this.host = config.host;
            var svgRoot = this.host.view.ownerSVGElement;
            this.svgContainer = svg("g", {
                id: "svgpane",
                x: "0",
                y: "0",
			});
			svgRoot.appendChild(this.svgContainer);
			this.modelChangeSub = this.host.model.after(["tilesChange", "viewChange", "imageChange"], this.adjustTransform, this);
			this.loadSvg(config.svgSrc);
        },
        destructor: function() {
			this.modelChangeSub.detach();
        },
    }, {
		NAME : 'svgPane',
		NS : 'svg',
        ATTRS: {
            view: {},
            model: {},
        }
    });

    Y.mix(Y.namespace("Faust"), { SvgPane: SvgPane });

}, '0.0', {
	requires: ['facsimile', 'plugin', 'svg-utils']
});