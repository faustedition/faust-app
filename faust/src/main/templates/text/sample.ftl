<@faust.page title=message("text.sample")>

<script type="text/javascript">
	Faust.YUI().use('event', 'node', 'base', 'io', 'json', 'interaction', function(Y) {
		var lines = Y.all('.ann-l');

		function lineNumberForLine(line) {
			return parseInt(
					line.getAttribute('class')
							.split(' ')[1]
							.slice('linenum-'.length)
			)
		}

		var lineNumbers = [];
		lines.each(function(line){
			lineNumbers.push(lineNumberForLine(line));
		});

		function augmentText(id, o, args) {
			var variants = Y.JSON.parse(o.responseText);
	        lines.each(function(line) {
		        var variantsForLine = variants[lineNumberForLine(line)];
		        var numOfVariantsForLine = variantsForLine.length;
		        var c = 255 - (Math.min(numOfVariantsForLine - 1, 10) * 5);
		        line.setStyle('backgroundColor', 'rgb(' + c + ',' + c + ',' + c + ')');


		        line.on('mouseenter', function(e) {
			        Y.fire('faust:mouseover-info', { info: numOfVariantsForLine + ' variants.', mouseEvent: e });
		        });

		        line.on('mouseleave', function(e) { Y.fire('faust:mouseover-info-hide', {})});

		        line.on('click', function(e) {
			        Y.all('.variant').remove(true);

			        line.ancestor().insert('<div class="variant"><br/></div>', line);
			        Y.each(variantsForLine, function(variant) {
				        line.ancestor().insert(
						        '<div class="variant">' +
								        variant.variantText +
								        ' <a href="' + cp + variant.source.slice('faust://xml'.length) + '">'
								        + variant.name + '</a></div>', line);
			        })
		        });

	        });
		}


		var ioConfig = {
			method: 'POST',
			data: Y.JSON.stringify(lineNumbers),
			headers: {
				'Content-Type': 'application/json'
			},
			on: {
				success: augmentText
			}
		};

		Y.io(cp + '/genesis/variants', ioConfig);

	});
</script>


	<div class="yui3-g" style="margin-top: 2em">
	<div class="yui3-u-1-5"></div>

	<div class="yui3-u-3-5">


	<div class="text-center color-1">Vor dem Palaste des Menelas zu Sparta.</div>

	<div class="text-center color-1">Helena tritt auf und Chor gefangener Trojanerinnen. Panthalis Chorführerin.</div>


	<div class="text-center color-1">Helena.</div>

	<div class="ann-l linenum-8488">Bewundert viel und viel gescholten Helena</div>

	<div class="ann-l linenum-8489">Vom Strande komm' ich wo wir erst gelandet sind,</div>

	<div class="ann-l linenum-8490">Noch immer trunken von des Gewoges regsamem</div>

	<div class="ann-l linenum-8491">Geschaukel, das vom phrygischen Blachgefild uns her</div>

	<div class="ann-l linenum-8492">Auf sträubig-hohem Rücken, durch Poseidons Gunst</div>

	<div class="ann-l linenum-8493">Und Euros Kraft, in vaterländische Buchten trug.</div>

	<div class="ann-l linenum-8494">Dort unten freuet nun der König Menelas</div>

	<div class="ann-l linenum-8495">Der Rückkehr sammt den tapfersten seiner Krieger sich.</div>

	<div class="ann-l linenum-8496">Du aber heiße mich willkommen, hohes Haus,</div>

	<div class="ann-l linenum-8497">Das Tyndareos, mein Vater, nah dem Hange sich</div>

	<div class="ann-l linenum-8498">Von Pallas Hügel wiederkehrend aufgebaut</div>

	<div class="ann-l linenum-8499">Und, als ich hier mit Klytämnestren schwesterlich,</div>

	<div class="ann-l linenum-8500">Mit Castor auch und Pollux fröhlich spielend wuchs,</div>

	<div class="ann-l linenum-8501">Vor allen Häusern Sparta's herrlich ausgeschmückt.</div>

	<div class="ann-l linenum-8502">Gegrüßet seid mir, der ehrnen Pforte Flügel ihr!</div>

	<div class="ann-l linenum-8503">Durch euer gastlich ladendes Weiteröffnen einst</div>

	<div class="ann-l linenum-8504">Geschah's daß mir, erwählt aus vielen, Menelas</div>

	<div class="ann-l linenum-8505">In Bräutigams-Gestalt entgegen leuchtete.</div>


	<div class="ann-l linenum-8506">Eröffnet mir sie wieder, daß ich ein Eilgebot</div>

	<div class="ann-l linenum-8507">Des Königs treu erfülle, wie der Gattin ziemt.</div>

	<div class="ann-l linenum-8508">Laßt mich hinein! und alles bleibe hinter mir,</div>

	<div class="ann-l linenum-8509">Was mich umstürmte bis hieher, verhängnißvoll.</div>

	<div class="ann-l linenum-8510">Denn seit ich diese Schwelle sorgenlos verließ,</div>

	<div class="ann-l linenum-8511">Cytherens Tempel besuchend, heiliger Pflicht gemäß,</div>

	<div class="ann-l linenum-8512">Mich aber dort ein Räuber griff, der phrygische,</div>

	<div class="ann-l linenum-8513">Ist viel geschehen, was die Menschen weit und breit</div>

	<div class="ann-l linenum-8514">So gern erzählen, aber der nicht gerne hört</div>

	<div class="ann-l linenum-8515">Von dem die Sage wachsend sich zum Mährchen spann.</div>



	<div class="text-center color-1">Chor.</div>

	<div class="ann-l linenum-8516">Verschmähe nicht, o herrliche Frau,</div>

	<div class="ann-l linenum-8517">Des höchsten Gutes Ehrenbesitz!</div>

	<div class="ann-l linenum-8518">Denn das größte Glück ist dir einzig beschert,</div>

	<div class="ann-l linenum-8519">Der Schönheit Ruhm der vor allen sich hebt.</div>

	<div class="ann-l linenum-8520">Dem Helden tönt sein Name voran,</div>

	<div class="ann-l linenum-8521">Drum schreitet er stolz,</div>

	<div class="ann-l linenum-8522">Doch beugt sogleich hartnäckigster Mann</div>

	<div class="ann-l linenum-8523">Vor der allbezwingenden Schöne den Sinn.</div>


	</div>
	</div>

</@faust.page>