package net.betaengine.authsite.util;

import static net.betaengine.authsite.util.Util.unchecked;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class InstallInMemoryProtocolHandler extends AbstractLifeCycleListener {
    @Override
    public void lifeCycleStarting(LifeCycle event) {
        URL.setURLStreamHandlerFactory(new InMemoryURLStreamHandlerFactory());
    }
    
    public static class InMemoryURLStreamHandlerFactory implements URLStreamHandlerFactory {
        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            return protocol.equalsIgnoreCase("class") ? new InMemoryURLStreamHandler() : null;
        }
        
        private static class InMemoryURLStreamHandler extends URLStreamHandler {
            @Override
            protected URLConnection openConnection(URL url) throws IOException {
                try {
                    Multimap<String, String> parameters = getParameters(url.getQuery());
                    Class<?> clazz = Class.forName(url.getPath());
                    Constructor<?> ctor = clazz.getConstructor(URL.class, Multimap.class);
                    
                    return (URLConnection)ctor.newInstance(url, parameters);
                } catch (Exception e) {
                    throw unchecked(e);
                }
            }
            
            private Multimap<String, String> getParameters(String query) {
                MultiMap<String> map = new MultiMap<>();
                
                UrlEncoded.decodeTo(query, map, StandardCharsets.UTF_8, -1);
                
                Multimap<String, String> result = ArrayListMultimap.create();
                
                // Repackage Jetty multimap into Guava multimap.
                map.forEach((key, values) -> { result.putAll(key, values); });
                
                return result;
            }
        }
        
        /** Subclasses should implement getInputStream() and/or getOutputStream(). */
        public static class InMemoryURLConnection extends URLConnection {
            protected final Multimap<String, String> parameters;
            
            protected InMemoryURLConnection(URL url, Multimap<String, String> parameters) {
                super(url);
                
                this.parameters = parameters;
            }

            @Override
            public void connect() throws IOException { }
        }
    }
}
