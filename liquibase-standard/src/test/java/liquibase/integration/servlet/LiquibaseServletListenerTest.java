package liquibase.integration.servlet;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.h2.jdbcx.JdbcDataSource;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class LiquibaseServletListenerTest extends TestCase {

    private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";
    private static final String LIQUIBASE_DATASOURCE = "liquibase.datasource";

    static DataSource dataSource;

    private LiquibaseServletListener servletListener;
    private GenericServletWrapper.ServletContext servletContext;
    private Context namingContext;

    public static Test suite() {
        return new TestSetup(new TestSuite(LiquibaseServletListenerTest.class)) {
            @Override protected void setUp() {
                TestInitialContextFactory.install();
                JdbcDataSource ds = new JdbcDataSource();
                ds.setURL("jdbc:h2:mem:lbcat");
                ds.setUser("sa");
                ds.setPassword("sa");
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

        servletContext = mock(GenericServletWrapper.ServletContext.class);
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

    public void testShouldNotShutEmbeddedDatabaseDown() throws Exception {
        if (dataSource == null) {
            return;
        }
        try (Connection pooled = dataSource.getConnection()) {
            servletListener.contextInitialized(servletContext);
            assertFalse("connection.closed", pooled.isClosed());
        }
    }

}
