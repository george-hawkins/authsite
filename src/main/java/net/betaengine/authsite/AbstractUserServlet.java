package net.betaengine.authsite;

import static net.betaengine.authsite.util.Util.unchecked;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.betaengine.authsite.mybatis.domain.User;
import net.betaengine.authsite.mybatis.service.UserService;
import net.betaengine.authsite.util.Util;

import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.Resources;

@SuppressWarnings("serial")
public abstract class AbstractUserServlet extends HttpServlet {
    private final static Logger log = LoggerFactory.getLogger(AbstractUserServlet.class);
    
    private final static String UNCHANGED_PASSWORD = "-unchanged-";
    
    protected final UserService userService;
    private final Map<String, Consumer<HttpServletRequest>> operations = createOperations();
    
    protected AbstractUserServlet(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().print(createPage(request));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String operation = request.getParameter("operation");
            
            if (operation == null) {
                throw new UserManagerServletException("no operation specified");
            } else if (!operations.containsKey(operation)) {
                throw new UserManagerServletException("no handler for " + operation);
            }
            
            operations.get(operation).accept(request);

            response.sendRedirect(request.getRequestURI() + "?success=true");
        } catch (Exception e) {
            log.error("getting user manager page failed", e);
            throw new ServletException(e);
            // Note: in web.xml you can specify an exception handler to nicely handle/format exceptions with <error-page><exception-type>...<location>handler...
        }
    }
    
    protected abstract String createPage(HttpServletRequest request) throws IOException;
    
    protected String escape(String s) {
        return s != null ? HtmlEscapers.htmlEscaper().escape(s) : "";
    }

    protected abstract Map<String, Consumer<HttpServletRequest>> createOperations();
    
    protected void setupUser(HttpServletRequest request, User user) {
        assert user.getUsername() != null;
        
        String fullName = Util.getMandatoryParameter(request, "fullName");
        Optional<String> email = Util.getOptionalParameter(request, "email");
        String password = Util.getMandatoryParameter(request, "password");
        String confirmPassword = Util.getMandatoryParameter(request, "confirmPassword");

        if (!password.equals(confirmPassword)) {
            throw new UserManagerServletException("passwords do not match");
        }
        
        user.setFullName(fullName);
        user.setEmail(email.orElse(null));

        if (password.equals(UNCHANGED_PASSWORD)) {
            // If there's no existing password, i.e. the user is being created, and
            // the new password is UNCHANGED_PASSWORD then the user is being silly...
            if (user.getPassword() == null) {
                throw new UserManagerServletException("invalid password");
            }
        } else {
            setPassword(user, user.getUsername(), password);
        }
    }
    
    protected void modifyUser(HttpServletRequest request, User user) {
        setupUser(request, user);
        
        userService.modifyUser(user);
    }
    
    protected void setPassword(User user, String username, String password) {
        user.setPassword(Credential.Crypt.crypt(username, password));
    }
    
    protected String getTemplate(String path) {
        try {
            URL url = getServletContext().getResource("/WEB-INF/templates/" + path);
            
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
