package liquibase.logging;

import static org.junit.Assert.*;
import org.junit.Test;

public class LogFactoryTest {
    
    @Test
    public void getLogger() {
        assertNotNull(LogFactory.getLogger());
    }
}
