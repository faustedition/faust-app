<#assign title=message("menu.visualization.style_catalogue") />
<#macro style title height="2em">
	<h3>${title}</h3>
	<div>
	<svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="400px" height="${height}">
		<#nested/>
	</svg>
	</div>
</#macro>

<@faust.page title=title>

	<hr/>
	
	<h2>Schrift</h2>
	
	<div class="yui3-g">
		<div class="yui3-u-1-2">
			<@style "Farbe">
				<text x="1em" y="1.25em">
					<tspan class="fill-0">Farbe #1,</tspan>
					<tspan class="fill-1">Farbe #2,</tspan>
					<tspan class="fill-2">Farbe #3,</tspan>
					<tspan class="fill-3">Farbe #4</tspan>
				</text>
			</@style>		
			<@style title="Art">
				<text x="1em" y="1.25em">
					<tspan class="font-family-0" >serif,</tspan>
					<tspan class="font-family-1" >serifenlos,</tspan>
					<tspan class="font-family-2" >feste Laufw.</tspan>
				</text>
			</@style>
			<@style "Größe">
				<text x="1em" y="1.25em">
					<tspan class="font-size-0">normal,</tspan>
					<tspan class="font-size-1">größer,</tspan>
					<tspan class="font-size-2">kleiner,</tspan>
					<tspan class="font-size-3">Kapitälchen</tspan>
				</text>
			</@style>	
		</div>
		<div class="yui3-u-1-2">
			<@style "Varianten">
				<text x="1em" y="1.25em">
					<tspan class="font-variant-0">Variante #1,</tspan>
					<tspan class="font-variant-1">Variante #2,</tspan>
					<tspan class="font-variant-2">Variante #3,</tspan>
					<tspan class="font-variant-3">Variante #4</tspan>
				</text>
			</@style>

			<h3>Dekoration</h3>
			<p>
				<span class="text-decoration-0">normal,</span>
				<span class="text-decoration-1">unterstrichen,</span>
				<span class="text-decoration-2">durchstrichen,</span>
				<span class="text-decoration-3">überstrichen</span>
			</p>
		</div>
	</div>
	
	<hr/>
	
	<h2>Graphische Elemente</h2>
	
	<div class="yui3-g">
		<div class="yui3-u-1-2">
			<@style title="Linien" height="50px">
				<line x1="10" y1="10" x2="150" y2="20" class="stroke-0"/>
				<line x1="20" y1="20" x2="150" y2="20" class="stroke-1"/>
				<line x1="30" y1="30" x2="150" y2="20" class="stroke-2"/>
				<line x1="40" y1="40" x2="150" y2="20" class="stroke-3"/>
			</@style>
			<@style title="Geschwungene Linien" height="100px">
				<path d="M 10 10 q 20,20 20,0 t 20,0 t 20,0 t 20,0" class="stroke-0" style="fill: #fff" />
				<path d="M 10 30 q 20,20 20,0 t 20,0 t 20,0 t 20,0" class="stroke-1" style="fill: #fff" />
			</@style>
		</div>
		<div class="yui3-u-1-2">
			<@style title="Runde Klammern" height="100px">
				<path d="M 10 40 q 70,-60 140,0 " class="stroke-0" style="fill: #fff" />
				<path d="M 10 40 q 70,-40 140,0 " class="stroke-1" style="fill: #fff" />
				<path d="M 10 40 q 70,-20 140,0 " class="stroke-2" style="fill: #fff" />
				<path d="M 10 40 q 70,0 140,0 " class="stroke-3" style="fill: #fff" />
			</@style>
			<@style title="Geschwungene Klammern" height="100px">
				<path d="M 10 40 q 10,-20 100,-10 q 5,0 10,-10 q -5,0 10,10 q 90,-10 100,10" class="stroke-2" style="fill: #fff" />
				<path d="M 10 60 q 10,-20 100,-10 q 5,0 10,-10 q -5,0 10,10 q 90,-10 100,10" class="stroke-3" style="fill: #fff" />
			</@style>
		</div>
	</div>

	<hr/>
		
	<h2>Positionierung</h2>
	
	<div class="yui3-g">
		<div class="yui3-u-1-2">
			<h3>Interlineare Einfügung</h3>
			
			<canvas id="test-canvas1" width="350" height="120" style="background: #eee">Keine HTML5-Canvas-Unterstützung</canvas>
			<script type="text/javascript">
				var ctx = document.getElementById("test-canvas1").getContext("2d");
				ctx.font = "150% sans-serif";
				ctx.textAlign = "start";
				ctx.fillText("Die Tat!", 40, 60);

				var xOffset = ctx.measureText("Die ").width;
				ctx.strokeStyle = "rgba(96,96,96,1)";
				ctx.beginPath();
				ctx.moveTo(40 + (xOffset - 2), 60);
				ctx.lineTo(40 + (xOffset - 2), 35);
				ctx.lineTo(40 + (xOffset + 4), 35);
				ctx.stroke();
				ctx.font = "100% sans-serif";
				ctx.fillText("Wort", 40 + xOffset + 5, 40);
			</script>

			<h3>Fixierung</h3>
		
			<canvas id="test-canvas3" width="350" height="120" style="background: #eee">Keine HTML5-Canvas-Unterstützung</canvas>
			<script type="text/javascript">
				var ctx3 = document.getElementById("test-canvas3").getContext("2d");
				ctx3.font = "20px serif";
				ctx3.textAlign = "start";
				ctx3.fillStyle = "rgba(128,128,128,1)";
				ctx3.fillText("Im Anfang war ...", 80, 40);
				ctx3.fillStyle = "rgba(0,0,0,0.75)";
				ctx3.fillText("Im Anfang war ...", 82, 38);
			</script>
		</div>
		<div class="yui3-u-1-2">
			<h3>Drehung</h3>
		
			<canvas id="test-canvas2" width="350" height="120" style="background: #eee">Keine HTML5-Canvas-Unterstützung</canvas>
			<script type="text/javascript">
				var ctx2 = document.getElementById("test-canvas2").getContext("2d");
				ctx2.font = "15px serif";
				ctx2.textAlign = "start";
				ctx2.rotate(0.75);
				ctx2.fillText("Im Anfang war ...", 30, 10);
			</script>
		</div>
	</div>		
</@faust.page>