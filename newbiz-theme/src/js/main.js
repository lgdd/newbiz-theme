$(document).ready(function () {
    // scroll shadow
    $(window).scroll(function () {
        var $header = $("#header");
        // scroll shadow
        if ($header.offset().top > 100) {
            $header.addClass("scroll-shadow");
        } else {
            $header.removeClass("scroll-shadow");
        }
    });

    // Back to top button
    $(window).scroll(function () {
        if ($(this).scrollTop() > 100) {
            $('.back-to-top').fadeIn('slow');
        } else {
            $('.back-to-top').fadeOut('slow');
        }
    });
    $('.back-to-top').click(function () {
        $('html, body').animate({scrollTop: 0}, 1500, 'easeInOutExpo');
        return false;
    });

    // Initiate the wowjs animation library
    new WOW().init();

});
