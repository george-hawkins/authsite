package net.betaengine.authsite.heroku;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.betaengine.authsite.util.InstallInMemoryProtocolHandler.InMemoryURLStreamHandlerFactory.InMemoryURLConnection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

//
// JDBCLoginService.doStart() expects the JDBC configuration properties to be available via a simple static resource.
//
// With Heroku we want to get some of the properties from DATABASE_URL so the logic here constructs an in-memory
// resource made up of properties read from a file combined with some constructed dynamically from DATABASE_URL.
//
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
