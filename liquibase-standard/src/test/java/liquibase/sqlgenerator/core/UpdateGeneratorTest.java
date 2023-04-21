package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UpdateGeneratorTest {
    @Test
    public void testGenerateSqlWithParamPlaceholders() {
        // given
        Database database = new MSSQLDatabase();
        UpdateStatement statement = new UpdateStatement(null, null, "DATABASECHANGELOG")
                .addNewColumnValue("MD5SUM", "7:e27bf9c0c2313160ef960a15d44ced47")
                .setWhereClause(
                        database.escapeObjectName("ID", Column.class) + " = ? " +
                        "AND " + database.escapeObjectName("AUTHOR", Column.class) + " = ? " +
                        "AND " + database.escapeObjectName("FILENAME", Column.class) + " = ?")
                .addWhereParameters(
                        "SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?",
                        "martin",
                        "db/changelog.xml");
        UpdateGenerator generator = new UpdateGenerator();

        // when
        Sql[] sqls = generator.generateSql(statement, database, null);

        // then
        assertEquals(
            "UPDATE DATABASECHANGELOG " +
                "SET MD5SUM = '7:e27bf9c0c2313160ef960a15d44ced47' " +
                "WHERE ID = N'SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?' " +
                "AND AUTHOR = 'martin' " +
                "AND FILENAME = 'db/changelog.xml'",
                sqls[0].toSql());
    }

    @Test
    public void testGenerateSqlWithNameValuePlaceholderPairs() {
        // given
        Database database = new MSSQLDatabase();
        UpdateStatement statement = new UpdateStatement(null, null, "DATABASECHANGELOG")
                .addNewColumnValue("MD5SUM", "7:e27bf9c0c2313160ef960a15d44ced47")
                .setWhereClause(":name = :value AND :name = :value AND :name = :value")
                .addWhereColumnName("ID")
                .addWhereColumnName("AUTHOR")
                .addWhereColumnName("FILENAME")
                .addWhereParameters(
                        "SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?",
                        "martin",
                        "db/changelog.xml");
        UpdateGenerator generator = new UpdateGenerator();

        // when
        Sql[] sqls = generator.generateSql(statement, database, null);

        // then
        assertEquals(
            "UPDATE DATABASECHANGELOG " +
                "SET MD5SUM = '7:e27bf9c0c2313160ef960a15d44ced47' " +
                "WHERE ID = N'SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?' " +
                "AND AUTHOR = 'martin' " +
                "AND FILENAME = 'db/changelog.xml'",
                sqls[0].toSql());
    }

////    @Test
////    public void addNewColumnValue_nullValue() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////
////            public void performTest(Database database) {
////                UpdateStatement statement = new UpdateStatement(null, TABLE_NAME);
////                statement.addNewColumnValue(COLUMN_NAME, null);
////
////                assertEquals("UPDATE " + database.escapeTableName(null, TABLE_NAME) + " SET " + database.escapeColumnName(null, TABLE_NAME, COLUMN_NAME) + " = NULL", statement.getSqlStatement(database));
////            }
////        });
////    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA,
//                new UpdateStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
//                        .addNewColumnValue(COLUMN_NAME, null)) {
//            protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                //nothing to test
//            }
//
//            protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                //nothing to test
//            }
//        });
//    }

}
