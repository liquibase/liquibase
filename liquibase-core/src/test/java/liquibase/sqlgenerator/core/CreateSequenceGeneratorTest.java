package liquibase.sqlgenerator.core;

import static org.mockito.Mockito.*;

import java.math.BigInteger;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;

public class CreateSequenceGeneratorTest extends AbstractSqlGeneratorTest<CreateSequenceStatement> {

    protected static final String SEQUENCE_NAME = "SEQUENCE_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";
    protected static final String SCHEMA_NAME = "SCHEMA_NAME";
//    private DatabaseConnection mockedUnsupportedMinMaxSequenceConnection;
//    private DatabaseConnection mockedSupportedMinMaxSequenceConnection;

    public CreateSequenceGeneratorTest() throws Exception {
        super(new CreateSequenceGenerator());
    }

    @Override
    protected CreateSequenceStatement createSampleSqlStatement() {
        return new CreateSequenceStatement(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);
    }

    @Test
    public void postgresDatabaseSupportIfNotExistsByVersion() throws Exception {
        DatabaseConnection dbConnection = mock(DatabaseConnection.class);
        when(dbConnection.getDatabaseMajorVersion()).thenReturn(9);
        when(dbConnection.getDatabaseMinorVersion()).thenReturn(4);

        PostgresDatabase database = spy(new PostgresDatabase());
        database.setConnection(dbConnection);
        doReturn(SEQUENCE_NAME).when(database).escapeSequenceName(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);

        CreateSequenceStatement createSequenceStatement = createSampleSqlStatement();
        createSequenceStatement.setStartValue(new BigInteger("1"));

        // verify that for version <= 9.4 no IF NOT EXISTS is not in the statement
        Sql[] sql = new CreateSequenceGenerator().generateSql(createSequenceStatement, database, new MockSqlGeneratorChain());
        Assertions.assertThat(sql).isNotEmpty().hasSize(1);
        Assertions.assertThat(sql[0].toSql()).doesNotContain("IF NOT EXISTS");

        // verify that if no version is available the optional no IF NOT EXISTS is not in the statement
        reset(dbConnection);
        when(dbConnection.getDatabaseMajorVersion()).thenThrow(DatabaseException.class);

        sql = new CreateSequenceGenerator().generateSql(createSequenceStatement, database, new MockSqlGeneratorChain());
        Assertions.assertThat(sql).isNotEmpty().hasSize(1);
        Assertions.assertThat(sql[0].toSql()).doesNotContain("IF NOT EXISTS");

        reset(dbConnection);
        when(dbConnection.getDatabaseMajorVersion()).thenReturn(9);
        when(dbConnection.getDatabaseMinorVersion()).thenReturn(5);

        sql = new CreateSequenceGenerator().generateSql(createSequenceStatement, database, new MockSqlGeneratorChain());
        Assertions.assertThat(sql).isNotEmpty().hasSize(1);
        Assertions.assertThat(sql[0].toSql()).contains("IF NOT EXISTS");
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

//    @Test
//    public void h2DatabaseSupportsSequenceMaxValue() throws Exception {
//
//        H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedSupportedMinMaxSequenceConnection);
//
//        CreateSequenceStatement createSequenceStatement = createSampleSqlStatement();
//        createSequenceStatement.setMaxValue(new BigInteger("1000"));
//
//        assertFalse(generatorUnderTest.validate(createSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//    }

//    @Test
//    public void h2DatabaseDoesNotSupportsSequenceMaxValue() throws Exception {
//
//        H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedUnsupportedMinMaxSequenceConnection);
//
//        CreateSequenceStatement createSequenceStatement = createSampleSqlStatement();
//        createSequenceStatement.setMaxValue(new BigInteger("1000"));
//
//        assertTrue(generatorUnderTest.validate(createSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//    }

//    @Test
//    public void h2DatabaseSupportsSequenceMinValue() throws Exception {
//
//        H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedSupportedMinMaxSequenceConnection);
//
//        CreateSequenceStatement createSequenceStatement = createSampleSqlStatement();
//        createSequenceStatement.setMinValue(new BigInteger("10"));
//
//        assertFalse(generatorUnderTest.validate(createSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//    }

//    @Test
//    public void h2DatabaseDoesNotSupportsSequenceMinValue() throws Exception {
//
//        H2Database h2Database = new H2Database();
//        h2Database.setConnection(mockedUnsupportedMinMaxSequenceConnection);
//
//        CreateSequenceStatement createSequenceStatement = createSampleSqlStatement();
//        createSequenceStatement.setMinValue(new BigInteger("10"));
//
//        assertTrue(generatorUnderTest.validate(createSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
//    }

    @Override
    protected boolean shouldBeImplementation(Database database) {

        return database.supportsSequences();
    }

//    //    @Test
////    public void supports() throws Exception {
////        new DatabaseTestTemplate().testOnAvailableDatabases(new DatabaseTest() {
////            public void performTest(Database database) throws Exception {
////                if (database.supportsSequences()) {
////                    assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////                } else {
////                    assertFalse(createGeneratorUnderTest().supportsDatabase(database));
////                }
////            }
////        });
////    }
//
//    @Test
//    public void execute() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                    }
//                });
//    }
//
//    @Test
//    public void execute_startValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setStartValue(1000)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert start value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_incrementBy() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setIncrementBy(5)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by value
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_minValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setMinValue(15)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getSequence(SEQ_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert min valuevalue
//                    }
//                });
//    }
//
//    @Test
//    public void execute_maxValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setMaxValue(50)) {
//
//            protected boolean expectedException(Database database, DatabaseException exception) {
//                return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//            }
//
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNull(snapshot.getSequence(SEQ_NAME));
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNotNull(snapshot.getSequence(SEQ_NAME));
//                //todo: assert max value
//            }
//        });
//    }
//
//    @Test
//    public void execute_order() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(null, new CreateSequenceStatement(null, SEQ_NAME).setOrdered(true)) {
//            protected boolean expectedException(Database database, DatabaseException exception) {
//                return !(database instanceof OracleDatabase || database instanceof DB2Database);
//            }
//
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNull(snapshot.getSequence(SEQ_NAME));
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                assertNotNull(snapshot.getSequence(SEQ_NAME));
//                //todo: assert max value
//            }
//        });
//    }
}
