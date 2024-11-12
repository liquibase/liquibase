package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.TagDatabaseStatement;
import org.junit.Test;

import static liquibase.sqlgenerator.SqlGenerator.EMPTY_SQL;
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
    public void whenNullTag_returnEmptyStatement() {
        // given
        ClearDuplicatedTagsBeforeTagStatementGenerator generator = new ClearDuplicatedTagsBeforeTagStatementGenerator();
        TagDatabaseStatement statement = new TagDatabaseStatement(null);
        Database database = new PostgresDatabase();

        // when
        Sql[] sqls = generator.generateSql(statement, database);

        // then
        assertEquals(EMPTY_SQL, sqls);
    }

}
