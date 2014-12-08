package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AlterSequenceStatement;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertFalse;

public class AlterSequenceGeneratorTest extends AbstractSqlGeneratorTest<AlterSequenceStatement> {

    private static final String SEQUENCE_NAME = "sequence_name";

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

    @Test
    public void h2DatabaseSupportsSequenceMaxValue() throws Exception {

        H2Database h2Database = new H2Database();

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMaxValue(new BigInteger("1000"));

        assertFalse(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
    }

    @Test
    public void h2DatabaseSupportsSequenceMinValue() throws Exception {

        H2Database h2Database = new H2Database();

        AlterSequenceStatement alterSequenceStatement = createSampleSqlStatement();
        alterSequenceStatement.setMinValue(new BigInteger("10"));

        assertFalse(generatorUnderTest.validate(alterSequenceStatement, h2Database, new MockSqlGeneratorChain()).hasErrors());
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
