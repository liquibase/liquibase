package liquibase.integration.cdi;

import liquibase.Scope;
import liquibase.logging.core.BufferedLogService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Level;

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

    @Test
    public void onStartupExceptionsAreCorrectlyHandled() throws Exception {
        System.setProperty("liquibase.config.shouldRun", "true");
        final CDILiquibase cdi = new CDILiquibase() {
            @Override
            protected void performUpdate() {
                throw new IllegalArgumentException("Tested Exception");
            }
        };
        cdi.config = new CDILiquibaseConfig();

        BufferedLogService bufferLog = new BufferedLogService();
        Scope.child(Scope.Attr.logService.name(), bufferLog, () -> {
            try {
                cdi.onStartup();
                fail("Did not throw exception");
            } catch (IllegalArgumentException e) {
                assert e.getMessage().equals("Tested Exception");
                assert bufferLog.getLogAsString(Level.SEVERE).contains("SEVERE Tested Exception");
            }
        });
    }
}
