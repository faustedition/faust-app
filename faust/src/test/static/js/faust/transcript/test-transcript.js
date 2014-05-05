YUI.add('test-transcript', function (Y) {

	var transcriptTestSuite = new Y.Test.Suite('Transcript Test Suite');


	var aboutEqual = function (a, b) {
		var delta = 1.0;
		return Math.abs (a - b) < delta;
	}

	var testX = 70;
	var testY = 50;

	var coordTest = new Y.Test.Case({
		name: 'Coord Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'should position elements. under test: setCoord(), getCoord()': function() {

			Y.FaustTest.marker(testX,testY);

			var vc1 = new Y.FaustTranscript.Text('TEST1', {});
			vc1.svgCont = this.svgCanvas;
			vc1.render();

			var vc2 = new Y.FaustTranscript.Text('TEST2', {});
			vc2.svgCont = this.svgCanvas;
			vc2.render();

			var vc3 = new Y.FaustTranscript.Text('TEST3', {});
			vc3.svgCont = this.svgCanvas;
			vc3.rotation = 90;
			vc3.render();

			vc1.setCoord (20, 0);
			vc1.setCoord(30, 90);

			var x1 = vc1.getCoord(0);

			vc2.setCoord(testX, 0);
			vc2.setCoord(testY, 90);

			var x2 = vc2.getCoord(0);
			var y2 = vc2.getCoord(90);


			vc3.setCoord(testX, 0);
			vc3.setCoord(testY, 90);

			var x3 = vc3.getCoord(0);
			var y3 = vc3.getCoord(90);

			Y.assert (x1 < x2, 'TEST2 should have higher coordinates than TEST1.');
			Y.assert (aboutEqual(x2, testX), 'TEST2 should have the specified coordinates');
			Y.assert (aboutEqual(y2, testY), 'TEST2 should have the specified coordinates');
			Y.assert (aboutEqual(x2, x3), 'TEST3 should have the same coordinates as TEST2.');
			Y.assert (aboutEqual(y2, y3), 'TEST3 should have the same coordinates as TEST2.');

		}
	});

	transcriptTestSuite.add(coordTest);

	var zoneCoordTest = new Y.Test.Case({
		name: 'Zone Coord Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'should position elements in a rotated zone. under test: setCoord(), getCoord()': function() {


			Y.FaustTest.marker (testX, testY);

			var surface = new Y.FaustTranscript.Surface();
			surface.svgCont = this.svgCanvas;


			var zone1 = new Y.FaustTranscript.Zone();
			surface.add(zone1);
			zone1.rotation = 90;

			var vc2 = new Y.FaustTranscript.Text('TEST2', {});
			zone1.add(vc2);

			var vc3 = new Y.FaustTranscript.Text('TEST3', {});
			zone1.add(vc3);

			var zone2 = new Y.FaustTranscript.Zone();
			surface.add(zone2);
			zone2.rotation = 45;

			var vc4 = new Y.FaustTranscript.Text('TEST4', {});
			zone2.add(vc4);

			vc3.rotation = 90;
			vc4.rotation = 30;

			surface.render();

			vc2.setCoord(testX, 0);
			vc2.setCoord(testY, 90);

			var x2 = vc2.getCoord(0);
			var y2 = vc2.getCoord(90);

			vc3.setCoord(testX, 0);
			vc3.setCoord(testY, 90);

			var x3 = vc3.getCoord(0);
			var y3 = vc3.getCoord(90);

			zone2.setCoord(testX, 0);
			zone2.setCoord(testY, 90);

			var x4 = vc4.getCoord(0);
			var y4 = vc4.getCoord(90);

			Y.assert (aboutEqual(x2, testX), 'TEST2 should have the specified coordinates');
			Y.assert (aboutEqual(y2, testY), 'TEST2 should have the specified coordinates');
			Y.assert (aboutEqual(x2, x3), 'TEST3 should have the same coordinates as TEST2.');
			Y.assert (aboutEqual(y2, y3), 'TEST3 should have the same coordinates as TEST2.');
			Y.assert (x4 > x2, 'TEST4 should be right of TEST2.');
			Y.assert (y4 > y2, 'TEST4 should be below TEST2.');
		}
	});

	transcriptTestSuite.add(zoneCoordTest);

	var alignTest = new Y.Test.Case({
		name: 'Align Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'should align elements. under test: Align': function() {
			var vc1 = new Y.FaustTranscript.Text('ANCHOR', {});
			vc1.rotation = 10;
			vc1.svgCont = this.svgCanvas;
			vc1.render();


			var vc2 = new Y.FaustTranscript.Text('ALIGNED', {});
			vc2.svgCont = this.svgCanvas;
			vc2.render();



			vc1.setCoord(40, 70);

			var x1 = vc1.getCoord(0);

			vc2.setAlign("hAlign", new Y.FaustTranscript.Align(vc2, vc1, 0, 0, 1, 0));
			vc2.setAlign("vAlign", new Y.FaustTranscript.Align(vc2, vc1, 90, 1, 1, 0));

			vc2.layout();

			var x2 = vc2.getCoord(0);

			Y.assert (x1 < x2, 'ALIGN should have higher coordinates than ANCHOR.');
		}
	});

	Y.Test.Runner.add(alignTest);


	var componentAlignTest = new Y.Test.Case({
		name: 'Coord Align Test',
		setUp: Y.FaustTest.svgCanvasSetUp,
		'should align view components. under test: Align, ViewComponent, Line': function() {

			var surface = new Y.FaustTranscript.Surface();
			surface.svgCont = this.svgCanvas;


			var zone1 = new Y.FaustTranscript.Zone();
			surface.add(zone1);
			zone1.rotation = 90;

			var line1 = new Y.FaustTranscript.Line({});
			zone1.add(line1);

			var text1 = new Y.FaustTranscript.Text('LINE1', {});
			line1.add(text1);

			var line2 = new Y.FaustTranscript.Line({});
			zone1.add(line2);

			var text2 = new Y.FaustTranscript.Text('LINE2', {});
			line2.add(text2);

			var zone2 = new Y.FaustTranscript.Zone();
			surface.add(zone2);
			zone2.rotation = 180;

			var line3 = new Y.FaustTranscript.Line({});
			zone2.add(line3);

			var text3 = new Y.FaustTranscript.Text('LINE3', {});
			line3.add(text3);

			zone2.setAlign("hAlign", new Y.FaustTranscript.Align(zone2, zone1, 0, 0, 0, 100));
			zone2.setAlign("vAlign", new Y.FaustTranscript.Align(zone2, zone1, 90, 0, 1, 100));

			surface.render();
			surface.setCoord(60, 0);
			surface.setCoord(60, 90);

			surface.layout();

			var line1x = line1.getCoord(0);
			var line2x = line2.getCoord(0);
			var line2bottom = line2.getCoord(90) + line2.getExt(90);
			var line3y = line3.getCoord(90);

			Y.assert (line1x > line2x, 'LINE2 should be on the left.');
			Y.assert (line2bottom <= line3y, 'LINE3 should be on the bottom.');

		}
	});

	transcriptTestSuite.add(componentAlignTest);

	Y.mix(Y.namespace("FaustTest"), {
        transcriptTestSuite: transcriptTestSuite
    });



}, '0.0', {
    requires: ["yui-base", "transcript", "test-utils"]
});