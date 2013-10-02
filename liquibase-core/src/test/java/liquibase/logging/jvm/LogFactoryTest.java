package liquibase.logging.jvm;

import static org.junit.Assert.*;

import liquibase.logging.LogFactory;
import org.junit.Test;

public class LogFactoryTest {
    
    @Test
    public void getLogger() {
        assertNotNull(LogFactory.getLogger());
    }
}
