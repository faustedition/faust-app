<div id="chart" style="text-align: center"></div>
<script type="text/javascript">
	Faust.YUI().use("oop", "dump", function(Y) {
		var data = [
			{ sigil: "V.H15", intervals : [ 
				{ url: "faust/2.5/gsa_391506.xml#1", interval: [11519, 11526] } 
				] },
			{ sigil: "V.H13v", intervals: [ 
				{ url: "faust/2.5/gsa_391027.xml#3", interval: [11511, 11530] } 
				] },
			{ sigil: "V.H14", intervals: [ 
				{ url: "faust/2.5/gsa_391505.xml#1", interval: [11511, 11530] } 
				] },
			{ sigil: "V.H18", intervals: [ 
				{ url: "faust/2.5/gsa_390757.xml#1", interval: [11595, 11603] } 
				] },
			{ sigil: "V.H17r", intervals: [ 
				{ url: "faust/2.5/gsa_391510.xml#1", interval: [11593, 11595] } 
				] },
			{ sigil: "V.H2", intervals: [ 
				{ url: "faust/2.5/gsa_390883.xml#12", interval: [11511, 11530] },
				{ url: "faust/2.5/gsa_390883.xml#13", interval: [11539, 11590] }, 
				{ url: "faust/2.5/gsa_390883.xml#14", interval: [11593, 11603] } 
				] },
			{ sigil: "V.H16", intervals: [ 
				{ url: "faust/2.5/gsa_391507.xml#1", interval: [11573, 11576] } 
				] },
			{ sigil: "V.H", intervals: [ 
				{ url: "faust/2/gsa_391098.xml#360", interval: [11511, 11522] },
				{ url: "faust/2/gsa_391098.xml#415", interval: [11523, 11543] },
				{ url: "faust/2/gsa_391098.xml#416", interval: [11544, 11562] },
				{ url: "faust/2/gsa_391098.xml#417", interval: [11563, 11586] },
				{ url: "faust/2/gsa_391098.xml#418", interval: [11587, 11593] },
				{ url: "faust/2/gsa_391098.xml#419", interval: [11594, 11619] }
				] }
		];
		data.reverse();
		
		var minLine = 100000;
		var maxLine = 0;
		Y.each(data, function(m) {
			Y.each(m.intervals, function(i) {
				minLine = Math.min(minLine, i.interval[0]);
				maxLine = Math.max(maxLine, i.interval[1]);
			});
		});

		var width = 800;
		var height = 250;
		var lineScale = pv.Scale.linear(minLine, maxLine).range(0, width);
		var barColor = pv.Scale.linear(0, data.length - 1).range('#805F40', '#999');
		
		var panel = new pv.Panel().canvas("chart")
			.width(width)
			.height(height)
			.top(25)
			.bottom(25)
			.left(50)
		
		panel.add(pv.Rule)
			.data(pv.range(data.length))
			.strokeStyle("#eee")
			.top(function(d) { return d * this.parent.height() / data.length })
			.anchor("left")
				.add(pv.Label)
				.text(function(d) { return data[d].sigil })
				.textMargin(10);
	
		panel.add(pv.Rule)
			.data(lineScale.ticks())
			.strokeStyle("#eee")
			.left(lineScale)
			.anchor("bottom")
				.add(pv.Label)
				.text(lineScale.tickFormat);
				
		panel.add(pv.Panel)
			.data(data)
			.height(height / (data.length * 2))
			.top(function(d) { return  (this.index * this.parent.height() / data.length) - this.height() / 2 })
				.add(pv.Bar)
				.data(function(m) { return m.intervals })
				.left(function(d) { return lineScale(d.interval[0]) })
				.width(function(d) { return lineScale(d.interval[1]) - lineScale(d.interval[0]) })
				.fillStyle(function() { return barColor(this.parent.index) })
				.event("click", function(n) { self.location = Faust.contextPath + "/document/" + n.url; });
				
		panel.render();
	});
</script>



<p>
	<span class="font-size-1">Paralipomena:</span>&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/faust/2.5/gsa_391082.xml#1">P195</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_390782.xml#1">P21</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_390720.xml#1">P1</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_390882.xml#1">P93/P95</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_391314.xml#1">P91</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_390781.xml#1">P92a</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_390826.xml#1">P92b</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/paralipomena/gsa_390050.xml#1">P96</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/faust/2/gsa_390777.xml#1">P97</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/faust/2/gsa_390705.xml#1">P98a</a>,&nbsp;
	<a title="Zum Manuskript" href="${cp}/document/faust/2/gsa_390705.xml#2">P98b</a>

</p>

<#--
<p>
	<span class="font-size-1">Urfaust:</span>&nbsp;
	<a href="${cp}/document/faust/gsa_390028.xml#95" title="Zum Manuskript">Schluss</a>
</p>
-->
