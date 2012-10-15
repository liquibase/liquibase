package liquibase.lockservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;

/**
 * @author John Sanda
 */
public class LockServiceFactoryTest {

    @Before
    public void before() {
        LockServiceImpl.resetAll();
    }

    @After
    public void after() {
        LockServiceImpl.resetAll();
    }

    @Test
    public void getInstance() {
        assertNotNull(LockServiceFactory.getInstance());
        assertTrue(LockServiceFactory.getInstance() == LockServiceFactory.getInstance());

        Collection<LockService> lockServices = LockServiceFactory.getInstance().getLockServices();
        assertEquals(0, lockServices.size());
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
        LockServiceFactory factory = LockServiceFactory.getInstance();

        assertNotNull(factory.getLockService(oracle1));
        assertNotNull(factory.getLockService(oracle2));
        assertNotNull(factory.getLockService(mysql));

        assertTrue(factory.getLockService(oracle1) == factory.getLockService(oracle1));
        assertTrue(factory.getLockService(oracle2) == factory.getLockService(oracle2));
        assertTrue(factory.getLockService(mysql) == factory.getLockService(mysql));

        assertTrue(factory.getLockService(oracle1) != factory.getLockService(oracle2));
        assertTrue(factory.getLockService(oracle1) != factory.getLockService(mysql));
    }

}
