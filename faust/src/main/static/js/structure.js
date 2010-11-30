FaustStructure = function(){};

var minD = 10;
var minLength = 150;
var buttonTop = 0;
var buttonLeft = 0;
var buttonTopBlatt;
var paper;
var prevBlatt = null;
var facsLefthand;
var facsRighthand;
var envWidth;
var initPic;

//***** main *****

FaustStructure.load = function(uri) {
	paper = Raphael(document.getElementById('canvas'), x = 800, y = 600,
			r = 100);

	stripped = "/xml/" + (new Faust.URI(uri).encodedPath());
	Faust.xml(stripped, function (xml) {

		//FaustStructure.test();
		var envelope = new FaustStructure.Envelope();
		
		FaustStructure.structureFromXML(envelope, xml.documentElement);
		
		
		envelope.layout();
		envWidth = envelope.width;
		
		facsHeight = envelope.width * 1.5;

		facsLefthand =  paper.image("", minD, minD, envelope.width, facsHeight);
		facsLefthand.hide();
		
		facsRighthand = paper.image("", envelope.width + minD, minD, envelope.width, facsHeight);
		FaustStructure.setSrc (facsRighthand, initPic);
		
		
		buttonTop = facsHeight + minD;
		buttonLeft = envelope.width + minD;		
				
		envelope.draw(envelope.width + minD, facsHeight + minD * 3);
	})
};



FaustStructure.structureFromXML  = function(element, node){


	for ( var i = 0; i < node.childNodes.length; i++) {
		var childNode = node.childNodes[i];

		var childElement = null;

		if (childNode.nodeName== "Doppelblatt") 
			childElement = new FaustStructure.Doppelblatt();
		if (childNode.nodeName== "Einzelblatt") 
		    childElement = new FaustStructure.Blatt();
		if (childNode.nodeName== "Lage"	) 
			childElement = new FaustStructure.Lage();


		if (childNode.nodeName == "Bogenblatt")
			if (!element.first) {
				element.first = new FaustStructure.Blatt();
				element.first.lengthmod = minD;
				FaustStructure.structureFromXML(element.first, childNode);
			} else {
				element.second = new FaustStructure.Blatt();
				element.second.lengthmod = minD;
				FaustStructure.structureFromXML(element.second, childNode);				
			}
		
		if (childNode.nodeName == "Seite")
			if (childNode.attributes.getNamedItem("Art").nodeValue == "recto") {
				element.recto = new FaustStructure.RSeite();
				FaustStructure.structureFromXML(element.recto, childNode);
				
			} else if (childNode.attributes.getNamedItem("Art").nodeValue == "verso") {
				element.verso = new FaustStructure.VSeite();
				FaustStructure.structureFromXML(element.verso, childNode);				
			} else throw "Art must be recto or verso!";
		
		if (childNode.nodeName == "Metadaten") {
			element.metadata = {};
			FaustStructure.metadataFromXML(element.metadata, childNode);
		}
		

		if (childElement) {
			element.children.push(childElement);
			FaustStructure.structureFromXML(childElement, childNode);
		}

		

	}

};


FaustStructure.metadataFromXML  = function(element, node){


	for ( var i = 0; i < node.childNodes.length; i++) {
		var childNode = node.childNodes[i];

		var childElement = null;

		if (childNode.nodeName== "Digitalisat")  {
			var fausturi = new Faust.URI(childNode.childNodes[0].textContent);
			if (!element.digitalisat)
				element.digitalisat = fausturi;
			//first page?
			// fixme
			if (!initPic)
				initPic = fausturi;
			

		}
		
	}

};


FaustStructure.setSrc = function(pageDisplay, uri) {
	if (uri)
	pageDisplay.attr("src", "https://faustedition.uni-wuerzburg.de/images/iipsrv.fcgi?FIF=" +  uri.encodedPath()
			+ ".tif&SDS=0,90&CNT=1.0&WID="+ envWidth + "&QLT=99&CVT=jpeg");
	else
		pageDisplay.attr("src", "https://faustedition.uni-wuerzburg.de/dev/static/img/emblem.jpg");
		

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

//***** envelope *****

FaustStructure.Envelope = function() {

	this.path = paper.path();
	this.path.attr("stroke-width", "1");
	this.path.attr("stroke-linecap", "round");
	this.children = [];
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

		}

};




//***** doppelblatt *****

FaustStructure.Doppelblatt = function() {

	this.path = paper.path();
	this.path.attr("stroke-width", "3");
	this.path.attr("stroke-linecap", "round");
	this.children = [];
	this.x = 0;
	this.y = 0;

	//this.first = new FaustStructure.Blatt();
	//this.second = new FaustStructure.Blatt();

	// draw a little longer, to show attachment
	//this.first.lengthmod = minD;
	//this.second.lengthmod = minD;
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
	
	// doubly linked list
	this.pBlatt = prevBlatt;
	if (prevBlatt)
		prevBlatt.nBlatt = this;
	prevBlatt = this;

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


			//var debug = paper.rect(abs_x,abs_y, this.width, this.height);
			//debug.attr("stroke", "yellow");
			//debug.attr("stroke-width", 2);


			var lx = (abs_x - this.lengthmod);
			var ly = (abs_y + this.recto.height);

			var right = (abs_x + this.width)  + " " + (abs_y + this.recto.height);
			var left = lx  + " " + ly;

			var pathstr = "M" + right + "L" + left;
			this.path.attr("path", pathstr);
			//this.path.rotate(-5, lx, ly);

			//draw a new button from the last vseite to this rseite
			var button = paper.rect(buttonLeft, buttonTop, (abs_x + this.width - buttonLeft), (abs_y+this.recto.height - buttonTop));
			button.attr("stroke", "none");
			button.attr("fill", "white");
			//set corner for next button
			buttonTop = abs_y + this.recto.height;
			buttonLeft = abs_x;
			button.topblatt = buttonTopBlatt;
			button.bottomblatt = this;
			buttonTopBlatt = this;
			button.toBack();
			button.hover(function (event) {
				this.attr({fill: "gray"});
				if (this.topblatt) {
					FaustStructure.setSrc(facsLefthand, this.topblatt.verso.metadata.digitalisat);
					facsLefthand.show();
				} else 
					facsLefthand.hide();
				
				if (this.bottomblatt) {
					FaustStructure.setSrc(facsRighthand, this.bottomblatt.recto.metadata.digitalisat);
					facsRighthand.show();
				} else 
					facsRighthand.hide();
				
			}, function (event) {
				this.attr({fill: "white"});
			});

			if (this.verso)
				this.verso.draw(abs_x, abs_y + this.recto.height);

		}	
};

//***** rseite *****

FaustStructure.RSeite = function(paper) {

	this.x = 0;
	this.y = 0;
	this.children = [];

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

			//this.height += minD / 2;

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

			//this.height += minD / 2;

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
//window.onload = load;