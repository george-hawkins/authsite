package net.betaengine.authsite;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import net.betaengine.authsite.mybatis.domain.User;
import net.betaengine.authsite.mybatis.service.UserService;
import net.betaengine.authsite.util.Util;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UserManagerServlet extends AbstractUserServlet {
    private final static String ADMIN_USER = "admin";
    private final static String USER_ROW = "<tr><td><input type=\"checkbox\" name=\"id\" value=\"%d\"></td><td class=\"x-username\">%s</td><td class=\"x-fullName\">%s</td><td class=\"x-email\">%s</td></tr>%n";

    private String pageTemplate;
    
    @Inject
    UserManagerServlet(UserService userService) {
        super(userService);
    }
    
    @Override
    public void init() {
        pageTemplate = getTemplate("user-manager-template.html");
    }
    
    @Override
    protected String createPage(HttpServletRequest request) throws IOException {
        List<User> users = userService.getAllUsers();
        StringBuilder builder = new StringBuilder();
        
        users.forEach(user -> builder.append(String.format(USER_ROW,
                user.getId(), escape(user.getUsername()), escape(user.getFullName()), escape(user.getEmail()))));
        
        return pageTemplate.replace("USER_ROWS", builder.toString());
    }

    @Override
    protected Map<String, Consumer<HttpServletRequest>> createOperations() {
        return ImmutableMap.of(
                "create", this::createUser,
                "modify", this::modifyUser,
                "delete", this::deleteUser);
    }
    
    private void createUser(HttpServletRequest request) {
        String username = Util.getMandatoryParameter(request, "username");
        User user = new User();
        
        user.setUsername(username);
        setupUser(request, user);
        
        userService.createUser(user);
    }
    
    private void modifyUser(HttpServletRequest request) {
        int id = Integer.parseInt(Util.getMandatoryParameter(request, "id"));
        User user = userService.getUserById(id);
        
        modifyUser(request, user);
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
}
