package net.betaengine.authsite.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletRequest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
    
    @FunctionalInterface
    public interface JsonAsStringFunction {
        void accept(JsonGenerator generator) throws IOException;
    }
    
    public static String generateJsonAsString(JsonAsStringFunction mapper) {
        try {
            StringWriter writer = new StringWriter();
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(writer);
            generator.writeStartObject();
            mapper.accept(generator);
            generator.writeEndObject();
            generator.close();
            
            return writer.toString();
        } catch (IOException e) {
            // IO exceptions shouldn't occur as all actions are in-memory.
            throw Util.unchecked(e);
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

    /** Returns all trimmed non-empty parameter values with the given name and throws an exception if there are none. */
    public static List<String> getMandatoryParameterValues(ServletRequest request, String name) {
        List<String> result = getParameterValues(request, name);
        
        if (result.isEmpty()) {
            throw new MissingParameterException(name);
        }

        return result;
    }
    
    /** Returns the first trimmed non-empty parameter value with the given name. */
    public static Optional<String> getOptionalParameter(ServletRequest request, String name) {
        return getParameterValues(request, name).stream().findFirst();
    }
    
    /** Returns all trimmed non-empty parameter values with the given name and returns an empty list if there are none. */
    public static List<String> getParameterValues(ServletRequest request, String name) {
        return Optional.ofNullable(request.getParameterValues(name))
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("serial")
    public static class MissingParameterException extends RuntimeException {
        public MissingParameterException(String name) {
            super(name);
        }
    }
}
