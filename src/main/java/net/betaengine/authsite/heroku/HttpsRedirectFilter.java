package net.betaengine.authsite.heroku;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpsRedirectFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        if (!isConfidential((HttpServletRequest)request)) {
            String httpsUrl = httpRequest.getRequestURL().toString().replace("http:", "https:");

            System.err.println("ZZZ " + httpsUrl);
//            httpResponse.sendRedirect(httpsUrl);
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isConfidential(HttpServletRequest request) {
        boolean secure = false;
        
        Enumeration<String> forwardProto = request.getHeaders("x-forwarded-proto");
        
        if (forwardProto != null) {
            System.err.println("XXX " + Collections.list(forwardProto));
        } else {
            System.err.println("YYY");
        }
        
        return secure;
    }

    @Override
    public void destroy() { }
}
