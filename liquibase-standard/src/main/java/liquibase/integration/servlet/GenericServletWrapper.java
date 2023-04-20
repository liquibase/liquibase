package liquibase.integration.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * To support both javax.servlet and jakarta.servlet implementations, this class wraps the classes we use so that the shared code does not have to depend
 * on a particular implementation.
 * <p>
 * NOTE: Only methods which Liquibase currently uses are exposed, which may cause breaking changes if/when new abstract methods are added to the wrappers.
 */
class GenericServletWrapper {

    public abstract static class ServletContext {

        public abstract void log(String message);

        public abstract String getInitParameter(String key);

        public abstract Enumeration<String> getInitParameterNames();
    }

    public abstract static class HttpServletRequest {

        public abstract String getParameter(String key);

        public abstract String getRequestURI();
    }

    public abstract static class HttpServletResponse {

        public abstract void setStatus(int status);

        public abstract void setContentType(String type);

        public abstract PrintWriter getWriter() throws IOException;
    }

}
