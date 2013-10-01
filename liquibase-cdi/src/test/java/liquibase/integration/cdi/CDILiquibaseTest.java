package liquibase.integration.cdi;

import liquibase.Liquibase;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for CDILiquibaseTest
 *
 *  @author Aaron Walker (http://github.com/aaronwalker)
 */
public class CDILiquibaseTest {


    @Test
    public void shouldntRunWhenShouldRunIsFalse() {
        System.setProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY,"false");
        WeldContainer weld = new Weld().initialize();
        CDILiquibase cdiLiquibase = weld.instance().select(CDILiquibase.class).get();
        assertNotNull(cdiLiquibase);
        assertFalse(cdiLiquibase.isInitialized());
        assertFalse(cdiLiquibase.isUpdateSuccessful());
    }

    @Test
    public void shouldRunWhenShouldRunIsTrue() {
        System.setProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY,"true");
        WeldContainer weld = new Weld().initialize();
        CDILiquibase cdiLiquibase = weld.instance().select(CDILiquibase.class).get();
        assertNotNull(cdiLiquibase);
        assertTrue(cdiLiquibase.isInitialized());
        assertTrue(cdiLiquibase.isUpdateSuccessful());
    }
}
