package net.betaengine.authsite;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.betaengine.authsite.mybatis.domain.User;
import net.betaengine.authsite.mybatis.service.UserService;

public class SessionUser {
    private final static String USER_ATTRIBUTE = "user";
    
    public static Optional<User> getUser(HttpServletRequest request, UserService userService) {
        String username = request.getRemoteUser();
        
        if (username == null) {
            return Optional.empty();
        }
        
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute(USER_ATTRIBUTE);
        
        if (user == null) {
            user = userService.getUserByUsername(username);
            session.setAttribute(USER_ATTRIBUTE, user);
        }

        return Optional.of(user);
    }
    
    public static void clear(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_ATTRIBUTE);
    }
}
