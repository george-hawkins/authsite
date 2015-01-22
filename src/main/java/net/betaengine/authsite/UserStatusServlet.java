package net.betaengine.authsite;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.betaengine.authsite.mybatis.service.UserService;
import net.betaengine.authsite.util.Util;

import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UserStatusServlet extends HttpServlet {
    private final UserService userService;
    
    @Inject
    UserStatusServlet(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getRemoteUser();
        boolean isLoggedIn = username != null;
        String fullName = isLoggedIn ? getFullName(request, username) : null;
        
        response.setContentType(MediaType.JSON_UTF_8.toString());
        
        response.getWriter().print(Util.generateJsonAsString(generator -> {
            generator.writeBooleanField("isLoggedIn", isLoggedIn);
            generator.writeStringField("username", username);
            generator.writeStringField("fullName", fullName);
        }));
    }
    
    private String getFullName(HttpServletRequest request, String username) {
        return userService.getUserByUsername(username).getFullName();
//        HttpSession session = request.getSession();
//        User user = (User)session.getAttribute("user");
//        
//        if (user == null) {
//            user = userService.getUserByUsername(username);
//            session.setAttribute("user", user);
//        }
//
//        return user.getFullName();
    }
}