package net.betaengine.authsite;

import java.io.IOException;

import net.betaengine.authsite.util.Util;

import org.junit.Assert;
import org.junit.Test;

public class JsonTest {
    @Test
    public void testJsonBodies() throws IOException {
        String jsonString = Util.generateJsonAsString(generator -> {
            generator.writeBooleanField("isLoggedIn", true);
            generator.writeStringField("username", "ghawkins");
            generator.writeStringField("fullName", "George Hawkins");
        });

        // Bean level JSON generators etc. don't guarantee a particular field ordering.
        // But with this kind of low-level generation we know the ordering...
        Assert.assertEquals(jsonString, "{\"isLoggedIn\":true,\"username\":\"ghawkins\",\"fullName\":\"George Hawkins\"}");
    }
}
