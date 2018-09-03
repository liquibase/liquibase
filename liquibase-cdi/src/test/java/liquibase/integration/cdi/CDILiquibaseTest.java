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
        System.clearProperty("liquibase.config.shouldRun");
        LiquibaseConfiguration.getInstance().reset();
    }

    private void validateRunningState(boolean shouldBeRunning) {
        WeldContainer weld = new Weld().initialize();
        CDILiquibase cdiLiquibase = weld.instance().select(CDILiquibase.class).get();
        assertNotNull(cdiLiquibase);
        assertEquals(shouldBeRunning, cdiLiquibase.isInitialized());
        assertEquals(shouldBeRunning, cdiLiquibase.isUpdateSuccessful());
    }

    @Test
    public void shouldntRunWhenShouldRunIsFalse() {
        System.setProperty("liquibase.shouldRun", "false");
        validateRunningState(false);
    }

    @Test
    public void shouldRunWhenShouldRunIsTrue() {
        System.setProperty("liquibase.shouldRun", "true");
        validateRunningState(true);
    }

    @Test
    public void shouldntRunWhenConfigShouldRunIsFalse() {
        System.setProperty("liquibase.config.shouldRun", "false");
        validateRunningState(false);
    }

    @Test
    public void shouldRunWhenConfigShouldRunIsTrue() {
        System.setProperty("liquibase.config.shouldRun", "true");
        validateRunningState(true);
    }
}
