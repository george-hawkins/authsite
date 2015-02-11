package net.betaengine.authsite;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("httpsRedirect")
public class HttpsRedirectServlet extends HttpServlet {
    private final Boolean enabled = Boolean.getBoolean("https.redirect");
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (enabled && request.getScheme().equals("http")) {
            String uri = (String)request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            String query = (String)request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
            
            String url = "https://" + request.getServerName() + uri;
            
            if (query != null) {
                url += '?' + query;
            }
            response.sendRedirect(url);
        } else {
            int code = (Integer)request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            String message = (String)request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            
            if (message == null) {
                response.sendError(code);
            } else {
                response.sendError(code, message);
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}