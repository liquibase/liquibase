package liquibase.integration.servlet;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.sql.DataSource;

import org.apache.derby.jdbc.BasicEmbeddedDataSource40;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LiquibaseServletListenerTest extends TestCase {

    private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";
    private static final String LIQUIBASE_DATASOURCE = "liquibase.datasource";

    static DataSource dataSource;

    private LiquibaseServletListener servletListener;
    private ServletContext servletContext;
    private Context namingContext;

    public static Test suite() {
        return new TestSetup(new TestSuite(LiquibaseServletListenerTest.class)) {
            @Override protected void setUp() {
                TestInitialContextFactory.install();
                BasicEmbeddedDataSource40 ds = new BasicEmbeddedDataSource40();
                ds.setDatabaseName("memory:foo");
                ds.setCreateDatabase("create");
                dataSource = ds;
            }
            @Override protected void tearDown() {
                dataSource = null;
                TestInitialContextFactory.uninstall();
            }
        };
    }

    @Override
    public void setUp() throws Exception {
        servletListener = new LiquibaseServletListener();

        servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameter(LIQUIBASE_DATASOURCE)).thenReturn("myDS");
        when(servletContext.getInitParameter(LIQUIBASE_CHANGELOG))
                .thenReturn("liquibase/integration/servlet/simple-changelog.xml");
        when(servletContext.getInitParameterNames()).then(invocation ->
                Collections.enumeration(Arrays.asList(LIQUIBASE_DATASOURCE, LIQUIBASE_CHANGELOG)));

        Context env = mock(Context.class);
        when(env.lookup(anyString())).thenThrow(NamingException.class);

        namingContext = mock(Context.class);
        when(namingContext.lookup("java:comp/env")).thenReturn(env);
        when(namingContext.lookup("myDS")).thenReturn(dataSource);
        TestInitialContextFactory.setInitialContext(namingContext);
    }

    public void testShouldNotShutEmbeddedDerbyDown() throws Exception {
        try (Connection pooled = dataSource.getConnection()) {
            servletListener.contextInitialized(new ServletContextEvent(servletContext));
            assertEquals("connection.closed", false, pooled.isClosed());
        }
    }

}
