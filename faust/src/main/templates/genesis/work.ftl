<#assign header>
    <script type="text/javascript">
    		var Y = Faust.YUI().use('node', 'event',
						function(Y) {
							Y.on('domready', function() {
							
								var lines = { 1: {start: 4613, end: 4727, title: 'Anmutige Gegend'},
									    	  2: {start: 4728, end: 5064, title: 'Saal des Thrones'},
										      3: {start: 5065, end: 5986, title: 'Weitläufiger Saal mit Nebengemächern'},
										      4: {start: 5987, end: 6172, title: 'Lustgarten'},
										      5: {start: 6173, end: 6306, title: 'Finstere Galerie'},
										      6: {start: 6307, end: 6376, title: 'Hell erleuchtete Säle'},
										      7: {start: 6377, end: 6565, title: 'Rittersaal'},
										
										      8:  {start: 6566, end: 6818, title: 'Hochgewölbtes, enges gotisches Zimmer'},
										      9:  {start: 6819, end: 7004, title: 'Laboratorium'},
   										      10: {start: 7005, end: 7248, title: 'Pharsalische Felder'},
										      11: {start: 7249, end: 7494, title: 'Peneios'},
										      12: {start: 7495, end: 8033, title: 'Am oberen Peneios'},
										      13: {start: 8034, end: 8274, title: 'Felsbuchten des ägäischen Meers'},
										      14: {start: 8275, end: 8487, title: 'Telcinen von Rhodos'},
										      
										      15: {start: 8488, end: 9126, title: 'Vor dem Palaste des Menelas zu Sparta'},
										      16: {start: 9127, end: 9573, title: 'Innerer Burghof'},
										      17: {start: 9574, end: 10038, title: '[Arkadien]'},
										      
										      18: {start: 10039, end: 10344, title: 'Hochgebirg'},
										      19: {start: 10345, end: 10782, title: 'Auf dem Vorgebirg'},
										      20: {start: 10783, end: 11042, title: 'Des Gegenkaisers Zelt'},

										      21: {start: 11043, end: 11142, title: 'Offene Gegend'},
										      22: {start: 11143, end: 11287, title: 'Palast'},
										      23: {start: 11288, end: 11383, title: 'Tiefe Nacht'},
										      24: {start: 11384, end: 11510, title: 'Mitternacht'},
										      25: {start: 11511, end: 11603, title: 'Großer Vorhof des Palasts'},
										      26: {start: 11604, end: 11843, title: 'Grablegung'},										      
										      27: {start: 11844, end: 12111, title: 'Bergschluchten'}};
								
							
								emb = Y.one('.emb');
								var svgdoc = emb.getDOMNode().getSVGDocument();
								scene_uis = svgdoc.getElementsByClassName('scene_ui');
								ynodes = Y.Node.all(scene_uis);
								
								for (var i=0; i < 27; i++) {
									(function (){
										var line = lines[i+1];
										var start = line.start;
										var end = line.end;											
										var element = ynodes.item(i);																							
										element.on('click', function(e){
											window.location.href = '${cp}/genesis/app/#/' + start + '/' + end;
										});
										var domElement = element.getDOMNode();
										title = domElement.ownerDocument.createElementNS("http://www.w3.org/2000/svg", "title");
										title.appendChild(domElement.ownerDocument.createTextNode(line.title));
										element.insert(title, 0);								
									})();
								}
							});
						});
    </script>
</#assign>

<@faust.page title=message("genesis.work") header=header>
	<div class="yui3-g">
		<div class="yui3-u-2-3">
			<p style="margin-right: 1em">
			<object class="emb" width="100%" data="${cp}/static/svg/work_genesis_interactive.svg" type="image/svg+xml"></object>
			</p>
		</div>
		<div class="yui3-u-1-3">
		<h2>Werkgenese.</h2>				
		<p> Goethe hat beinahe die ganze Zeit seines Lebens am Werkprojekt Faust gearbeitet. 
		Die Ergebnisse der frühesten Beschäftigung mit dem Stoff liegen im Dunkeln; den größten Teil des im Jahr 1776 erreichten Stands der Arbeit 
		überliefert eine Abschrift Louise von Göchhausens: der traditionell so genannte „Urfaust“. Als erster Druck erschien 1790 „Faust. Ein Fragment“ 
		in der ersten von Goethe selbst besorgten, bei Göschen erschienenen Sammelausgabe seiner Werke. 
		Vermutlich im Frühjahr 1800 fiel die Entscheidung zugunsten einer Teilung des Werks, noch im selben Jahr 
		entstand der Anfang der Helena-Dichtung. 1808 erschien „Faust. Eine Tragödie“, der Faust I, in der ersten bei Cotta erschienenen Gesamtausgabe. ...
		</p>
			<p style="margin-right: 1em; border: 1px solid black; padding: 0.3em;">
			<strong><u>Legende</u></strong><br/>
			<strong>Szenen</strong> sind als Spalten von links nach rechts angeordnet, 
			<strong>Jahre</strong> von unten nach oben. 
			Grau zeigt die <strong>begonnene Arbeit</strong> an einer Szene an, 
			Schwarz den <strong>Abschluss der Arbeit</strong>.
			</p>
		</div>		
				
	</div>
</@faust.page>
