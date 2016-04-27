package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AlterSequenceStatement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AlterSequenceGeneratorTest extends AbstractSqlGeneratorTest<AlterSequenceStatement> {

    private static final String SEQUENCE_NAME = "sequence_name";

    H2Database mockH2DatabasePre18Jan2014Version;
    H2Database mockH2DatabasePost18Jan2014Version;

    public AlterSequenceGeneratorTest() throws Exception {
        this(new AlterSequenceGenerator());
    }

    protected AlterSequenceGeneratorTest(SqlGenerator<AlterSequenceStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

    @Override
    protected AlterSequenceStatement createSampleSqlStatement() {
        return new AlterSequenceStatement(null, null, SEQUENCE_NAME);
    }

    @Before
    public void setUpMocks() throws DatabaseException {

        mockH2DatabasePre18Jan2014Version = mock(H2Database.class);
        when(mockH2DatabasePre18Jan2014Version.getDatabaseMajorVersion()).thenReturn(1);
        when(mockH2DatabasePre18Jan2014Version.getDatabaseMinorVersion()).thenReturn(3);
        when(mockH2DatabasePre18Jan2014Version.getDatabaseProductVersion()).thenReturn("1.3.174 (2013-10-19)");

        mockH2DatabasePost18Jan2014Version = mock(H2Database.class);
        when(mockH2DatabasePost18Jan2014Version.getDatabaseMajorVersion()).thenReturn(1);
        when(mockH2DatabasePost18Jan2014Version.getDatabaseMinorVersion()).thenReturn(3);
        when(mockH2DatabasePost18Jan2014Version.getDatabaseProductVersion()).thenReturn("1.3.175 (2014-01-18)");
    }

    @Test
    public void h2DatabasePre18Jan2014VersionDoesNotSupportSequenceMaxValue() throws Exception {

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMaxValue(new BigInteger("1000"));

        assertTrue(generatorUnderTest.validate(alterSequenceStatement, mockH2DatabasePre18Jan2014Version, new MockSqlGeneratorChain()).hasErrors());
    }

    @Test
    public void h2DatabasePost18Jan2014VersionSupportsSequenceMaxValue() throws Exception {

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMaxValue(new BigInteger("1000"));

        assertFalse(generatorUnderTest.validate(alterSequenceStatement, mockH2DatabasePost18Jan2014Version, new MockSqlGeneratorChain()).hasErrors());
    }

    @Test
    public void h2DatabasePre18Jan2014VersionDoesNotSupportSequenceMinValue() throws Exception {

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMinValue(new BigInteger("1000"));

        assertTrue(generatorUnderTest.validate(alterSequenceStatement, mockH2DatabasePre18Jan2014Version, new MockSqlGeneratorChain()).hasErrors());
    }

    @Test
    public void h2DatabasePost18Jan2014VersionSupportsSequenceMinValue() throws Exception {

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMinValue(new BigInteger("1000"));

        assertFalse(generatorUnderTest.validate(alterSequenceStatement, mockH2DatabasePost18Jan2014Version, new MockSqlGeneratorChain()).hasErrors());
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof OracleDatabase
                || database instanceof MSSQLDatabase
                || database instanceof PostgresDatabase
                || database instanceof HsqlDatabase
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof InformixDatabase;
    }
////    @Test
////    public void supports() throws Exception {
////        for (Database database : TestContext.getWriteExecutor().getAllDatabases()) {
////            if (database.supportsSequences()) {
////                assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////            } else {
////                assertFalse(createGeneratorUnderTest().supportsDatabase(database));
////            }
////        }
////    }
//
//    @Test
//    public void execute_incrementBy() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setIncrementBy(5)) {
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by is 1
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_minValue() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setMinValue(0)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo; assert minValue is 1
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
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setMaxValue(50)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert initial max value
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert max value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_order() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AlterSequenceStatement(null, SEQ_NAME).setOrdered(true)) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !(database instanceof OracleDatabase || database instanceof DB2Database);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert order default
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert max value
//                    }
//                });
//    }
//
//    @Test
//    public void execute_schemaSet() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AlterSequenceStatement(TestContext.ALT_SCHEMA, SEQ_NAME).setIncrementBy(5)) {
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return database instanceof FirebirdDatabase || database instanceof HsqlDatabase || database  instanceof H2Database;
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by is 1
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getSequence(SEQ_NAME));
//                        //todo: assert increment by value
//                    }
//                });
//    }

}
