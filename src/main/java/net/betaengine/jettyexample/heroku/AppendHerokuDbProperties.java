package net.betaengine.jettyexample.heroku;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.betaengine.jettyexample.util.InstallInMemoryProtocolHandler.InMemoryURLStreamHandlerFactory.InMemoryURLConnection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

public class AppendHerokuDbProperties extends InMemoryURLConnection {
    private final static String DB_PROPERTIES = "%nurl=%s%nusername=%s%npassword=%s%n";
    public AppendHerokuDbProperties(URL url, Multimap<String, String> parameters) {
        super(url, parameters);
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        String path = Iterables.getOnlyElement(parameters.get("path"));
        HerokuDbProperties properties = new HerokuDbProperties();
        String content = Files.toString(new File(path), StandardCharsets.ISO_8859_1);
        
        content += String.format(DB_PROPERTIES, properties.getUrl(), properties.getUsername(), properties.getPassword());
        
        // Note: java.util.Properties.load(InputStream) mandates ISO 8859-1.
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));
    }
}
