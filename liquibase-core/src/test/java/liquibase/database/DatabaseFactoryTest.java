package liquibase.database;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DatabaseFactoryTest {
    
    @Test
    public void getInstance() {
        assertNotNull(DatabaseFactory.getInstance());
    }
}
