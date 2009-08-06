package liquibase.database;

import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseFactoryTest {
    
    @Test
    public void getInstance() {
        assertNotNull(DatabaseFactory.getInstance());
    }
}
