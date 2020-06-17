package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.lockservice.ext.MockLockService;
import liquibase.sdk.database.MockDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author John Sanda
 */
public class LockServiceFactoryTest {

    @Before
    public void before() {
        LockServiceFactory.getInstance().resetAll();
    }

    @After
    public void after() {
        LockServiceFactory.getInstance().resetAll();
    }

    @Test
    public void getInstance() {
        assertNotNull(LockServiceFactory.getInstance());
        assertTrue(LockServiceFactory.getInstance() == LockServiceFactory.getInstance());

//        Collection<LockService> lockServices = LockServiceFactory.getInstance().getLockServices();
//        assertEquals(0, lockServices.size());
    }

    @Test
    public void getLockService() {
        final Database oracle1 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };
        final Database oracle2 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }

        };
        final Database mysql = new MySQLDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };

        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
        databaseFactory.register(oracle1);
        databaseFactory.register(oracle2);
        databaseFactory.register(mysql);

        LockServiceFactory lockServiceFactory = LockServiceFactory.getInstance();

        assertNotNull(lockServiceFactory.getLockService(oracle1));
        assertNotNull(lockServiceFactory.getLockService(oracle2));
        assertNotNull(lockServiceFactory.getLockService(mysql));

        assertTrue(lockServiceFactory.getLockService(oracle1) == lockServiceFactory.getLockService(oracle1));
        assertTrue(lockServiceFactory.getLockService(oracle2) == lockServiceFactory.getLockService(oracle2));
        assertTrue(lockServiceFactory.getLockService(mysql) == lockServiceFactory.getLockService(mysql));

        assertTrue(lockServiceFactory.getLockService(oracle1) != lockServiceFactory.getLockService(oracle2));
        assertTrue(lockServiceFactory.getLockService(oracle1) != lockServiceFactory.getLockService(mysql));

        assertTrue(lockServiceFactory.getLockService(getMockDatabase()) instanceof MockLockService);
    }

    private MockDatabase getMockDatabase() {
        DatabaseFactory factory = DatabaseFactory.getInstance();
        for (Database db : factory.getInternalDatabases()) {
            if (db instanceof MockDatabase) {
                return (MockDatabase) db;
            }
        }
        return null;
    }

}
