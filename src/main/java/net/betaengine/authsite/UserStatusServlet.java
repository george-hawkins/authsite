package net.betaengine.authsite;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.betaengine.authsite.mybatis.domain.User;
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
        Optional<User> user = SessionUser.getUser(request, userService);
        
        if (user.isPresent()) {
            writeRespons(response, true, user.get().getUsername(), user.get().getFullName());
        } else {
            writeRespons(response, false, null, null);
        }
    }
    
    private void writeRespons(HttpServletResponse response, boolean isLoggedIn, String username, String fullName) throws IOException {
        response.setContentType(MediaType.JSON_UTF_8.toString());
        
        response.getWriter().print(Util.generateJsonAsString(generator -> {
            generator.writeBooleanField("isLoggedIn", isLoggedIn);
            generator.writeStringField("username", username);
            generator.writeStringField("fullName", fullName);
        }));
    }
}