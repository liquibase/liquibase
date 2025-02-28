package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.CockroachDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.structure.core.Sequence;
import liquibase.test.TestContext;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class AlterSequenceGeneratorTest extends AbstractSqlGeneratorTest<AlterSequenceStatement> {

    protected static final String SEQUENCE_NAME = "SEQUENCE_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";
    protected static final String SCHEMA_NAME = "SCHEMA_NAME";
//	private DatabaseConnection mockedUnsupportedMinMaxSequenceConnection;
//    private DatabaseConnection mockedSupportedMinMaxSequenceConnection;
    public AlterSequenceGeneratorTest() throws Exception {
        super(new AlterSequenceGenerator());
    }

//    @Before
//    public void setUpMocks() throws DatabaseException {
//
//        mockedUnsupportedMinMaxSequenceConnection = mock(DatabaseConnection.class);
//        when(mockedUnsupportedMinMaxSequenceConnection.getDatabaseMajorVersion()).thenReturn(1);
//        when(mockedUnsupportedMinMaxSequenceConnection.getDatabaseMinorVersion()).thenReturn(3);
//        when(mockedUnsupportedMinMaxSequenceConnection.getDatabaseProductVersion()).thenReturn("1.3.174 (2013-10-19)");
//
//        mockedSupportedMinMaxSequenceConnection = mock(DatabaseConnection.class);
//        when(mockedSupportedMinMaxSequenceConnection.getDatabaseMajorVersion()).thenReturn(1);
//        when(mockedSupportedMinMaxSequenceConnection.getDatabaseMinorVersion()).thenReturn(3);
//        when(mockedSupportedMinMaxSequenceConnection.getDatabaseProductVersion()).thenReturn("1.3.175 (2014-01-18)");
//    }

    @Test
    public void testAlterSequenceDatabase(){
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (database instanceof OracleDatabase) {
                AlterSequenceStatement statement = createSampleSqlStatement();
                statement.setCacheSize(BigInteger.valueOf(3000L));

                Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);

                assertEquals("ALTER SEQUENCE CATALOG_NAME.SEQUENCE_NAME CACHE 3000", generatedSql[0].toSql());
            }
        }
    }

//    @Test
//	public void h2DatabaseSupportsSequenceMaxValue() throws Exception {
//
//		H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedSupportedMinMaxSequenceConnection);
//
//		AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
//		alterSequenceStatement.setMaxValue(new BigInteger("1000"));
//
//		assertFalse(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//	}

//    @Test
//    public void h2DatabaseDoesNotSupportsSequenceMaxValue() throws Exception {
//
//        H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedUnsupportedMinMaxSequenceConnection);
//
//        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
//        alterSequenceStatement.setMaxValue(new BigInteger("1000"));
//
//        assertTrue(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//    }

//	@Test
//	public void h2DatabaseSupportsSequenceMinValue() throws Exception {
//
//		H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedSupportedMinMaxSequenceConnection);
//
//		AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
//		alterSequenceStatement.setMinValue(new BigInteger("10"));
//
//		assertFalse(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//	}
//
//    @Test
//    public void h2DatabaseDoesNotSupportsSequenceMinValue() throws Exception {
//
//        H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedUnsupportedMinMaxSequenceConnection);
//
//        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
//        alterSequenceStatement.setMinValue(new BigInteger("10"));
//
//        assertTrue(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//    }

    @Test
    public void testAlterSequenceCycleDatabase() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            AlterSequenceStatement statement = createSampleSqlStatement();
            statement.setCycle(false);
            Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
            if (database instanceof OracleDatabase) {
                assertEquals("ALTER SEQUENCE CATALOG_NAME.SEQUENCE_NAME NOCYCLE", generatedSql[0].toSql());
            } else if (database instanceof PostgresDatabase || database instanceof CockroachDatabase) {
                assertEquals("ALTER SEQUENCE SCHEMA_NAME.SEQUENCE_NAME NO CYCLE", generatedSql[0].toSql());
            }
        }
    }

    @Override
    protected AlterSequenceStatement createSampleSqlStatement() {
        return new AlterSequenceStatement(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supports(Sequence.class);
    }
}
