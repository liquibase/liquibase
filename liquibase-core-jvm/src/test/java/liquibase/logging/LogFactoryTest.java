package liquibase.logging;

import org.junit.Test;
import static org.junit.Assert.*;

public class LogFactoryTest {
    
    @Test
    public void getLogger() {
        assertNotNull(LogFactory.getLogger());
    }
}
