var page = require('webpage').create(),
    system = require('system'),
    address, output, size;

if (system.args.length < 3 || system.args.length > 5) {
    console.log('Usage: rasterize.js URL filename [paperwidth*paperheight|paperformat] [zoom]');
    console.log('  paper (pdf output) examples: "5in*7.5in", "10cm*20cm", "A4", "Letter"');
    phantom.exit(1);
} else {
    address = system.args[1];
    output = system.args[2];
    page.viewportSize = { width: 1024, height: 860 };
    if (system.args.length > 3 && system.args[2].substr(-4) === ".pdf") {
        size = system.args[3].split('*');
//        page.paperSize = size.length === 2 ? { width: size[0], height: size[1], margin: '0px' }
//                                          : { format: system.args[3], orientation: 'portrait', margin: '1cm' };
	
    }


    page.zoomFactor = 3;

    page.open(address, function (status) {
        if (status !== 'success') {
            console.log('Unable to load the address!');
            phantom.exit();
        } else {
//            window.setTimeout(function () {
				
				// var out_width = String(page.evaluate(function(){
				// 	return document.getElementsByClassName('diplomatic')[0].clientWidth;
				// })) + 'px';				
				// var out_height = String(page.evaluate(function(){
				// 	return document.getElementsByClassName('diplomatic')[0].clientHeight;
				// })) + 'px';				
				// page.paperSize = {width: out_width, height: out_height, margin: '0px'}
				// console.log(out_width);
				// console.log(out_height);

			// wait for layout to complete

			function isLayoutOngoing () {
				return document.getElementsByClassName('transcript-layout-complete').length < 1;
			}

			function hasPageLoaded () {
				return document.getElementsByClassName('diplomatic').length > 0;
			}
			
			function takeScreenshot() {
                    page.render(output);
                    phantom.exit();				
			}

			function waitAndCheckLayoutStatus() {
				if (page.evaluate(isLayoutOngoing)) {
					console.log(' .');
					setTimeout(waitAndCheckLayoutStatus, 500);
				} else {
					console.log(' layout finished');
					takeScreenshot();
				}							
			}

			console.log(' waiting for layout to finish');
			if (page.evaluate(hasPageLoaded))
				waitAndCheckLayoutStatus();
			else
				takeScreenshot();
		}
    });
}
