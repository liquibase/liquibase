package liquibase.sqlgenerator.core;

import static org.junit.Assert.*;

import java.math.BigInteger;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.test.TestContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class AlterSequenceGeneratorTest extends AbstractSqlGeneratorTest<AlterSequenceStatement> {

    protected static final String SEQUENCE_NAME = "SEQUENCE_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";
    protected static final String SCHEMA_NAME = "SCHEMA_NAME";
	private DatabaseConnection mockedUnsupportedMinMaxSequenceConnection;
    private DatabaseConnection mockedSupportedMinMaxSequenceConnection;
    public AlterSequenceGeneratorTest() throws Exception {
        super(new AlterSequenceGenerator());
    }

    @Before
    public void setUpMocks() throws DatabaseException {

        mockedUnsupportedMinMaxSequenceConnection = mock(DatabaseConnection.class);
        when(mockedUnsupportedMinMaxSequenceConnection.getDatabaseMajorVersion()).thenReturn(1);
        when(mockedUnsupportedMinMaxSequenceConnection.getDatabaseMinorVersion()).thenReturn(3);
        when(mockedUnsupportedMinMaxSequenceConnection.getDatabaseProductVersion()).thenReturn("1.3.174 (2013-10-19)");

        mockedSupportedMinMaxSequenceConnection = mock(DatabaseConnection.class);
        when(mockedSupportedMinMaxSequenceConnection.getDatabaseMajorVersion()).thenReturn(1);
        when(mockedSupportedMinMaxSequenceConnection.getDatabaseMinorVersion()).thenReturn(3);
        when(mockedSupportedMinMaxSequenceConnection.getDatabaseProductVersion()).thenReturn("1.3.175 (2014-01-18)");
    }

    @Test
    public void testAlterSequenceDatabase() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                AlterSequenceStatement statement =  createSampleSqlStatement();
                statement.setCacheSize(BigInteger.valueOf(3000L));

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("ALTER SEQUENCE CATALOG_NAME.SEQUENCE_NAME CACHE 3000", generatedSql[0].toSql());
            }
        }
    }

    @Test
	public void h2DatabaseSupportsSequenceMaxValue() throws Exception {

		H2Database h2Database = new H2Database();
        h2Database.setConnection(mockedSupportedMinMaxSequenceConnection);

		AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
		alterSequenceStatement.setMaxValue(new BigInteger("1000"));

		assertFalse(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
	}

    @Test
    public void h2DatabaseDoesNotSupportsSequenceMaxValue() throws Exception {

        H2Database h2Database = new H2Database();
        h2Database.setConnection(mockedUnsupportedMinMaxSequenceConnection);

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMaxValue(new BigInteger("1000"));

        assertTrue(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
    }

	@Test
	public void h2DatabaseSupportsSequenceMinValue() throws Exception {

		H2Database h2Database = new H2Database();
        h2Database.setConnection(mockedSupportedMinMaxSequenceConnection);

		AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
		alterSequenceStatement.setMinValue(new BigInteger("10"));

		assertFalse(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
	}

    @Test
    public void h2DatabaseDoesNotSupportsSequenceMinValue() throws Exception {

        H2Database h2Database = new H2Database();
        h2Database.setConnection(mockedUnsupportedMinMaxSequenceConnection);

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMinValue(new BigInteger("10"));

        assertTrue(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
    }

	@Override
    protected AlterSequenceStatement createSampleSqlStatement() {
        AlterSequenceStatement statement = new AlterSequenceStatement(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);
        return statement;
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsSequences();
    }
}
