package liquibase

import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.RanChangeSet
import liquibase.command.util.CommandUtil
import liquibase.database.Database
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.snapshot.SnapshotGeneratorFactory
import spock.lang.Shared
import spock.lang.Specification

import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.junit.Assert.*

@LiquibaseIntegrationTest
class LiquibaseTestIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def setupSpec() {
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogForUnmanagedDatabase() throws Exception {
        when:
        Liquibase liquibase = createUnmanagedDatabase(h2);
        assertFalse(hasDatabaseChangeLogTable(liquibase));

        liquibase.changeLogSync("");

        then:
        assert hasDatabaseChangeLogTable(liquibase);
        assertTags(liquibase, "1.0", "1.1", "2.0");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogToTagForUnmanagedDatabase() throws Exception {
        when:
        Liquibase liquibase = createUnmanagedDatabase(h2);

        then:
        assert !hasDatabaseChangeLogTable(liquibase)

        when:
        liquibase.changeLogSync("1.1", "");

        then:
        assert hasDatabaseChangeLogTable(liquibase);
        assertTags(liquibase, "1.0", "1.1");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogForManagedDatabase() throws Exception {
        when:
        Liquibase liquibase = createDatabaseAtTag(h2.getDatabaseFromFactory(), "1.0");

        then:
        assert hasDatabaseChangeLogTable(liquibase)

        when:
        liquibase.changeLogSync("");

        then:
        assertTags(liquibase, "1.0", "1.1", "2.0");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogToTagForManagedDatabase() throws Exception {
        when:
        Liquibase liquibase = createDatabaseAtTag(h2.getDatabaseFromFactory(), "1.0");
        then:
        assert hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("1.1", "");

        then:
        assertTags(liquibase, "1.0", "1.1");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogSqlForUnmanagedDatabase() throws Exception {
        when:
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createUnmanagedDatabase(h2);

        then:
        assert !hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("", writer);

        then:
        assert !hasDatabaseChangeLogTable(liquibase);
        assertSqlOutputAppliesTags(writer.toString(), "1.0", "1.1", "2.0");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogToTagSqlForUnmanagedDatabase() throws Exception {
        when:
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createUnmanagedDatabase(h2);

        then:
        assert !hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("1.1", "", writer);

        then:
        !hasDatabaseChangeLogTable(liquibase);
        assertSqlOutputAppliesTags(writer.toString(), "1.0", "1.1");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogSqlForManagedDatabase() throws Exception {
        when:
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createDatabaseAtTag(h2.getDatabaseFromFactory(), "1.0");

        then:
        assert hasDatabaseChangeLogTable(liquibase);

        when:
        liquibase.changeLogSync("", writer);

        then:
        assertSqlOutputAppliesTags(writer.toString(), "1.1", "2.0");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def syncChangeLogToTagSqlForManagedDatabase() throws Exception {
        when:
        StringWriter writer = new StringWriter();

        Liquibase liquibase = createDatabaseAtTag(h2.getDatabaseFromFactory(), "1.0");

        then:
        assertTrue(hasDatabaseChangeLogTable(liquibase));

        when:
        liquibase.changeLogSync("1.1", "", writer);

        then:
        assertSqlOutputAppliesTags(writer.toString(), "1.1");

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "validate checksums from ran changesets have all been reset"() {
        when:
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2.getDatabaseFromFactory())
        liquibase.update()
        liquibase.clearCheckSums()

        then:
        List<RanChangeSet> ranChangeSets = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(liquibase.getDatabase()).getRanChangeSets()
        assert ranChangeSets.get(0).getLastCheckSum() == null

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    private Liquibase createUnmanagedDatabase(DatabaseTestSystem database) throws SQLException, LiquibaseException {
        String createTableSql = "CREATE TABLE PUBLIC.TABLE_A (ID INTEGER);";

        PreparedStatement stmt = database.getConnection().prepareStatement(createTableSql)
        try {
            stmt.execute();
        } finally {
            stmt.close()
        }

        return new Liquibase("liquibase/tagged-changelog.xml", new ClassLoaderResourceAccessor(), database.getDatabaseFromFactory());
    }

    private Liquibase createDatabaseAtTag(Database database, String tag) throws LiquibaseException {
        Liquibase liquibase = new Liquibase("liquibase/tagged-changelog.xml", new ClassLoaderResourceAccessor(),
                database);
        liquibase.update(tag, "");
        return liquibase;
    }

    private boolean hasDatabaseChangeLogTable(Liquibase liquibase) throws DatabaseException {
        return SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(liquibase.database);
    }

    private void assertTags(Liquibase liquibase, String... expectedTags) throws DatabaseException {
        def actualTags = []
        for (def ranChangeset : liquibase.database.getRanChangeSetList()) {
            if (ranChangeset.getTag() != null) {
                actualTags.add(ranChangeset.getTag())
            }
        }

        assertEquals(Arrays.asList(expectedTags), actualTags);
    }

    private void assertSqlOutputAppliesTags(String output, String... expectedTags) throws IOException {
        String insertTagH2SqlTemplate =
                "INSERT INTO PUBLIC\\.DATABASECHANGELOG \\(.*, DESCRIPTION,.*, TAG\\) VALUES \\(.*, 'tagDatabase',.*, '%s'\\);";

        List<Pattern> patterns = []

        for (def tag : expectedTags) {
            patterns.add(Pattern.compile(String.format(insertTagH2SqlTemplate, tag)))
        }

        BufferedReader reader = new BufferedReader(new StringReader(output))
        try {
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null && index < patterns.size()) {
                Matcher matcher = patterns.get(index).matcher(line);
                if (matcher.matches()) {
                    index++;
                }
            }
            assertTrue(index > 0 && index == patterns.size());
        } finally {
            reader.close()
        }
    }
}
