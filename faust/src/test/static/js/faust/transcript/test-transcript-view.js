YUI.add('test-transcript-view', function (Y) {

	var transcriptViewTestSuite = new Y.Test.Suite('TranscriptView Test Suite');

	var constructionTest = new Y.Test.Case({

		name: 'Test for new DiplomaticTranscriptView()',

		transcript: {"text":{"n":941972007,"t":[[0,24256,386]],"d":{},"id":387},"textContent":"Overlapping Inscriptions overlapped standard lineoverlapping lineoverlapped standard lineoverlapping line ","names":{"-2129942746":["http://www.w3.org/XML/1998/namespace","pi"],"-1223563659":["http://www.tei-c.org/ns/1.0","zone"],"1678307402":["http://www.faustedition.net/ns","hand"],"-600381810":["http://www.tei-c.org/ns/1.0","facsimile"],"1987775755":["http://www.tei-c.org/ns/geneticEditions","document"],"-1965677943":["http://www.faustedition.net/ns","vspace"],"-64481874":["http://www.tei-c.org/ns/1.0","handShift"],"-1238942023":["http://www.tei-c.org/ns/1.0","graphic"],"1147210066":["http://www.tei-c.org/ns/geneticEditions","line"],"-1339545664":["http://www.tei-c.org/ns/1.0","hi"],"1339702676":["http://www.tei-c.org/ns/1.0","surface"],"-1337077847":["http://www.tei-c.org/ns/1.0","TEI"]},"annotations":[{"n":-2129942746,"t":[[0,0,387]],"d":{"xml:piData":"RNGSchema=\"https://faustedition.uni-wuerzburg.de/schema/1.3/faust-tei.rng\" type=\"xml\"","xml:piTarget":"oxygen","xml:node":"1"},"id":388},{"n":-1238942023,"t":[[0,0,387]],"d":{"xml:node":"2/4/2","url":"faust://facsimile/test/10"},"id":389},{"n":-600381810,"t":[[0,0,387]],"d":{"xml:node":"4/2"},"id":390},{"n":-64481874,"t":[[0,0,387]],"d":{"new":"#g_bl","xml:node":"1/2/2/2/6/2"},"id":391},{"n":-1339545664,"t":[[0,24,387]],"d":{"rend":"underline","xml:node":"3/2/2/2/6/2"},"id":392},{"n":1147210066,"t":[[0,25,387]],"d":{"rend":"centered","xml:node":"2/2/2/6/2"},"id":393},{"n":-1965677943,"t":[[25,25,387]],"d":{"unit":"lines","quantity":"0.5","xml:node":"4/2/2/6/2"},"id":394},{"n":-1223563659,"t":[[0,25,387]],"d":{"f:bottom-top":"#mainzone","xml:node":"2/2/6/2","f:left":"#mainzone"},"id":395},{"n":1678307402,"t":[[0,25,387]],"d":{"value":"#g_bl"},"id":396},{"n":-64481874,"t":[[25,25,387]],"d":{"new":"#g_bl","xml:node":"1/2/4/2/6/2"},"id":397},{"n":1147210066,"t":[[25,49,387]],"d":{"xml:node":"2/4/2/6/2"},"id":398},{"n":1147210066,"t":[[49,65,387]],"d":{"f:pos":"over","xml:node":"4/4/2/6/2"},"id":399},{"n":1147210066,"t":[[65,89,387]],"d":{"xml:node":"6/4/2/6/2"},"id":400},{"n":1147210066,"t":[[89,105,387]],"d":{"f:pos":"over","xml:node":"8/4/2/6/2"},"id":401},{"n":-1223563659,"t":[[25,105,387]],"d":{"xml:id":"mainzone","type":"main","xml:node":"4/2/6/2"},"id":402},{"n":1339702676,"t":[[0,106,387]],"d":{"xml:node":"2/6/2"},"id":403},{"n":1987775755,"t":[[0,106,387]],"d":{"xml:node":"6/2"},"id":404},{"n":-1337077847,"t":[[0,106,387]],"d":{"xml:node":"2"},"id":405},{"n":1678307402,"t":[[25,106,387]],"d":{"value":"#g_bl"},"id":406}]},

		testConstruction : function() {

			var transcriptView = new Y.FaustTranscript.DiplomaticTranscriptView({
				container: Y.one('#diplomaticTranscript'),
				transcript: this.transcript
			});
			transcriptView.render();


			Y.assert(Y.one('#diplomaticTranscript').one('svg').one('g'),
				'No transcript view was rendered.');
		}
	});

	transcriptViewTestSuite.add(constructionTest);

	Y.mix(Y.namespace("FaustTest"), {
        transcriptViewTestSuite: transcriptViewTestSuite
    });

}, '0.0', {
    requires: ["yui-base", "transcript-view"]
});