//
// This logic expects to find a <li> tags with id 'nav-user-login' and 'nav-user-name'.
// It queries the service at '/userStatus' and creates either a login link or the user's name and a logout link.
//
(function ($) {
    var loginTag = $('#nav-user-login');

    if (loginTag.length != 0) {
        function unhide(tag) {
            tag.removeClass('hidden');
            return tag;
        }

        function setupLoginTag(link, t) {
            unhide(loginTag).find('a').prop('href', link).text(t);
        }

        $.getJSON('/userStatus', function(data) {
            if (data.isLoggedIn) {
                var userNameTag = unhide($('#nav-user-name'));

                if (window.location.pathname === '/userSettings') {
                    userNameTag.addClass('active');
                }

                userNameTag.find('a').text(data.fullName);
                setupLoginTag('/logout', 'Sign out');
            } else {
                var isLoginPage = $('form[action=j_security_check').length != 0;

                if (isLoginPage) {
                    loginTag.addClass('active');
                }

                setupLoginTag('/private/', 'Sign in');
            }
        });
    }
})(jQuery);

// http://stackoverflow.com/a/8764051/245602
function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null;
}
