package liquibase.dbtest.pgsql;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeSet;
import liquibase.command.CommandScope;
import liquibase.command.core.GenerateChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.ValidationFailedException;
import liquibase.executor.ExecutorService;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.test.JUnitResourceAccessor;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

public class PostgreSQLIntegrationTest extends AbstractIntegrationTest {

    private String dependenciesChangeLog;
    private String blobChangeLog;
    private static DatabaseTestSystem localTestSystem;

    public PostgreSQLIntegrationTest() throws Exception {
        super("pgsql", DatabaseFactory.getInstance().getDatabase("postgresql"));
        dependenciesChangeLog = "changelogs/pgsql/complete/testFkPkDependencies.xml";
        blobChangeLog = "changelogs/pgsql/complete/testBlob.changelog.xml";
        localTestSystem = testSystem;
    }

    @Test
    public void testDependenciesInGenerateChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(this.dependenciesChangeLog);
        clearDatabase();

        try {
            liquibase.update(new Contexts());
            Database database = liquibase.getDatabase();
            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, null, new CompareControl());
            DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult,
                new DiffOutputControl(false, false, false, null));
            List<ChangeSet> changeSets = changeLogWriter.generateChangeSets();
            Assert.assertTrue(changeSets.size() > 0);
            ChangeSet addPrimaryKeyChangeSet =
                changeSets.stream()
                          .filter(changeSet -> changeSet.getChanges().get(0) instanceof AddPrimaryKeyChange)
                          .findFirst()
                          .orElse(null);
            Assert.assertNull(addPrimaryKeyChangeSet);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Test
    public void testBlobTypesChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase = createLiquibase(this.blobChangeLog);
        clearDatabase();
        try {
            liquibase.update();
            List<Map<String, ?>>  data = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(
                            new RawParameterizedSqlStatement("SELECT pg_column_size(content_bytea) as BYTEASIZE, pg_column_size(lo_get(content_oid)) as OIDSIZE FROM  public.blobtest"));
            Assert.assertNotNull(data.get(0));
            Assert.assertTrue(((Integer)data.get(0).get("BYTEASIZE")) > 0);
            Assert.assertEquals(data.get(0).get("BYTEASIZE"), data.get(0).get("OIDSIZE"));
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Test
    public void testMissingDataGenerator() throws Exception {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("CREATE TABLE \"FIRST_TABLE\" (\"ID\" INT, \"NAME\" VARCHAR(20), \"LAST_NAME\" VARCHAR(20) DEFAULT 'Snow', " +
                                                    "\"AGE\" INT DEFAULT 25, \"REGISTRATION_DATE\" date DEFAULT TO_DATE('2014-08-11', 'YYYY-MM-DD'), " +
                                                    "\"COMPVALCOL\" INT DEFAULT 1*22)"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("CREATE TABLE \"SECOND_TABLE\" (\"ID\" INT, \"NAME\" VARCHAR(20))"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("ALTER TABLE \"FIRST_TABLE\" ADD CONSTRAINT \"FIRST_TABLE_PK\" PRIMARY KEY (\"ID\")"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("ALTER TABLE \"SECOND_TABLE\" ADD CONSTRAINT \"FIRST_TABLE_FK\" FOREIGN KEY (\"ID\") REFERENCES \"FIRST_TABLE\"(\"ID\")"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("CREATE INDEX \"IDX_FIRST_TABLE\" ON \"FIRST_TABLE\"(\"NAME\")"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("INSERT INTO \"FIRST_TABLE\"(\"ID\", \"NAME\") VALUES (1, 'JOHN')"));
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("INSERT INTO \"FIRST_TABLE\"(\"ID\", \"NAME\", \"LAST_NAME\", \"AGE\", \"REGISTRATION_DATE\", \"COMPVALCOL\") VALUES (2, 'JEREMY', 'IRONS', 71, TO_DATE('2020-04-01', 'YYYY-MM-DD'), 2*11 )"));
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("INSERT INTO \"SECOND_TABLE\"(\"ID\", \"NAME\") VALUES (1, 'JOHN')"));
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                  .execute(
                          new RawParameterizedSqlStatement("INSERT INTO \"SECOND_TABLE\"(\"ID\", \"NAME\") VALUES (2, 'JEREMY')"));
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());

        DiffToChangeLog changeLogWriter =
                new DiffToChangeLog(diffResult,
                   new DiffOutputControl(false, false, false, null));
        List<ChangeSet> changeSets = changeLogWriter.generateChangeSets();
        boolean found = false;
        for (ChangeSet changeSet : changeSets) {
            List<Change> changes = changeSet.getChanges();
            for (Change change : changes) {
                if (! (change instanceof CreateTableChange)) {
                    continue;
                }
                found = ((CreateTableChange) change).getTableName().equals("FIRST_TABLE");
                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        Assert.assertTrue("There should be a table named \"FIRST_TABLE\"", found);
    }

    @Test
    public void testCreateIndexUsingFunctions() throws Exception {
        String function = "UPPER";
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .execute(
                        new RawParameterizedSqlStatement("CREATE TABLE INDEX_TEST (ID INT, NAME VARCHAR(20))"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .execute(
                        new RawParameterizedSqlStatement(String.format("CREATE INDEX INDEX_TEST_IDX ON INDEX_TEST(ID, %s(NAME)) WHERE ID > 0", function)));
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());

        DiffToChangeLog changeLogWriter =
                new DiffToChangeLog(diffResult,
                        new DiffOutputControl(false, false, false, null));
        List<ChangeSet> changeSets = changeLogWriter.generateChangeSets();
        boolean found = false;
        for (ChangeSet changeSet : changeSets) {
            List<Change> changes = changeSet.getChanges();
            for (Change change : changes) {
                if (! (change instanceof CreateIndexChange)) {
                    continue;
                }
                found = ((CreateIndexChange) change).getColumns().stream().anyMatch(c -> c.getName().toUpperCase().contains(function));
                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        Assert.assertTrue("There should be a sequence column starting with function \"" + function + "\"", found);
    }

    @Test
    public void correctlyHandlesAutoIncrementSequences() throws Exception {
        Database database = getDatabase();
        boolean supportsIdentity = database.getDatabaseMajorVersion() > 9;

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .execute(
                        new RawParameterizedSqlStatement("CREATE TABLE serial_table (id serial)"));


        if (supportsIdentity) {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                    .execute(
                            new RawParameterizedSqlStatement("CREATE TABLE autoinc_table (id int generated by default as identity)"));
        }


        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .execute(
                        new RawParameterizedSqlStatement("CREATE TABLE owned_by_table (id int)"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .execute(
                        new RawParameterizedSqlStatement("create sequence seq_owned owned by owned_by_table.id"));

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .execute(
                        new RawParameterizedSqlStatement("create sequence seq_unowned"));


        SnapshotGeneratorFactory.resetAll();
        SnapshotGeneratorFactory factory = SnapshotGeneratorFactory.getInstance();
        DatabaseSnapshot snapshot = factory.createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(getDatabase()));

        List<String> seenSequences = new ArrayList<>();
        for (Sequence sequence : snapshot.get(Sequence.class)) {
            seenSequences.add(sequence.getName());
        }
        Collections.sort(seenSequences);

        assertEquals(2, seenSequences.size());
        assertEquals("seq_owned", seenSequences.get(0));
        assertEquals("seq_unowned", seenSequences.get(1));

        assert snapshot.get(new Table(null, null, "serial_table")).getColumn("id").isAutoIncrement();
        assert !supportsIdentity || snapshot.get(new Table(null, null, "autoinc_table")).getColumn("id").isAutoIncrement();
        assert !snapshot.get(new Table(null, null, "owned_by_table")).getColumn("id").isAutoIncrement();

    }

    @Test
    public void testGeneratedColumn() throws Exception {
        assumeNotNull(getDatabase());
        assumeTrue(getDatabase().getDatabaseMajorVersion() >= 12);
        clearDatabase();
        String textToTest = "GENERATED ALWAYS AS (height_cm / 2.54) STORED";

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .execute(
                        new RawParameterizedSqlStatement(String.format("CREATE TABLE generated_test (height_cm numeric, height_stored numeric %s)", textToTest)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
                    .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                    .setOutput(baos)
                    .execute();

            assertTrue(baos.toString().contains(textToTest));

    }

    @Test
    public void testGeneratedClobColumn() throws Exception {
        assumeNotNull(getDatabase());
        assumeTrue(getDatabase().getDatabaseMajorVersion() >= 12);
        clearDatabase();
        String textToTest = "GENERATED ALWAYS AS ((surname || ', '::text) || forename) STORED";

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
            .execute(new RawParameterizedSqlStatement(String.format(
                    "CREATE TABLE generated_text_test (fullname text %s, surname text, forename text)",
                    textToTest)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
            .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
            .setOutput(baos)
            .execute();

        assertTrue(baos.toString().contains(textToTest));
    }

    @Test
    public void validateUserCanOnlyAccessTablesFromSchemasAllowedToRead() throws Exception {
        assumeNotNull(this.getDatabase());

        //Set up changelog to be deployed to have some tables in the public schema
        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);

        //Create a new table with a serial type field on a new schema for a new user to test it can only get access to the created table
        ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                "DROP SCHEMA IF EXISTS TEST_SCHEMA CASCADE;" +
                        "DROP USER IF EXISTS TEST_USER;" +
                        "CREATE SCHEMA TEST_SCHEMA;" +
                        "CREATE USER TEST_USER WITH PASSWORD '1234';" +
                        "CREATE TABLE TEST_SCHEMA.permissionDeniedTable(id serial, name varchar(50));" +
                        "GRANT ALL ON ALL TABLES IN SCHEMA public TO TEST_USER"
        );
        getDatabase().commit();

        String url = getDatabase().getConnection().getURL();
        Database newDatabase = DatabaseFactory.getInstance().openDatabase(url, "test_user", "1234", null, new JUnitResourceAccessor());
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new CatalogAndSchema(null, "test_schema"), newDatabase, new SnapshotControl(newDatabase));

        Set<Table> tableList = snapshot.get(Table.class);

        assertEquals(tableList.size(), 1);
        assertEquals(tableList.iterator().next().getName(), "permissiondeniedtable");
    }

    @Test
    public void runYamlChangelog() throws Exception {
        if (getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        //run again to test changelog testing logic
        liquibase = createLiquibase("changelogs/yaml/create.procedure.back.compatibility.changelog.yaml");
        liquibase.setChangeLogParameter("loginuser", testSystem.getUsername());

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }


    }
}
