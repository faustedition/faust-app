<#assign title><@spring.message "menu.visualization.style_catalogue" /></#assign>
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
		<div class="yui3-u first">
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
		<div class="yui3-u">
			<@style "Varianten">
				<text x="1em" y="1.25em">
					<tspan class="font-variant-0">Variante #1,</tspan>
					<tspan class="font-variant-1">Variante #2,</tspan>
					<tspan class="font-variant-2">Variante #3,</tspan>
					<tspan class="font-variant-3">Variante #4</tspan>
				</text>
			</@style>
			<@style title="Dekoration">
				<text x="1em" y="1.25em">
					<tspan class="text-decoration-0">normal,</tspan>
					<tspan class="text-decoration-1">unterstrichen,</tspan>
					<tspan class="text-decoration-2">durchstrichen,</tspan>
					<tspan class="text-decoration-3">überstrichen</tspan>
				</text>
			</@style>
		</div>
	</div>
	
	<hr/>
	
	<h2>Graphische Elemente</h2>
	
	<div class="yui3-g">
		<div class="yui3-u first">
			<@style title="Linien" height="50">
				<line x1="10" y1="10" x2="150" y2="20" class="stroke-0"/>
				<line x1="20" y1="20" x2="150" y2="20" class="stroke-1"/>
				<line x1="30" y1="30" x2="150" y2="20" class="stroke-2"/>
				<line x1="40" y1="40" x2="150" y2="20" class="stroke-3"/>
			</@style>
			<@style title="Geschwungene Linien" height="100">
				<path d="M 10 10 q 20,20 20,0 t 20,0 t 20,0 t 20,0" class="stroke-0" style="fill: #fff" />
				<path d="M 10 30 q 20,20 20,0 t 20,0 t 20,0 t 20,0" class="stroke-1" style="fill: #fff" />
			</@style>
		</div>
		<div class="yui3-u">
			<@style title="Runde Klammern" height="50">
				<path d="M 10 40 q 70,-60 140,0 " class="stroke-0" style="fill: #fff" />
				<path d="M 10 40 q 70,-40 140,0 " class="stroke-1" style="fill: #fff" />
				<path d="M 10 40 q 70,-20 140,0 " class="stroke-2" style="fill: #fff" />
				<path d="M 10 40 q 70,0 140,0 " class="stroke-3" style="fill: #fff" />
			</@style>
			<@style title="Geschwungene Klammern" height="100">
				<path d="M 10 40 q 10,-20 100,-10 q 5,0 10,-10 q -5,0 10,10 q 90,-10 100,10" class="stroke-2" style="fill: #fff" />
				<path d="M 10 60 q 10,-20 100,-10 q 5,0 10,-10 q -5,0 10,10 q 90,-10 100,10" class="stroke-3" style="fill: #fff" />
			</@style>
		</div>
	</div>
	
</@faust.page>