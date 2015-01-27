package net.betaengine.authsite;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import net.betaengine.authsite.mybatis.domain.User;
import net.betaengine.authsite.mybatis.service.UserService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class UserSettingsServlet extends AbstractUserServlet {
    private String pageTemplate;
    
    @Inject
    UserSettingsServlet(UserService userService) {
        super(userService);
    }
    
    @Override
    public void init() {
        pageTemplate = getTemplate("user-settings.html");
    }
    
    @Override
    protected String createPage(HttpServletRequest request) throws IOException {
        User user = getUser(request);
        
        return pageTemplate
                .replace("FULL_NAME", escape(user.getFullName()))
                .replace("EMAIL", escape(user.getEmail()));
    }

    @Override
    protected Map<String, Consumer<HttpServletRequest>> createOperations() {
        return ImmutableMap.of("modify", request -> modifyUser(request, getUser(request)));
    }
    
    private User getUser(HttpServletRequest request) {
        return userService.getUserByUsername(request.getRemoteUser());
    }
}
