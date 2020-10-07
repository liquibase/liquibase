package liquibase.lockservice;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.MockDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LockServiceFactoryTest {

    @Before
    public void before() {
        Scope.getCurrentScope().getSingleton(LockServiceFactory.class).resetAll();
    }

    @After
    public void after() {
        Scope.getCurrentScope().getSingleton(LockServiceFactory.class).resetAll();
    }

    @Test
    public void getInstance() {
        assertNotNull(Scope.getCurrentScope().getSingleton(LockServiceFactory.class));
        assertTrue(Scope.getCurrentScope().getSingleton(LockServiceFactory.class) == Scope.getCurrentScope().getSingleton(LockServiceFactory.class));

//        Collection<LockService> lockServices = Scope.getCurrentScope().getSingleton(LockServiceFactory.class).getLockServices();
//        assertEquals(0, lockServices.size());
    }

    @Test
    public void getLockService() throws Exception {
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

        LockServiceFactory lockServiceFactory = Scope.getCurrentScope().getSingleton(LockServiceFactory.class);

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
