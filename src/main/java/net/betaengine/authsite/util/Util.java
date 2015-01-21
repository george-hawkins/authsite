package net.betaengine.authsite.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;

import com.google.common.collect.ImmutableList;

public class Util {
    public static RuntimeException unchecked(Exception e) {
        return (e instanceof RuntimeException) ? (RuntimeException)e : new UncheckedException(e);
    }

    @SuppressWarnings("serial")
    public static class UncheckedException extends RuntimeException {
        public UncheckedException(Exception e) {
            super(e);
        }
    }
    
    public static String getMandatoryEnv(String name) {
        return Optional.ofNullable(System.getenv(name))
        .orElseThrow(() -> new MandatoryEnvException(name + " not present"));
    }
    
    @SuppressWarnings("serial")
    public static class MandatoryEnvException extends RuntimeException {
        public MandatoryEnvException(String message) { super(message); }
    }
    
    public static String getMandatoryParameter(ServletRequest request, String name) {
        return getMandatoryParameterValues(request, name).get(0);
    }

    /** Returns all trimmed non-empty parameters with the given name and throws an exception if there are none. */
    public static List<String> getMandatoryParameterValues(ServletRequest request, String name) {
        List<String> result = getParameterValues(request, name).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        
        if (result.isEmpty()) {
            throw new MissingParameterException(name);
        }

        return result;
    }
    
    /** Unlike ServletRequest.getParameterValues(String) this method returns an empty list if the parameter isn't present. */
    public static List<String> getParameterValues(ServletRequest request, String name) {
        return Optional.ofNullable(request.getParameterValues(name)).map(Arrays::asList).orElse(ImmutableList.of());
    }

    @SuppressWarnings("serial")
    public static class MissingParameterException extends RuntimeException {
        public MissingParameterException(String name) {
            super(name);
        }
    }
}
