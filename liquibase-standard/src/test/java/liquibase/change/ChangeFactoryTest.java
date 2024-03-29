package liquibase.change;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.change.core.CreateTableChange;
import liquibase.database.core.MSSQLDatabase;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateSequenceStatement;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class ChangeFactoryTest {

    @Test
    public void supportStatement() {
        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, "seq_my_table");
        MSSQLDatabase database10 = new MSSQLDatabase() {
            @Override
            public int getDatabaseMajorVersion() {
                return MSSQL_SERVER_VERSIONS.MSSQL2008;
            }
        };

        MSSQLDatabase database11 = new MSSQLDatabase() {
            @Override
            public int getDatabaseMajorVersion() {
                return MSSQL_SERVER_VERSIONS.MSSQL2012;
            }
        };

        Scope.getCurrentScope().getSingleton(ChangeFactory.class); //make sure there is no problem with SqlGeneratorFactory.generatorsByKey cache
        assertFalse("unsupported create sequence", SqlGeneratorFactory.getInstance().supports(statement, database10));
        assertTrue("supported create sequence", SqlGeneratorFactory.getInstance().supports(statement, database11));
    }

    /**
     * This test exists to ensure that the cache key contains enough information so that we aren't considering
     * objects with different checksum versions as the same.
     */
    @Test
    public void differentChecksumVersionsResultInDifferentObjects() throws Exception {
        ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);

        Change change8 = new CreateTableChange();
        Change change9 = new CreateTableChange();

        ChangeMetaData changeMetaData8 = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), ChecksumVersion.V8), () -> changeFactory.getChangeMetaData(change8));
        ChangeMetaData changeMetaData9 = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), ChecksumVersion.V9), () -> changeFactory.getChangeMetaData(change9));

        assertNotEquals(changeMetaData8, changeMetaData9);
    }

    @Test
    public void create_exists() {
        Change change = Scope.getCurrentScope().getSingleton(ChangeFactory.class).create("createTable");

        assertNotNull(change);
        assertTrue(change instanceof CreateTableChange);

        assertNotSame(change, Scope.getCurrentScope().getSingleton(ChangeFactory.class).create("createTable"));
    }

    @Test
    public void create_notExists() {
        Change change = Scope.getCurrentScope().getSingleton(ChangeFactory.class).create("badChangeName");

        assertNull(change);

    }
    
    @LiquibaseService(skip = true)
    public static class Priority5Change extends CreateTableChange {
        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 5, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class Priority10Change extends CreateTableChange {
        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 10, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class AnotherPriority5Change extends CreateTableChange {
        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 5, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class ExceptionThrowingChange extends CreateTableChange {
        public ExceptionThrowingChange() {
            throw new RuntimeException("I throw exceptions");
        }

        @Override
        public ChangeMetaData createChangeMetaData() {
            return new ChangeMetaData("createTable", null, 15, null, null, null);
        }
    }
}
