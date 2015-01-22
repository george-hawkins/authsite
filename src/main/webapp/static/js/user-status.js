//
// This logic expects to find an <li> tag with id 'nav-user-status'.
// It queries the service at '/userStatus' and creates either a login link or the user's name and a logout link.
//
(function ($) {
    var userTag = $('#nav-user-status');

    function setupUserTag(link, t) {
        $('<a/>').prop('href', link).text(t).appendTo(userTag);
    }

    $.getJSON('/userStatus', function(data) {
        if (data.isLoggedIn) {
            setupUserTag('/logout', 'Sign out');
            $('<li/>').addClass('navbar-text').text(data.fullName).insertBefore(userTag);
        } else {
            setupUserTag('/login.html', 'Sign in');
        }
        userTag.removeClass('hidden');
    });
})(jQuery);
