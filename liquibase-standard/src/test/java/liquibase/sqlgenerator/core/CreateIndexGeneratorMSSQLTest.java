package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateIndexStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreateIndexGeneratorMSSQLTest {

    @Test
    public void generateSql_tablespaceAlreadyInBrackets_isNotQuotedAgain() {
        // changelogs written for versions before 5.0.2 spell the filegroup as [PRIMARY]
        assertEquals("CREATE INDEX IX_EXAMPLE ON example(id) ON [PRIMARY]", generateSql("[PRIMARY]"));
    }

    @Test
    public void generateSql_reservedWordTablespace_isQuoted() {
        assertEquals("CREATE INDEX IX_EXAMPLE ON example(id) ON [PRIMARY]", generateSql("PRIMARY"));
    }

    private static String generateSql(String tablespace) {
        AddColumnConfig column = new AddColumnConfig();
        column.setName("id");
        CreateIndexStatement statement = new CreateIndexStatement("IX_EXAMPLE", null, null, "example", false, null, column);
        statement.setTablespace(tablespace);
        Sql[] result = new CreateIndexGenerator().generateSql(statement, new MSSQLDatabase(), null);
        return result[0].toSql();
    }
}
