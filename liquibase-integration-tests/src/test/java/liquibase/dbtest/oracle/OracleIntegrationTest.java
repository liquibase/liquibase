package liquibase.dbtest.oracle;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.GenerateChangelogCommandStep;
import liquibase.command.core.SnapshotCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.visitor.AbstractSqlVisitor;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import static liquibase.test.SnapshotAssert.assertThat;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

/**
 * Integration test for Oracle Database, Version 11gR2 and above.
 */
public class OracleIntegrationTest extends AbstractIntegrationTest {
    String indexOnSchemaChangeLog;
    String viewOnSchemaChangeLog;
    String customExecutorChangeLog;
    String indexWithAssociatedWithChangeLog;
    String strictChangelog;

    public OracleIntegrationTest() throws Exception {
        super("oracle", DatabaseFactory.getInstance().getDatabase("oracle"));
        indexOnSchemaChangeLog = "changelogs/oracle/complete/indexOnSchema.xml";
        viewOnSchemaChangeLog = "changelogs/oracle/complete/viewOnSchema.xml";
        customExecutorChangeLog = "changelogs/oracle/complete/sqlplusExecutor.xml";
        indexWithAssociatedWithChangeLog = "changelogs/common/index.with.associatedwith.changelog.xml";
        strictChangelog = "changelogs/oracle/complete/strict.changelog.xml";
        // Respect a user-defined location for sqlnet.ora, tnsnames.ora etc. stored in the environment
        // variable TNS_ADMIN. This allowes the use of TNSNAMES.
        if (System.getenv("TNS_ADMIN") != null)
            System.setProperty("oracle.net.tns_admin",System.getenv("TNS_ADMIN"));
    }

