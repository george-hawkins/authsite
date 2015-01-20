package net.betaengine.jettyexample;

import static net.betaengine.jettyexample.util.Util.unchecked;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.betaengine.jettyexample.mybatis.domain.User;
import net.betaengine.jettyexample.mybatis.service.UserService;
import net.betaengine.jettyexample.util.Util;

import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UserManagerServlet extends HttpServlet {
    private final static Logger log = LoggerFactory.getLogger(UserManagerServlet.class);
    
    private final static String PAGE_TEMPLATE = getTemplate("templates/user-manager-template.html");
    private final static String ADMIN_USER = "admin";
    private final static String UNCHANGED_PASSWORD = "-unchanged-";
    private final static String USER_ROW = "<tr><td><input type=\"checkbox\" name=\"id\" value=\"%d\"></td><td class=\"x-username\">%s</td><td class=\"x-fullName\">%s</td><td class=\"x-email\">%s</td></tr>%n";
    
    private final UserService userService;
    
    @Inject
    UserManagerServlet(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().print(createPage());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String operation = request.getParameter("operation");
            
            if (operation != null) {
                performOperation(request, operation);
            }

            response.getWriter().print(createPage());
        } catch (Exception e) {
            log.error("getting user manager page failed", e);
            throw new ServletException(e);
            // Note: in web.xml you can specify an exception handler to nicely handle/format exceptions with <error-page><exception-type>...<location>handler...
        }
    }
    
    private String createPage() throws IOException {
        List<User> users = userService.getAllUsers();
        StringBuilder builder = new StringBuilder();
        
        users.forEach(user -> builder.append(String.format(USER_ROW, user.getId(), user.getUsername(), user.getFullName(), user.getEmail())));
        
        return PAGE_TEMPLATE.replace("USER_ROWS", builder.toString());
    }

    private void performOperation(HttpServletRequest request, String operation) {
        switch (operation) {
        case "create":
            createUser(request);
            break;
        case "modify":
            modifyUser(request);
            break;
        case "delete":
            deleteUser(request);
            break;
        default:
            throw new UserManagerServletException("unknown operation " + operation);
        }
    }
    
    private void createUser(HttpServletRequest request) {
        String fullName = Util.getMandatoryParameter(request, "fullName");
        String email = request.getParameter("email");
        String username = Util.getMandatoryParameter(request, "username");
        String password = Util.getMandatoryParameter(request, "password");
        String confirmPassword = Util.getMandatoryParameter(request, "confirmPassword");

        if (!password.equals(confirmPassword)) {
            throw new UserManagerServletException("passwords do not match");
        }
        
        User user = new User();
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        
        setPassword(user, username, password);
        
        userService.createUser(user);
    }
    
    private void modifyUser(HttpServletRequest request) {
        int id = Integer.parseInt(Util.getMandatoryParameter(request, "id"));
        String fullName = Util.getMandatoryParameter(request, "fullName");
        String email = request.getParameter("email");
        String password = Util.getMandatoryParameter(request, "password");
        String confirmPassword = Util.getMandatoryParameter(request, "confirmPassword");

        if (!password.equals(confirmPassword)) {
            throw new UserManagerServletException("passwords do not match");
        }
        
        User user = userService.getUserById(id);
        
        user.setFullName(fullName);
        user.setEmail(email);
        
        if (!password.equals(UNCHANGED_PASSWORD)) {
            setPassword(user, user.getUsername(), password);
        }
        
        userService.modifyUser(user);
    }
    
    private void setPassword(User user, String username, String password) {
        user.setPassword(Credential.Crypt.crypt(username, password));
    }

    private void deleteUser(HttpServletRequest request) {
        Util.getMandatoryParameterValues(request, "id").stream().map(Integer::valueOf).forEach(this::deleteUser);
    }
    
    private void deleteUser(int id) {
        User user = userService.getUserById(id);
        
        if (user == null) {
            throw new UserManagerServletException("no user with id " + id + " found");
        }
        
        if (user.getUsername().equals(ADMIN_USER)) {
            throw new UserManagerServletException("the admin user is undeletable");
        }
        
        userService.deleteUser(id);
    }
    
    private static String getTemplate(String path) {
        try {
            URL url = Resources.getResource(UserManagerServlet.class, path);
            
            return Resources.toString(url, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            log.error("could not retrieve page template", e);
            throw unchecked(e);
        }
    }
    
    public static class UserManagerServletException extends RuntimeException {
        public UserManagerServletException(String message) { super(message); }
    }
}
