package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.TagDatabaseStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClearDuplicatedTagsBeforeTagStatementGeneratorTest {

    @Test
    public void whenTagStatement_returnClearingStatement() {
        // given
        ClearDuplicatedTagsBeforeTagStatementGenerator generator = new ClearDuplicatedTagsBeforeTagStatementGenerator();
        TagDatabaseStatement statement = new TagDatabaseStatement("tag1.0");
        Database database = new PostgresDatabase();

        // when
        Sql[] sqls = generator.generateSql(statement, database);

        // then
        assertEquals("UPDATE databasechangelog SET TAG = NULL WHERE TAG = 'tag1.0'", sqls[0].toSql());
    }

    @Test
    public void whenNullTagInStatement_validationFails() {
        // given
        ClearDuplicatedTagsBeforeTagStatementGenerator generator = new ClearDuplicatedTagsBeforeTagStatementGenerator();
        TagDatabaseStatement statement = new TagDatabaseStatement(null);
        Database database = new PostgresDatabase();

        // when
        ValidationErrors validate = generator.validate(statement, database, null);

        // then
        assertEquals(1, validate.getErrorMessages().size());
    }

}
