package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.H2Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;
import org.junit.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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
        assertThat(sql).isNotEmpty().hasSize(1);
        assertThat(sql[0].toSql()).doesNotContain("IF NOT EXISTS");

        // verify that if no version is available the optional no IF NOT EXISTS is not in the statement
        reset(dbConnection);
        when(dbConnection.getDatabaseMajorVersion()).thenThrow(DatabaseException.class);

        sql = new CreateSequenceGenerator().generateSql(createSequenceStatement, database, new MockSqlGeneratorChain());
        assertThat(sql).isNotEmpty().hasSize(1);
        assertThat(sql[0].toSql()).doesNotContain("IF NOT EXISTS");

        reset(dbConnection);
        when(dbConnection.getDatabaseMajorVersion()).thenReturn(9);
        when(dbConnection.getDatabaseMinorVersion()).thenReturn(5);

        sql = new CreateSequenceGenerator().generateSql(createSequenceStatement, database, new MockSqlGeneratorChain());
        assertThat(sql).isNotEmpty().hasSize(1);
        assertThat(sql[0].toSql()).contains("IF NOT EXISTS");
    }

    @Test
    public void postgresDatabaseSupportAsStructureByVersion() throws Exception {
        DatabaseConnection dbConnection = mock(DatabaseConnection.class);
        ValidationErrors errors;
        PostgresDatabase postgresDatabase = spy(new PostgresDatabase());
        postgresDatabase.setConnection(dbConnection);
        doReturn(SEQUENCE_NAME).when(postgresDatabase).escapeSequenceName(CATALOG_NAME, SCHEMA_NAME, SEQUENCE_NAME);

        // verify that for version < 10 validate() method returns error
        when(dbConnection.getDatabaseMajorVersion()).thenReturn(9);
        when(dbConnection.getDatabaseMinorVersion()).thenReturn(6);

        CreateSequenceStatement createSequenceStatement = createSampleSqlStatement();
        createSequenceStatement.setStartValue(new BigInteger("1"));
        createSequenceStatement.setDataType("int");

        errors = new CreateSequenceGenerator().validate(createSequenceStatement, postgresDatabase, new MockSqlGeneratorChain());
        assertThat(errors.getErrorMessages()).contains("AS is not allowed on postgresql");


        // verify that if no version is available the validate() method passes
        reset(dbConnection);
        when(dbConnection.getDatabaseMajorVersion()).thenThrow(DatabaseException.class);
        errors = new CreateSequenceGenerator().validate(createSequenceStatement, postgresDatabase, new MockSqlGeneratorChain());
        assertThat(errors.getErrorMessages()).isEmpty();


        // verify that for version >= 10 the validate() method passes
        reset(dbConnection);
        when(dbConnection.getDatabaseMajorVersion()).thenReturn(10);
        when(dbConnection.getDatabaseMinorVersion()).thenReturn(0);

        errors = new CreateSequenceGenerator().validate(createSequenceStatement, postgresDatabase, new MockSqlGeneratorChain());
        assertThat(errors.getErrorMessages()).isEmpty();
    }

    @Test
    public void h2IgnoresDataType() {


        final CreateSequenceStatement stmt = new CreateSequenceStatement(null, null, "test_seq")
                .setDataType("BIGINT");
        ValidationErrors errors = new CreateSequenceGenerator().validate(stmt, new H2Database(), new MockSqlGeneratorChain());
        assertThat(errors.getErrorMessages()).isEmpty();
        assertThat(errors.getWarningMessages()).isEmpty();

        stmt.setDataType("INT");
        errors = new CreateSequenceGenerator().validate(stmt, new H2Database(), new MockSqlGeneratorChain());
        assertThat(errors.getErrorMessages()).isEmpty();
        assertEquals("H2 only crates BIGINT sequences. Ignoring requested type INT", errors.getWarningMessages().get(0));
        assertEquals("CREATE SEQUENCE test_seq", new CreateSequenceGenerator().generateSql(stmt, new H2Database(), new MockSqlGeneratorChain())[0].toSql());

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
