package liquibase.serializer.core.formattedsql;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FormattedSqlChangeLogSerializerTest {

    private FormattedSqlChangeLogSerializer serializer;
    private ChangeSet changeSet;
    private Database database;

    @Before
    public void setUp() {
        serializer = new FormattedSqlChangeLogSerializer();
        changeSet = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.h2.sql", null, null, null);
        database = new H2Database();
    }

    @Test
    public void serialize_changeSetWithNoChanges() {
        String serialized = serializer.serialize(changeSet, true);
        assertEquals(serialized, ("-- changeset testAuthor:1\n"));
    }

    @Test
    public void serialize_changeSetWithContextAndLabels() {
        ChangeSet changeSetWithContextAndLabels = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.h2.sql", null, null, null);
        changeSetWithContextAndLabels.setLabels(new Labels("label1"));
        changeSetWithContextAndLabels.setContextFilter(new ContextExpression("context1"));
        String serialized = serializer.serialize(changeSetWithContextAndLabels, true);
        assertEquals(serialized, ("-- changeset testAuthor:1 labels: \"label1\" contextFilter: \"context1\"\n"));
    }

    @Test
    public void getValidFileExtensions_returnsSqlExtension() {
        String[] expectedExtensions = new String[] {"sql"};
        assertArrayEquals(expectedExtensions, serializer.getValidFileExtensions());
    }

    @Test
    public void serialize_changeSetWithChanges() {
        AddAutoIncrementChange statement = new AddAutoIncrementChange();
        statement.setTableName("table_name");
        statement.setColumnName("column_name");
        statement.setColumnDataType("int");
        changeSet.addChange(statement);
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String expectedSql = "-- changeset testAuthor:1\n" + sqls[0].toSql() + ";\n";

        String serialized = serializer.serialize(changeSet, true);
        assertEquals(expectedSql, serialized);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void serialize_changeSetWithNoFilePath() {
        changeSet.setFilePath(null);
        serializer.serialize(changeSet, true);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void serialize_changeSetWithInvalidFilePath() {
        changeSet.setFilePath("invalidFilePath");
        serializer.serialize(changeSet, true);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void serialize_changeSetWithNoDatabase() {
        ChangeSet changeSetWithInvalidDb = new ChangeSet("1", "testAuthor",
                false, false, "path/to/changeLogFile.LALALA.sql", null, null, null);
        serializer.serialize(changeSetWithInvalidDb, true);
    }

}
