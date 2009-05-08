package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.template.Executor;
import liquibase.exception.JDBCException;
import liquibase.sql.Sql;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSqlGeneratorTest<T extends SqlStatement> {

    protected SqlGenerator<T> generatorUnderTest;

    public AbstractSqlGeneratorTest(SqlGenerator generatorUnderTest) throws Exception {
        this.generatorUnderTest = generatorUnderTest;
    }

    protected abstract T createSampleSqlStatement();

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, JDBCException {
        new Executor(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    @Test
    public void isImplementation() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            boolean isImpl = generatorUnderTest.supports(createSampleSqlStatement(), database);
            if (shouldBeImplementation(database)) {
                assertTrue("Unexpected false supports for " + database.getProductName(), isImpl);
            } else {
                assertFalse("Unexpected true supports for " + database.getProductName(), isImpl);
            }
        }
    }

    @Test
    public void isValid() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
        	if (shouldBeImplementation(database)) {
        		assertFalse("isValid failed against " + database, generatorUnderTest.validate(createSampleSqlStatement(), database).hasErrors());
        	} else if (waitForException(database)) {
        		assertTrue("The validation should be failed for " + database, generatorUnderTest.validate(createSampleSqlStatement(), database).hasErrors());
        	}
        }
    }

    @Test
    public void checkExpectedGenerator() {
        assertEquals(this.getClass().getName().replaceFirst("Test$", ""), generatorUnderTest.getClass().getName());
    }

    protected boolean waitForException(Database database) {
        return false;
    }

    protected boolean shouldBeImplementation(Database database) {
        return true;
    }



}
