package net.betaengine.authsite;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.betaengine.authsite.mybatis.domain.User;
import net.betaengine.authsite.mybatis.service.UserService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UserSettingsServlet extends AbstractUserServlet {
    private final static String PAGE_TEMPLATE = getTemplate("templates/user-settings-template.html");
    
    @Inject
    UserSettingsServlet(UserService userService) {
        super(userService);
    }
    
    @Override
    protected String createPage(HttpServletRequest request) throws IOException {
        User user = getUser(request);
        
        return PAGE_TEMPLATE
                .replace("FULL_NAME", escape(user.getFullName()))
                .replace("EMAIL", escape(user.getEmail()));
    }

    @Override
    protected boolean performOperation(HttpServletRequest request, String operation) {
        if (operation.equals("modify")) {
            modifyUser(request, getUser(request));
            return true;
        } else {
            return false;
        }
    }
    
    private User getUser(HttpServletRequest request) {
        return userService.getUserByUsername(request.getRemoteUser());
    }
}
