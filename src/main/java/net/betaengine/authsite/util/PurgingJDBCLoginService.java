package net.betaengine.authsite.util;

import java.io.IOException;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.JDBCLoginService;
import org.eclipse.jetty.server.UserIdentity;

// If you change the password for a user in the underlying DB this will not be picked
// up by JDBCLoginService if that user has already logged in as it caches all details.
//
// You can set cacheTime to 0 which causes all cached data to be discarded each time
// someone logs in.
//
// This causes a hit for all users next time they access a page (as the cached details
// are used to check page rights etc.).
//
// So this subclass just discards the details of the user who is logging in.
//
// See http://stackoverflow.com/questions/28170319/jetty-jdbcloginservice-doesnt-pick-up-password-changes
//
public class PurgingJDBCLoginService extends JDBCLoginService {
    @Override
    public UserIdentity login(String username, Object credentials) {
        removeUser(username);
        
        return super.login(username, credentials);
    }
    
    // -----------------------------------------------------------------

    public PurgingJDBCLoginService() throws IOException {
    }

    public PurgingJDBCLoginService(String name) throws IOException {
        super(name);
    }

    public PurgingJDBCLoginService(String name, String config) throws IOException {
        super(name, config);
    }

    public PurgingJDBCLoginService(String name, IdentityService identityService, String config) throws IOException {
        super(name, identityService, config);
    }
}
