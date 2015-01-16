package net.betaengine.jettyexample.heroku;

import static net.betaengine.jettyexample.Util.unchecked;

import java.io.IOException;
import java.util.Optional;

import net.betaengine.jettyexample.Util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/**  Launch the web application in an embedded Jetty container. */
public class Main {
    private final static String WEBAPP_PATH = "src/main/webapp";
    private final static String WEBXML_PATH = WEBAPP_PATH + "/WEB-INF/web.xml";
    private final static String PORT_ENV = "PORT";
    private final static int DEFAULT_PORT = 8080;
    
    public static void main(String[] args) throws Exception {
        try {
            WebAppContext root = createRoot();
            Server server = new Server(getPort());
            
            server.setHandler(root);
            server.start();
            
            if (!root.isAvailable()) {
                // Container will have already logged the reason.
                System.exit(1);
            }
            
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // Without an explicit exit non-daemon threads will keep JVM alive.
        }
    }
    
    private static WebAppContext createRoot() {
        try {
            System.setProperty("jetty.base", "target/classes");
            Resource rootContextXml = Resource.newSystemResource("root-context.xml");
            XmlConfiguration configuration = new XmlConfiguration(rootContextXml.getInputStream());
            WebAppContext root = (WebAppContext)configuration.configure();
            
            // Use standard JSE class loading priority rather than prioritizing WEB-INF/[lib|classes].
            // See http://www.eclipse.org/jetty/documentation/current/jetty-classloading.html
            root.setParentLoaderPriority(true);

            return root;
        } catch (Exception e) {
            throw unchecked(e);
        }
    }
    
    /** Returns the port specified by the environment variable PORT or DEFAULT_PORT if not present. */
    private static int getPort() {
        Optional<String> envPort = Optional.ofNullable(System.getenv(PORT_ENV));
        
        return envPort.map(Integer::parseInt).orElse(DEFAULT_PORT);
    }
}