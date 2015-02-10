package net.betaengine.authsite.util;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

public class HttpsRedirectErrorPageHandler extends org.eclipse.jetty.servlet.ErrorPageErrorHandler {
    @Override
    protected void writeErrorPageMessage(HttpServletRequest request, Writer writer, int code, String message,String uri) throws IOException {
        if (uri.equals("/httpsRedirect")) {
            String originalUri = (String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
            
            // Ensure that the original page rather than the redirect page is named in the output.
            if (originalUri != null) {
                uri = originalUri;
            }
        }
        
        super.writeErrorPageMessage(request, writer, code, message, uri);
    }
}
