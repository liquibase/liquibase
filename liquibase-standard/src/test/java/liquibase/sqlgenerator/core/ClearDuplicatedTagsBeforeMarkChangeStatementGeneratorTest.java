package liquibase.sqlgenerator.core;

import liquibase.change.core.InsertDataChange;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.MarkChangeSetRanStatement;
import org.junit.Test;

import static liquibase.sqlgenerator.SqlGenerator.EMPTY_SQL;
import static org.junit.Assert.assertEquals;

public class ClearDuplicatedTagsBeforeMarkChangeStatementGeneratorTest {

    @Test
    public void whenTagChange_returnClearingStatement() {
        // given
        ClearDuplicatedTagsBeforeMarkChangeStatementGenerator generator = new ClearDuplicatedTagsBeforeMarkChangeStatementGenerator();
        MarkChangeSetRanStatement statement = new MarkChangeSetRanStatement(createChangeSetWithTag(), ChangeSet.ExecType.EXECUTED);
        Database database = new PostgresDatabase();

        // when
        Sql[] sqls = generator.generateSql(statement, database);

        // then
        assertEquals("UPDATE databasechangelog SET TAG = NULL WHERE TAG = 'tag1.0'", sqls[0].toSql());
    }

    @Test
    public void whenNullTag_returnEmptyStatement() {
        // given
        ClearDuplicatedTagsBeforeMarkChangeStatementGenerator generator = new ClearDuplicatedTagsBeforeMarkChangeStatementGenerator();
        MarkChangeSetRanStatement statement = new MarkChangeSetRanStatement(createChangeSetWithoutTag(), ChangeSet.ExecType.EXECUTED);
        Database database = new PostgresDatabase();

        // when
        Sql[] sqls = generator.generateSql(statement, database);

        // then
        assertEquals(EMPTY_SQL, sqls);
    }

    @Test
    public void whenChangeSetEmptyInStatement_validationFails() {
        // given
        ClearDuplicatedTagsBeforeMarkChangeStatementGenerator generator = new ClearDuplicatedTagsBeforeMarkChangeStatementGenerator();
        MarkChangeSetRanStatement statement = new MarkChangeSetRanStatement(null, ChangeSet.ExecType.EXECUTED);

        // when
        ValidationErrors validate = generator.validate(statement, new PostgresDatabase(), null);

        // then
        assertEquals(1, validate.getErrorMessages().size());
    }

    @Test
    public void whenChangeSetFailed_returnEmptyStatement() {
        // given
        ClearDuplicatedTagsBeforeMarkChangeStatementGenerator generator = new ClearDuplicatedTagsBeforeMarkChangeStatementGenerator();
        MarkChangeSetRanStatement statement = new MarkChangeSetRanStatement(createChangeSetWithoutTag(), ChangeSet.ExecType.FAILED);
        Database database = new PostgresDatabase();

        // when
        Sql[] sqls = generator.generateSql(statement, database);

        // then
        assertEquals(EMPTY_SQL, sqls);
    }

    @Test
    public void whenChangeSetSkipped_returnEmptyStatement() {
        // given
        ClearDuplicatedTagsBeforeMarkChangeStatementGenerator generator = new ClearDuplicatedTagsBeforeMarkChangeStatementGenerator();
        MarkChangeSetRanStatement statement = new MarkChangeSetRanStatement(createChangeSetWithoutTag(), ChangeSet.ExecType.SKIPPED);
        Database database = new PostgresDatabase();

        // when
        Sql[] sqls = generator.generateSql(statement, database);

        // then
        assertEquals(EMPTY_SQL, sqls);
    }

    private ChangeSet createChangeSetWithTag() {
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("/patch/changeLog.xml");
        ChangeSet changeSet = new ChangeSet("id", "author", false, false, "/path/changeSet.xml", "", "", databaseChangeLog);
        TagDatabaseChange tagDatabaseChange = new TagDatabaseChange();
        tagDatabaseChange.setTag("tag1.0");
        changeSet.addChange(tagDatabaseChange);
        databaseChangeLog.addChangeSet(changeSet);
        return changeSet;
    }

    private ChangeSet createChangeSetWithoutTag() {
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("/patch/changeLog.xml");
        ChangeSet changeSet = new ChangeSet("id", "author", false, false, "/path/changeSet.xml", "", "", databaseChangeLog);
        changeSet.addChange(new InsertDataChange());
        databaseChangeLog.addChangeSet(changeSet);
        return changeSet;
    }

}
