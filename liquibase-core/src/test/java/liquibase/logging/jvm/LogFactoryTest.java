package liquibase.logging.jvm;

import liquibase.logging.LogFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LogFactoryTest {
    
    @Test
    public void getLogger() {
        assertNotNull(LogFactory.getLogger());
    }
}
