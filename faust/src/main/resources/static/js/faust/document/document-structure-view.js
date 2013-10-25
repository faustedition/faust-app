YUI.add('document-structure-view', function (Y) {

    var DocumentPaginator = Y.Base.create("document-paginator-view", Y.View, [], {
        events: {
            "a": { "click": "pageClicked"}
        },
        destructor: function() {
            this.navigateHandle && this.navigateHandle.detach();
        },
        render: function() {
            this.get("container").addClass("pure-paginator");
            this.pages = [];
            for (var unit = this.get("document").metadata, unitQueue = []; unit; unit = unitQueue.shift()) {
                if (unit.type == "page") this.pages.push(Y.merge(unit, { pageNum: this.pages.length }));
                if (unit.contents) unitQueue = unit.contents.concat(unitQueue);
            }

            this.page(0);
            this.navigateHandle = Y.on("faust:navigate", this.navigate, this);

            return this;
        },
        page: function(page) {
            if (this.currentPage == page) return;

            var container = this.get("container").empty(),
                totalPages = this.pages.length,
                maxAdjacentPages = this.get("maxAdjacentPages"),
                maxAdjacentLeft = Math.floor(maxAdjacentPages / 2),
                maxAdjacentRight = Math.ceil(maxAdjacentPages / 2),
                firstPage = Math.max(0, Math.min(totalPages - maxAdjacentPages, page - maxAdjacentLeft)),
                lastPage = Math.min(totalPages, Math.max(page + maxAdjacentRight, maxAdjacentPages));

            if (firstPage != 0) {
                container
                    .appendChild(Y.Node.create("<li/>"))
                    .appendChild(Y.Node.create("<a/>"))
                    .addClass("pure-button")
                    .addClass("prev")
                    .setAttrs({
                        "href": "#1",
                        "title": "Page 1",
                        "text": "«"
                    });
            }
            for (var p = firstPage + 1; p<= lastPage; p++) {
                var item = container
                    .appendChild(Y.Node.create("<li/>"))
                    .appendChild(Y.Node.create("<a/>"))
                    .addClass("pure-button")
                    .setAttrs({
                        "href": "#" + p,
                        "title": "Page " + p,
                        "text": "" + p
                    });
                if ((p - 1) == page) {
                    item.addClass("pure-button-active");
                }
            }
            if (lastPage != totalPages) {
                container
                    .appendChild(Y.Node.create("<li/>"))
                    .appendChild(Y.Node.create("<a/>"))
                    .addClass("pure-button")
                    .addClass("next")
                    .setAttrs({
                        "href": "#" + totalPages,
                        "title": "Page " + totalPages,
                        "text": "»"
                    });
            }

            this.currentPage = page;

            Y.fire("faust:navigate", {
                document: this.get("document").id,
                unit: this.pages[this.currentPage].order || 0,
                unitType: this.pages[this.currentPage].type,
                source: this
            });
        },
        navigate: function(e) {
            if (this == e.source || this.get("document").id != e.document || "page" != e.unitType) return;
            var page = Y.Array.find(this.pages, function(page) { return (e.unit == page.order); });
            if (page) this.page(page.pageNum);
        },
        pageClicked: function (e) {
            e.preventDefault();
            var href = e.currentTarget.get("href");
            this.page(parseInt(href.substring(href.lastIndexOf("#") + 1)) - 1);
        }
    }, {
        ATTRS: {
            document: {},
            maxAdjacentPages: { value: 7 }
        }
    });

    var DocumentTree = Y.Base.create("document-tree-view", Y.View, [], {
        events: {
            ".label": { "click": "toggle"}
        },
        initializer: function() {
            Y.on("faust:navigate", this.navigate, this);
        },
        render: function() {
            var container = this.get("container");

            this.document = parseInt(container.getData("document-id"));

            container.all("li").each(function(node) {
                if (node.one("> ol")) {
                    node.addClass("branch-node").one(".label").prepend(Y.Node.create("<i></i>").addClass("collapse-handle").set("text", "-"))
                } else {
                    node.addClass("leaf-node")
                }
            });

            if (!this.get("expandAll")) {
                container.all("li.branch-node").each(this.collapse, this);
            }
            return this;
        },
        expand: function(node) {
            node.ancestors("li.collapsed", true).each(function(node) {
                node.removeClass("collapsed");
                node.one(".collapse-handle").set("text", "-")
            });
            return node;
        },
        collapse: function(node) {
            node.ancestor().all("li.branch-node").each(function(node) {
                node.addClass("collapsed");
                node.one(".collapse-handle").set("text", "+");
            });
            return node;
        },
        activate: function(node) {
            this.active && this.active.removeClass("active");
            this.active = this.expand(node).addClass("active");

            if (!this.active.inRegion(this.get("container"))) this.active.scrollIntoView(true);

            Y.fire("faust:navigate", {
                document: this.document,
                unit: parseInt(this.active.getData("order")),
                unitType: this.active.getData("type"),
                source: this
            });

            return this.active;
        },
        toggle: function (e) {
            e.preventDefault();
            var node = e.currentTarget.ancestor("li");
            if (this.active && this.active.compareTo(node)) {
                node.hasClass("collapsed") ? this.expand(node) : this.collapse(node);
            } else {
                this.activate(node);
            }
        },
        navigate: function(e) {
            if (this == e.source) return;
            if (this.document == e.document) {
                var materialUnit = this.get("container").one("li[data-order='" + (e.unit || "0") + "']");
                if (materialUnit) this.activate(materialUnit);
            }
        }
    }, {
        ATTRS: {
            "expandAll": { value: true, validator: Y.Lang.isBoolean }
        }
    });

    var DocumentStructureView = Y.Base.create("document-structure-view", Y.View, [], {
		
		destructor: function() {
			
		},
		
		


		render: function() {
			this.get('container').empty();

			// TODO: cleanup

			FaustStructure = function(){};

			var minD = 15;
			var minLength = 180;
			var buttonTop = 0;
			var buttonLeft = 0;
			var buttonTopBlatt;
			var paper;
			var facsLefthand;
			var facsRighthand;
			var envWidth;
			//var initPic;
			var lockButton;


			//***** main *****

			FaustStructure.render = function(document, container) {
				paper = Raphael(container.getDOMNode(), 800, 600,
								100);

				var envelope = new FaustStructure.Envelope();

				FaustStructure.structureFromMU(envelope, document);

				envelope.layout();
				envWidth = envelope.width;

				//facsHeight = envelope.width * 1.5;
				facsHeight = 0;

				//facsLefthand =  paper.image("", minD, minD, envelope.width, facsHeight);
				//facsLefthand.hide();

				//facsRighthand = paper.image("", envelope.width + minD, minD, envelope.width, facsHeight);
				//facsRighthand.hide();


				buttonTop = facsHeight + minD;
				buttonLeft = envelope.width + minD;		

				//FaustStructure.displayMetadata(document.getElementById("metadata"), envelope.metadata);
				
				//envelope.draw(envelope.width + minD, facsHeight + minD * 3);
				envelope.draw(minD, facsHeight + minD * 3);


			};
			


			FaustStructure.structureFromMU  = function(element, node){

				var descendants = node.descendants();
				for ( var i = 0; i < descendants.length; i++) {
					var childNode = descendants[i];

					var childElement = null;

					if (childNode.type== "sheet") 
						childElement = new FaustStructure.Doppelblatt();
					if (childNode.type== "disjunctleaf") 
						childElement = new FaustStructure.Blatt();
					if (childNode.type== "layer") 
						childElement = new FaustStructure.Lage();
					if (childNode.type== "patch") 
						childElement = new FaustStructure.Anbringung();


					if (childElement && element.children) {
						element.children.push(childElement);
						FaustStructure.structureFromMU(childElement, childNode);
					}


					if (childNode.type == "leaf")
						if (!element.first) {
							element.first = new FaustStructure.Blatt();
							element.first.lengthmod = minD;
							FaustStructure.structureFromMU(element.first, childNode);
						} else {
							element.second = new FaustStructure.Blatt();
							element.second.lengthmod = minD;
							FaustStructure.structureFromMU(element.second, childNode);				
						}

					if (childNode.type == "page")
						//if (childNode.attributes.getNamedItem("Art").nodeValue == "recto") {
						//	element.recto = new FaustStructure.RSeite();
						//	FaustStructure.structureFromMU(element.recto, childNode);

						//} else if (childNode.attributes.getNamedItem("Art").nodeValue == "verso") {
						//	element.verso = new FaustStructure.VSeite();
						//	FaustStructure.structureFromMU(element.verso, childNode);				
						// } else throw "Art must be recto or verso!";

						if (childNode.type == "Metadaten") {
							FaustStructure.metadataFromXML(element.metadata, childNode);
						}
					
				}

			};


			FaustStructure.metadataFromXML  = function(element, node){

				for ( var i = 0; i < node.childNodes.length; i++) {
					
					var childNode = node.childNodes[i];
					
					if (childNode.nodeType != childNode.ELEMENT_NODE)
						continue;

					if (childNode.nodeName== "Digitalisat")  {
						var fausturi = new Faust.URI(childNode.childNodes[0].textContent);
						if (!element.digitalisat)
							element.digitalisat = fausturi;
						// first page?
						// fixme
						//if (!initPic)
						//initPic = fausturi;
					}

					
					if (childNode.nodeName == "Foliierung")  {
						element.foliierung = {};
						FaustStructure.metadataFromXML(element.foliierung, childNode);
						continue;
					}

					if (childNode.nodeName == "Blattnummer")  {
						var z = childNode.attributes.getNamedItem("Zählung");
						element[z.value] = childNode.textContent;
						continue;
					}
					
					//attach everything to the element
					//element[childNode.nodeName] = childNode.textContent.replace(/^\s*|\s(?=\s)|\s*$/g, "");
					element[childNode.nodeName] = FaustStructure.linearizeMetadata(childNode);
					
				}
			};

			FaustStructure.linearizeMetadata = function (node) {

				var txt = "";
				
				for ( var i = 0; i < node.childNodes.length; i++) {
					
					var childNode = node.childNodes[i];
					if (childNode.nodeType == childNode.ELEMENT_NODE)
						txt += childNode.nodeName + ": ";
					if (childNode.nodeType == childNode.TEXT_NODE)
						txt += childNode.textContent.replace(/^\s*|\s(?=\s)|\s*$/g, "");

					txt += FaustStructure.linearizeMetadata(childNode);

					if (childNode.nodeType == childNode.ELEMENT_NODE)
						txt += ". ";

				}	
				
				return txt;
			}


			FaustStructure.setSrc = function(pageDisplay, uri) {

				// first, set blank until image is loaded.
				pageDisplay.attr("src", "");

				// fixme: a better way to validate uris
				if (uri && uri.components) {
					pageDisplay.attr("src", "https://faustedition.uni-wuerzburg.de/images/iipsrv.fcgi?FIF=" +  uri.encodedPath()
									 + ".tif&SDS=0,90&CNT=1.0&WID="+ envWidth + "&QLT=99&CVT=jpeg");

					// link to transcript

					var transcriptPath = uriMap[uri.components[2]];
					if (transcriptPath) {
						pageDisplay.attr({
							cursor:"pointer",
							href: Faust.contextPath + transcriptPath
						});

					}
					else {
						pageDisplay.attr({
							cursor: "not-allowed",
							href: null
						});


					}


				}
				else {
					pageDisplay.attr("src", "https://faustedition.uni-wuerzburg.de/dev/static/img/emblem.jpg");
					pageDisplay.href = null;
				}

				
				


			}

			FaustStructure.displayMetadata = function(metadataDisplay, metadata) {
				
				metadataDisplay.appendChild(document.createElement("br"));
				for (var i in metadata) {
					
					var header = document.createElement("b");
					var title = document.createTextNode(i + ": ");
					var br = document.createElement("br");
					header.appendChild(title);
					metadataDisplay.appendChild(header);
					metadataDisplay.appendChild(document.createTextNode(metadata[i]));
					metadataDisplay.appendChild(br);
					
					

				}
			}

			FaustStructure.test = function() {

				var paper = Raphael(document.getElementById('canvas'), x = 800, y = 600,
									r = 100);

				var d1 = new FaustStructure.Doppelblatt();
				d4 = new FaustStructure.Doppelblatt();
				d5 = new FaustStructure.Doppelblatt();
				d6 = new FaustStructure.Doppelblatt();

				d1.children.push(d4);
				d4.children.push(d5);
				d4.children.push(d6);

				e1 = new FaustStructure.Blatt();
				d4.children.push(e1);

				d2 = new FaustStructure.Doppelblatt();
				a1 = new FaustStructure.Anbringung();
				a2 = new FaustStructure.Anbringung();
				a3 = new FaustStructure.Anbringung();


				a4 = new FaustStructure.Anbringung();
				a5 = new FaustStructure.Anbringung();

				d2.first.recto.children.push(a1);
				d2.first.recto.children.push(a2);
				d2.first.recto.children.push(a3);

				d4.first.verso.children.push(a4);
				d4.first.verso.children.push(a5);


				d3 = new FaustStructure.Doppelblatt();
				l1 = new FaustStructure.Lage();
				l1.children.push(d2);
				l1.children.push(d3);
				d1.children.push(l1);

				d1.layout();
				buttonTop = 0;
				buttonLeft = 400;
				d1.draw(400, 50);

			};

			//***** button *****

			FaustStructure.Button = function(x, y, width, height, topblatt, bottomblatt) {

				var button = paper.rect(x, y, width, height);
				button.attr("stroke", "none");
				button.attr("fill", "gray");
				button.attr("fill-opacity", 0);
				button.toFront();
				button.topblatt = topblatt;
				button.bottomblatt = bottomblatt;

				button.toBack();
				button.hover(function (event) {

					this.animate({"fill-opacity": .7}, 300, ">");
					this.toFront();
					// if (this.topblatt) {
					// 	FaustStructure.setSrc(facsLefthand, this.topblatt.verso.metadata.digitalisat);
					// 	facsLefthand.show();
					// } else 
					// 	facsLefthand.hide();

					// if (this.bottomblatt) {
					// 	FaustStructure.setSrc(facsRighthand, this.bottomblatt.recto.metadata.digitalisat);
					// 	facsRighthand.show();
					// } else 
					// 	facsRighthand.hide();

				}, function (event) {
					this.animate({"fill-opacity": 0}, 300, ">");

					// if (lockButton.topblatt) {
					// 	FaustStructure.setSrc(facsLefthand, lockButton.topblatt.verso.metadata.digitalisat);
					// 	facsLefthand.show();
					// } else 
					// 	facsLefthand.hide();

					// if (lockButton.bottomblatt) {
					// 	FaustStructure.setSrc(facsRighthand, lockButton.bottomblatt.recto.metadata.digitalisat);
					// 	facsRighthand.show();
					// } else 
					// 	facsRighthand.hide();


				});

				button.click(function (event) {

					var x = this.attrs.x;
					var y = this.attrs.y;
					var width = this.attrs.width;
					var height = this.attrs.height;

					lockButton.animate({"fill-opacity": .7,
										"x": x,
										"y": y,
										"width": width,
										"height": height
									   }, 0, ">");
					lockButton.toFront();
					lockButton.topblatt = this.topblatt;
					lockButton.bottomblatt = this.bottomblatt;
				});


			}

			//***** envelope *****

			FaustStructure.Envelope = function() {

				this.path = paper.path();
				this.path.attr("stroke-width", "1");
				this.path.attr("stroke-linecap", "round");
				this.children = [];
				this.metadata = {};
				this.x = 0;
				this.y = 0;

			};


			FaustStructure.Envelope.prototype = {

				toString : function() {return "Envelope"},

				layout : function() {
					// if I contain sth.
					// layout that first

					this.width = 0;
					this.height = 0;

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.layout();
						child.y = this.height;
						this.height += minD;
						this.height += child.height;
						this.width = Math.max(child.width, this.width - minD) + minD;
						child.x = this.x + minD;
					}
					this.height -= minD;


					// I now know how wide all of my children are;
					// make every child as wide as the widest one

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.width = this.width - minD;
					}

				},

				draw : function(x, y) {

					// draw lockButton
					lockButton = paper.rect(0,0,0,0);
					lockButton.attr("stroke", "none");
					lockButton.attr("fill", "red");
					lockButton.attr("fill-opacity", 0);
					lockButton.toFront();

					var abs_x = this.x + x;
					var abs_y = this.y + y;

					var topleft = (abs_x) + " " + (abs_y);
					var bottomleft = (abs_x) + " " + (abs_y + this.height);

					var pathstr = "M" + topleft + "L" + bottomleft;

					this.path.attr("path", pathstr);

					if (this.children.length > 0)
						for ( var i = 0; i < this.children.length; i++) {
							var child = this.children[i];
							child.draw(abs_x, abs_y);
						}

					// draw the last button

					new FaustStructure.Button(abs_x, buttonTop, this.width , (abs_y+this.height - buttonTop) +  minD, buttonTopBlatt, null);


				}

			};




			//***** doppelblatt *****

			FaustStructure.Doppelblatt = function() {

				this.path = paper.path();
				this.path.attr("stroke-width", "3");
				this.path.attr("stroke-linecap", "round");
				this.children = [];
				this.metadata = {};

				this.x = 0;
				this.y = 0;

				// this.first = new FaustStructure.Blatt();
				// this.second = new FaustStructure.Blatt();

				// draw a little longer, to show attachment
				// this.first.lengthmod = minD;
				// this.second.lengthmod = minD;
			};


			FaustStructure.Doppelblatt.prototype = {

				toString : function() {return "Doppelblatt"},

				layout : function() {
					// if I contain sth.
					// layout that first

					this.width = 0;
					this.height = 0;

					this.first.layout();
					this.second.layout();
					this.first.x = minD;
					this.second.x = minD;
					this.first.y = 0;

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.layout();
						this.height += minD;
						child.y = this.height;
						this.height += child.height;
						this.width = Math.max(child.width, this.width - minD) + minD;
						child.x = this.x + minD;
					}
					this.height += this.first.height;
					this.height += this.second.height;


					this.width = Math.max(this.first.width, this.width - minD) + minD;
					this.width = Math.max(this.second.width, this.width - minD) + minD;

					this.height += minD;

					// I now know how wide all of my children are;
					// make every child as wide as the widest one

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.width = this.width - minD;
					}

					this.second.y = this.height - this.second.height;
					this.first.width = this.width - minD;
					this.second.width = this.width - minD;

				},


				draw : function(x, y) {

					var abs_x = this.x + x;
					var abs_y = this.y + y;

					var topleft = (abs_x) + " " + (abs_y + this.first.recto.height);
					var bottomleft = (abs_x) + " " + (abs_y + this.height - this.second.verso.height - this.second.height);

					var pathstr = "M" + topleft + "L" + bottomleft;

					this.path.attr("path", pathstr);

					this.first.draw(abs_x, abs_y);

					if (this.children.length > 0)
						for ( var i = 0; i < this.children.length; i++) {
							var child = this.children[i];
							child.draw(abs_x, abs_y + this.first.height);
						}

					this.second.draw(abs_x, abs_y);

				}

			};


			//***** blatt *****

			FaustStructure.Blatt = function() {

				this.path = paper.path();
				this.path.attr("stroke-width", "3");

				this.x = 0;
				this.y = 0;

				this.lengthmod = 0;
				this.recto = new FaustStructure.RSeite();
				this.verso = new FaustStructure.VSeite();
				this.metadata = {};


			};


			FaustStructure.Blatt.prototype = {

				toString : function() {return "Blatt"},

				layout : function() {

					this.width = minLength;
					this.height = 0;
					if (this.recto) {
						this.recto.layout();
						this.height += this.recto.height;
					}
					if (this.verso) {
						this.verso.layout();
						this.height += this.verso.height;
					}

				},

				draw : function(x, y) {

					var abs_x = this.x + x;
					var abs_y = this.y + y;



					if (this.recto)
						this.recto.draw(abs_x, abs_y);


					// var debug = paper.rect(abs_x,abs_y, this.width, this.height);
					// debug.attr("stroke", "yellow");
					// debug.attr("stroke-width", 2);


					var lx = (abs_x - this.lengthmod);
					var ly = (abs_y + this.recto.height);

					var right = (abs_x + this.width)  + " " + (abs_y + this.recto.height);
					var left = lx  + " " + ly;

					var pathstr = "M" + right + "L" + left;
					this.path.attr("path", pathstr);
					// this.path.rotate(-5, lx, ly);

					// draw a new button from the last vseite to this rseite

					new FaustStructure.Button(buttonLeft, buttonTop, (abs_x + this.width - buttonLeft), (abs_y+this.recto.height - buttonTop), buttonTopBlatt, this);

					// set corner for next button
					buttonTop = abs_y + this.recto.height;
					buttonLeft = abs_x;
					buttonTopBlatt = this;

					if (this.verso)
						this.verso.draw(abs_x, abs_y + this.recto.height);
					
					if (this.metadata.foliierung)
						var i = 0;
					for (var z in this.metadata.foliierung) {
						i++;
						if (this.metadata.foliierung[z] != "keine") {
				    		var txt = paper.text (abs_x + this.width + i * minD, ly, this.metadata.foliierung[z]);
				    		txt.attr("font-size", 12);
				    		txt.attr("title", z);
						}
					}
				}	
			};

			//***** rseite *****

			FaustStructure.RSeite = function(paper) {

				this.x = 0;
				this.y = 0;
				this.children = [];
				this.metadata= {};
			};


			FaustStructure.RSeite.prototype = {

				toString : function() {return "RSeite"},

				layout : function() {

					this.width = minLength;
					this.height = 0;

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.type="recto";
						child.layout();
						// no space between children
						child.y = this.height;
						this.height += child.height;
						this.width = Math.max(child.width, this.width);
						child.x = this.x;
					}

					// this.height += minD / 2;

					// I now know how wide all of my children are;
					// make every child as wide as the widest one

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.width = this.width;
					}

				},

				draw : function(x, y) {

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.draw(x,y);
					}


				}

			};
			//***** vseite ******

			FaustStructure.VSeite = function() {

				this.x = 0;
				this.y = 0;
				this.children = [];
				this.metadata = {};
			};

			FaustStructure.VSeite.prototype = {

				toString : function() {return "VSeite"},

				layout : function() {

					this.width = minLength;
					this.height = 0;

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.layout();
						child.type="verso";
						// no space between children
						child.y = this.height;
						this.height += child.height;
						this.width = Math.max(child.width, this.width);
						child.x = this.x;
					}

					// this.height += minD / 2;

					// I now know how wide all of my children are;
					// make every child as wide as the widest one

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.width = this.width;
					}

				},

				draw : function(x, y) {

					for ( var i = 0; i < this.children.length; i++) {
						var child = this.children[i];
						child.draw(x,y);
					}
				}
			};

			//***** anbrigung *****

			FaustStructure.Anbringung = function() {

				this.path = paper.path();
				this.path.attr("stroke-width", "3");
				this.x = 0;
				this.y = 0;
				this.metadata = {};
			};

			FaustStructure.Anbringung.prototype = {

				toString : function() {return "Anbringung"},

				layout : function() {

					this.width = minLength;
					this.height = minD;
				},

				draw : function(x, y) {

					var abs_x = this.x + x;
					var abs_y = this.y + y;

					if (this.type=="recto") {
						var right = (abs_x + this.width) + " " + (abs_y);
						var left = (abs_x + this.width / 2) + " " + (abs_y + this.height);
					} else {
						var right = (abs_x + this.width) + " " + (abs_y +  this.height);
						var left = (abs_x + this.width / 2) + " " + (abs_y);		
					}

					var pathstr = "M" + right + "L" + left;
					this.path.attr("path", pathstr);

				}
			};


			//***** lage *****

			FaustStructure.Lage =  function() {

				this.path = paper.path();
				this.path.attr("stroke-width", "1");
				this.children = [];
				this.x = 0;
				this.y = 0;
				this.metadata = {};
			};

			FaustStructure.Lage.prototype = {

				toString : function() {return "Lage"},

				layout : function() {
					// if I contain sth.
					// layout that first
					if (this.children.length > 0) {

						this.width = 0;
						this.height = 0;

						for ( var i = 0; i < this.children.length; i++) {
							var child = this.children[i];
							child.layout();
							this.height += minD;
							child.y = this.height;
							this.height += child.height;
							this.width = Math.max(child.width, this.width - minD) + minD;
							child.x = this.x + minD;
						}

						// I now know how wide all of my children are;
						// make every child as wide as the widest one

						for ( var i = 0; i < this.children.length; i++) {
							var child = this.children[i];
							child.width = this.width - minD;
						}

						this.height += minD;

					} else {
						this.width = minLength;
						this.height = minD;
					}

				},

				draw : function(x, y) {

					var abs_x = this.x + x;
					var abs_y = this.y + y;

					var topright = (abs_x + 2 * minD) + " " + (abs_y);
					var topleft = (abs_x) + " " + (abs_y);
					var bottomleft = (abs_x) + " " + (abs_y + this.height);
					var bottomright = (abs_x + 2 * minD) + " " + (abs_y + this.height);

					var pathstr = "M" + topright + "L" + topleft + "L" + bottomleft + "L"
						+ bottomright;

					this.path.attr("path", pathstr);

					if (this.children.length > 0)
						for ( var i = 0; i < this.children.length; i++) {
							var child = this.children[i];
							child.draw(abs_x, abs_y);
						}

				}

			};


			var uriMap = {
				//IH.32
				"gsa/391282/391282_0002":"/document/faust/2.1/gsa_391282.xml#1",
				"gsa/391282/391282_0003":"/document/faust/2.1/gsa_391282.xml#2",
				"gsa/391282/391282_0004":"/document/faust/2.1/gsa_391282.xml#2",
				"gsa/391282/391282_0005":"/document/faust/2.1/gsa_391282.xml#4",
				"gsa/391282/391282_0006":"/document/faust/2.1/gsa_391282.xml#6",
				"gsa/391282/391282_0007":"/document/faust/2.1/gsa_391282.xml#6",
				"gsa/391282/391282_0008":"/document/faust/2.1/gsa_391282.xml#6",
				"gsa/391282/391282_0009":"/document/faust/2.1/gsa_391282.xml#9",
				"gsa/391282/391282_0010":"/document/faust/2.1/gsa_391282.xml#9",
				"gsa/391282/391282_0011":"/document/faust/2.1/gsa_391282.xml#9",
				"gsa/391282/391282_0012":"/document/faust/2.1/gsa_391282.xml#11",
				"gsa/391282/391282_0013":"/document/faust/2.1/gsa_391282.xml#12",
				"gsa/391282/391282_0014":"/document/faust/2.1/gsa_391282.xml#13",
				"gsa/391282/391282_0015":"/document/faust/2.1/gsa_391282.xml#15",
				"gsa/391282/391282_0016":"/document/faust/2.1/gsa_391282.xml#15",
				"gsa/391282/391282_0017":"/document/faust/2.1/gsa_391282.xml#16",
				"gsa/391282/391282_00F":"/document/faust/2.1/gsa_391282.xml#17",
				
				//VH.2
				"gsa/390883/390883_0002":"/document/faust/2.5/gsa_390883.xml#1",
				"gsa/390883/390883_0003":"/document/faust/2.5/gsa_390883.xml#2",
				"gsa/390883/390883_0004":"/document/faust/2.5/gsa_390883.xml#3",
				"gsa/390883/390883_0005":"/document/faust/2.5/gsa_390883.xml#4",
				"gsa/390883/390883_0006":"/document/faust/2.5/gsa_390883.xml#5",
				"gsa/390883/390883_0008":"/document/faust/2.5/gsa_390883.xml#6",
				"gsa/390883/390883_0007":"/document/faust/2.5/gsa_390883.xml#7",
				"gsa/390883/390883_0009":"/document/faust/2.5/gsa_390883.xml#8",
				"gsa/390883/390883_0010":"/document/faust/2.5/gsa_390883.xml#8",
				"gsa/390883/390883_0011":"/document/faust/2.5/gsa_390883.xml#9",
				"gsa/390883/390883_0012":"/document/faust/2.5/gsa_390883.xml#9",
				"gsa/390883/390883_0013":"/document/faust/2.5/gsa_390883.xml#10",
				"gsa/390883/FA_124_0002":"/document/faust/2.5/gsa_390883.xml#11",				
				"gsa/390883/390883_0014":"/document/faust/2.5/gsa_390883.xml#12",
				"gsa/390883/390883_0015":"/document/faust/2.5/gsa_390883.xml#13",
				"gsa/390883/390883_0016":"/document/faust/2.5/gsa_390883.xml#13",
				"gsa/390883/390883_0017":"/document/faust/2.5/gsa_390883.xml#14",
				"gsa/390883/390883_0018":"/document/faust/2.5/gsa_390883.xml#15",
				"gsa/390883/390883_0019":"/document/faust/2.5/gsa_390883.xml#15",
				"gsa/390883/390883_0025":"/document/faust/2.5/gsa_390883.xml#16",
				"gsa/390883/390883_0026":"/document/faust/2.5/gsa_390883.xml#16",
				"gsa/390883/FA_124_0005":"/document/faust/2.5/gsa_390883.xml#17",
				"gsa/390883/390883_0023":"/document/faust/2.5/gsa_390883.xml#18",
				"gsa/390883/390883_0024":"/document/faust/2.5/gsa_390883.xml#18",
				"gsa/390883/390883_0020":"/document/faust/2.5/gsa_390883.xml#19",
				"gsa/390883/390883_0027":"/document/faust/2.5/gsa_390883.xml#20",
				"gsa/390883/390883_0028":"/document/faust/2.5/gsa_390883.xml#20",
				"gsa/390883/390883_0029":"/document/faust/2.5/gsa_390883.xml#21",
				"gsa/390883/390883_0030":"/document/faust/2.5/gsa_390883.xml#21",
				"gsa/390883/390883_0021":"/document/faust/2.5/gsa_390883.xml#22",
				"gsa/390883/390883_0022":"/document/faust/2.5/gsa_390883.xml#22",
				"gsa/390883/390883_0031":"/document/faust/2.5/gsa_390883.xml#22",
				"gsa/390883/390883_0032":"/document/faust/2.5/gsa_390883.xml#22",
				"gsa/390883/390883_0033":"/document/faust/2.5/gsa_390883.xml#23",
				"gsa/390883/390883_0034":"/document/faust/2.5/gsa_390883.xml#23",
				"gsa/390883/390883_0035":"/document/faust/2.5/gsa_390883.xml#24",
				"gsa/390883/390883_0036":"/document/faust/2.5/gsa_390883.xml#25",
				"gsa/390883/390883_0037":"/document/faust/2.5/gsa_390883.xml#25",
				"gsa/390883/390883_0038":"/document/faust/2.5/gsa_390883.xml#26",
				"gsa/390883/390883_0039":"/document/faust/2.5/gsa_390883.xml#26",
				"gsa/390883/390883_0040":"/document/faust/2.5/gsa_390883.xml#26",
				"gsa/390883/390883_0041":"/document/faust/2.5/gsa_390883.xml#27"

			}
			
			FaustStructure.render(this.get('document'), this.get('container'));
		}
		
	} , {
		ATTRS: {
			rootVC: { validator: function() { return true; } }
		}
	});

	Y.mix(Y.namespace("Faust"), {
        DocumentStructureView: DocumentStructureView,
        DocumentTree: DocumentTree,
        DocumentPaginator: DocumentPaginator
	});
	
}, "0.0", {
	requires: ["view", "array-extras" ]
});

