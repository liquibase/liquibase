package liquibase.database;

import static org.junit.Assert.*;

import org.junit.Test;

public class DatabaseFactoryTest {
    
    @Test
    public void getInstance() {
        assertNotNull(DatabaseFactory.getInstance());
    }
}
