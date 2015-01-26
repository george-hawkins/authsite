//
// This logic expects to find an <li> tag with id 'nav-user-status'.
// It queries the service at '/userStatus' and creates either a login link or the user's name and a logout link.
//
(function ($) {
    var loginTag = $('#nav-user-login');

    function unhide(tag) {
        tag.removeClass('hidden');
        return tag;
    }

    function setupLoginTag(link, t) {
        unhide(loginTag).find('a').prop('href', link).text(t);
    }

    $.getJSON('/userStatus', function(data) {
        if (data.isLoggedIn) {
            unhide($('#nav-user-name')).find('a').text(data.fullName);
            setupLoginTag('/logout', 'Sign out');
        } else {
            var isLoginPage = $('form[action=j_security_check').length != 0;

            if (isLoginPage) {
                loginTag.addClass('active');
            }

            setupLoginTag('/login.html', 'Sign in');
        }
    });
})(jQuery);
