package liquibase.integration.servlet;

import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.ProvidedValue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

public class ServletConfigurationValueProvider implements ConfigurationValueProvider {

    private static final String JAVA_COMP_ENV = "java:comp/env";

    @Override
    public int getPrecedence() {
        return 30;
    }

    private final ServletContext servletContext;
    private final InitialContext initialContext;

    public ServletConfigurationValueProvider(ServletContext servletContext, InitialContext initialContext) {
        this.servletContext = servletContext;
        this.initialContext = initialContext;
    }

    /**
     * Try to read the value that is stored by the given key from
     * <ul>
     * <li>JNDI</li>
     * <li>the servlet context's init parameters</li>
     * <li>system properties</li>
     * </ul>
     */
    @Override
    public ProvidedValue getProvidedValue(String... keyAndAliases) {
        if (initialContext != null) {
            for (String key : keyAndAliases) {
                // Try to get value from JNDI
                try {
                    Context envCtx = (Context) initialContext.lookup(JAVA_COMP_ENV);
                    String valueFromJndi = (String) envCtx.lookup(key);

                    return new ProvidedValue(keyAndAliases[0], JAVA_COMP_ENV + "/" + key, valueFromJndi, "JNDI", this);
                } catch (NamingException e) {
                    // Ignore
                }
            }
        }

        if (servletContext != null) {
            for (String key : keyAndAliases) {
                // Return the value from the servlet context
                String valueFromServletContext = servletContext.getInitParameter(key);
                if (valueFromServletContext != null) {
                    return new ProvidedValue(keyAndAliases[0], key, valueFromServletContext, "Servlet context", this);
                }
            }
        }

        return null;
    }
}
