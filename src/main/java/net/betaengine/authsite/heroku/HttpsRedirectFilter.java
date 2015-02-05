package net.betaengine.authsite.heroku;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** This filter redirects all http:// requests to https:// - use instead of CONFIDENTIAL transport-guarantee on Heroku. */
// See http://stackoverflow.com/a/11574558/245602 and http://stackoverflow.com/a/13649976/245602
public class HttpsRedirectFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        
        if (!isConfidential(request)) {
            String httpsUrl = request.getRequestURL().toString().replace("http:", "https:");

            response.sendRedirect(httpsUrl);
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isConfidential(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeaders("x-forwarded-proto"))
                .map(Collections::list)
                .map(l -> l.contains("https"))
                .orElse(false);
    }

    @Override
    public void destroy() { }
}
