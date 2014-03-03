package liquibase.integration.cdi;

import liquibase.configuration.LiquibaseConfiguration;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CDILiquibaseTest
 *
 *  @author Aaron Walker (http://github.com/aaronwalker)
 */
public class CDILiquibaseTest {

    @Before
    @After
    public void clearProperty() {
        System.clearProperty("liquibase.shouldRun");
        LiquibaseConfiguration.getInstance().reset();
    }

    @Test
    public void shouldntRunWhenShouldRunIsFalse() {
        System.setProperty("liquibase.shouldRun", "false");
        WeldContainer weld = new Weld().initialize();
        CDILiquibase cdiLiquibase = weld.instance().select(CDILiquibase.class).get();
        assertNotNull(cdiLiquibase);
        assertFalse(cdiLiquibase.isInitialized());
        assertFalse(cdiLiquibase.isUpdateSuccessful());
    }

    @Test
    public void shouldRunWhenShouldRunIsTrue() {
        System.setProperty("liquibase.shouldRun", "true");
        WeldContainer weld = new Weld().initialize();
        CDILiquibase cdiLiquibase = weld.instance().select(CDILiquibase.class).get();
        assertNotNull(cdiLiquibase);
        assertTrue(cdiLiquibase.isInitialized());
        assertTrue(cdiLiquibase.isUpdateSuccessful());
    }
}
