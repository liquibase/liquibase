package liquibase.integration.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

/**
 * Version of {@link GenericServletListener} that uses javax.servlet and NOT jakarta.servlet
 */
public class LiquibaseServletListener extends GenericServletListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(new JavaxServletContext(sce.getServletContext()));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private static class JavaxServletContext extends GenericServletWrapper.ServletContext {

        private final javax.servlet.ServletContext servletContext;

        private JavaxServletContext(javax.servlet.ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public void log(String message) {
            servletContext.log(message);
        }

        @Override
        public String getInitParameter(String key) {
            return servletContext.getInitParameter(key);
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return servletContext.getInitParameterNames();
        }
    }


}