    @Test
    public void sqlplusChangelog() throws Exception {
        Database database = this.getDatabase();
        assumeNotNull(database);

        Liquibase liquibase = createLiquibase(this.customExecutorChangeLog);
        clearDatabase();

        //
        // Add a visitor so we can assert
        //
        DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog();
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            changeSet.addSqlVisitor(new TestSqlVisitor());
        }
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
        database.commit();
    }

    private class TestSqlVisitor extends AbstractSqlVisitor {
        @Override
        public String modifySql(String sql, Database database) {
            Scope.getCurrentScope().getLog(getClass()).info("In the TestSqlVisitor.modifySql method");
            Scope.getCurrentScope().getLog(getClass()).info(sql);
            assertTrue(sql.startsWith("CREATE TABLE primary_table_numero_uno (name CHAR(20));"));
            assertTrue(sql.endsWith("CREATE TABLE primary_table_numero_cinco(name CHAR(20));"));
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }

    @Test
    public void indexCreatedOnCorrectSchema() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(this.indexOnSchemaChangeLog);
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        Statement queryIndex = ((JdbcConnection) this.getDatabase().getConnection()).getUnderlyingConnection().createStatement();

        ResultSet indexOwner = queryIndex.executeQuery("SELECT owner FROM ALL_INDEXES WHERE index_name = 'IDX_BOOK_ID'");

        assertTrue(indexOwner.next());

        String owner = indexOwner.getString("owner");

        assertEquals("LBCAT2", owner);

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Test
    public void viewCreatedOnCorrectSchema() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(this.viewOnSchemaChangeLog);
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        Statement queryIndex = ((JdbcConnection) this.getDatabase().getConnection()).getUnderlyingConnection().createStatement();

        ResultSet indexOwner = queryIndex.executeQuery("SELECT owner FROM ALL_VIEWS WHERE view_name = 'V_BOOK2'");

        assertTrue(indexOwner.next());

        String owner = indexOwner.getString("owner");

        assertEquals("LBCAT2", owner);

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Test
    public void smartDataLoad() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase("changelogs/common/smartDataLoad.changelog.xml");
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Override
    @Test
    public void testDiffExternalForeignKeys() throws Exception {
        //cross-schema security for oracle is a bother, ignoring test for now
    }

    @Test
    public void verifyIndexIsCreatedWhenAssociatedWithPropertyIsSetAsForeignKey() throws DatabaseException {
        clearDatabase();
        try {
            Database database = getDatabase();
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, indexWithAssociatedWithChangeLog);
            commandScope.execute();

            final CommandScope snapshotScope = new CommandScope("snapshot");
            snapshotScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            snapshotScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, "json");
            CommandResults results = snapshotScope.execute();
            DatabaseSnapshot snapshot = (DatabaseSnapshot) results.getResult("snapshot");
            Index index = snapshot.get(new Index("idx_test_oracle"));
            Assert.assertNotNull(index);
        } catch (Exception e) {
            Assert.fail("Should not fail. Reason: " + e.getMessage());
        } finally {
            clearDatabase();
        }
    }

    @Test
    public void testChangeLogGenerationForTableWithGeneratedColumn() throws Exception {
        assumeNotNull(getDatabase());
        clearDatabase();
        String textToTest = "GENERATED ALWAYS AS (QTY*PRICE)";

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new RawParameterizedSqlStatement(
            String.format("CREATE TABLE GENERATED_COLUMN_TEST(QTY INT, PRICE INT, TOTALVALUE INT %s)", textToTest)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
            .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
            .setOutput(baos)
            .execute();

        String generatedChangeLog = baos.toString();
        assertTrue("Text '" + textToTest + "' not found in generated change log: " + generatedChangeLog, generatedChangeLog.contains(textToTest));
    }

    @Test
    public void verifySlashReplacementOnStrictMode() throws DatabaseException {
        assumeNotNull(getDatabase());
        try {
            Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
                Database database = getDatabase();
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, strictChangelog);
                commandScope.execute();
            });
        } catch (Exception e) {
            Assert.fail("Should not fail. Reason: " + e.getMessage());
        }
    }

    @Test
    public void snapshotNotNullConstraintsOfConnectedUserSchema() throws Exception {
        assumeNotNull(getDatabase());
        clearDatabase();
        try {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase());
            String schema = getDatabase().getDefaultSchemaName();
            // The USER_* views are only used for the schema of the connected user
            Assert.assertEquals(schema, executor.queryForObject(
                    new RawParameterizedSqlStatement("SELECT SYS_CONTEXT('USERENV', 'SESSION_USER') FROM DUAL"), String.class));

            executor.execute(new RawParameterizedSqlStatement("CREATE TABLE NOT_NULL_TEST (ID INT, " +
                    "NAMED_NN INT CONSTRAINT NOT_NULL_TEST_NN NOT NULL, " +
                    "DISABLED_NN INT CONSTRAINT NOT_NULL_TEST_DIS NOT NULL DISABLE, " +
                    "NOVALIDATE_NN INT, " +
                    "NULLABLE_COL INT)"));
            executor.execute(new RawParameterizedSqlStatement("INSERT INTO NOT_NULL_TEST (ID, NAMED_NN) VALUES (1, 1)"));
            executor.execute(new RawParameterizedSqlStatement("ALTER TABLE NOT_NULL_TEST MODIFY NOVALIDATE_NN NOT NULL ENABLE NOVALIDATE"));

            // A schema snapshot reads the not-null constraints of all tables in one query
            DatabaseSnapshot schemaSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
                    new CatalogAndSchema(null, schema), getDatabase(), new SnapshotControl(getDatabase()));
            Table table = schemaSnapshot.get(new Table(null, schema, "NOT_NULL_TEST"));
            Assert.assertNotNull(table);

            Column named = getColumn(table, "NAMED_NN");
            Assert.assertFalse(named.isNullable());
            Assert.assertEquals("NOT_NULL_TEST_NN", named.getAttribute("notNullConstraintName", String.class));

            Column disabled = getColumn(table, "DISABLED_NN");
            Assert.assertFalse(disabled.isNullable());
            Assert.assertFalse(disabled.getValidateNullable());
            Assert.assertEquals("NOT_NULL_TEST_DIS", disabled.getAttribute("notNullConstraintName", String.class));

            Column novalidate = getColumn(table, "NOVALIDATE_NN");
            Assert.assertFalse(novalidate.isNullable());
            Assert.assertFalse(novalidate.getValidateNullable());

            Assert.assertTrue(getColumn(table, "NULLABLE_COL").isNullable());

            // Liquibase's own tables are snapshotted one at a time, with a query filtered on the table name
            Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(getDatabase()).init();
            Table changeLogTable = SnapshotGeneratorFactory.getInstance().createSnapshot(
                    new Table(null, schema, getDatabase().getDatabaseChangeLogTableName()), getDatabase());
            Assert.assertNotNull(changeLogTable);
            Assert.assertFalse(getColumn(changeLogTable, "ID").isNullable());
            Assert.assertTrue(getColumn(changeLogTable, "COMMENTS").isNullable());
        } finally {
            clearDatabase();
        }
    }

    private static Column getColumn(Table table, String columnName) {
        Column column = table.getColumn(columnName);
        Assert.assertNotNull("Column " + columnName + " missing from the snapshot of " + table.getName(), column);
        return column;
    }

    @Override
    protected void assertThatSnapshotReportsAllObjectTypes(DatabaseSnapshot snapshot) {
        super.assertThatSnapshotReportsAllObjectTypes(snapshot);
        
        assertThat(snapshot).containsObject(Table.class, table ->
            "TEST_FLOAT_PRECISION".equalsIgnoreCase(table.getName()), table -> {
                Assert.assertEquals(Integer.valueOf(54), table.getColumn("floatColumn54").getType().getColumnSize());
                Assert.assertEquals(Integer.valueOf(126), table.getColumn("floatColumn126").getType().getColumnSize());
            });
    }

}
