package liquibase.integration.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Enumeration;

public class LiquibaseJakartaServletListener extends GenericServletListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(new JakartaServletContext(sce.getServletContext()));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    private static class JakartaServletContext extends GenericServletWrapper.ServletContext {

        private final jakarta.servlet.ServletContext servletContext;

        private JakartaServletContext(jakarta.servlet.ServletContext servletContext) {
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
