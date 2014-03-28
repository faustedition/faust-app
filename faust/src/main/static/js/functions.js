$( document ).ready(function() {
	$('body').addClass('js');
	
	/* *********** Kleiner Header beim Scrollen */
	var headerInnerHeight = $('header').height();
	var headerOuterHeight = $('header').outerHeight();
	$(document).scroll(function() {
		if(window.pageYOffset > headerInnerHeight) {
			$('#content').css('padding-top', headerOuterHeight);
			if ( !$('header').hasClass('compressed') ) {
				$('.pure-submenu').hide();
				$('header').hide().addClass('compressed').fadeTo('fast',1);
			}
		}
		else {
			$('#content').removeAttr('style');
			$('.pure-submenu').show();
			$('header').removeClass('compressed');
		}
	});
	
	/* *********** Logo-Fallback ohne SVG */
	$('.pure-filter').hover(
		function () {
			$(this).addClass('pure-filter-active').children('.pure-button').addClass('pure-button-active');
		},
		function () {
			$(this).removeClass('pure-filter-active').children('.pure-button').removeClass('pure-button-active');
		}
	);
	
	/* *********** Logo-Fallback ohne SVG */
	if ( $('body').hasClass('nosvg') ) {
		$('img').each( function () { $(this).attr('src',
		$(this).attr('src').replace(/.svg/g, '.png')); });
	}
});