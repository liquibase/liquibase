package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateIndexStatement;
import org.junit.Assert;
import org.junit.Test;

public class CreateIndexGeneratorMySQLTest {

    private static final String EXPRESSION = "cast(json_extract(`payload`,_latin1'$.regionId') as unsigned)";

    @Test
    public void generateSql_computedColumnOnMySQL_expressionIsWrappedInParentheses() {
        // MySQL requires a functional key part to be enclosed in its own pair of parentheses
        CreateIndexStatement statement = newStatement(newColumn(EXPRESSION, true));

        Sql[] result = generateSql(statement, new MySQLDatabase());

        Assert.assertEquals("CREATE INDEX idx_payload_region ON example((" + EXPRESSION + "))", result[0].toSql());
    }

    @Test
    public void generateSql_computedAndPlainColumnsOnMySQL_onlyComputedIsWrapped() {
        CreateIndexStatement statement = newStatement(newColumn("id", null), newColumn("lower(`name`)", true));

        Sql[] result = generateSql(statement, new MySQLDatabase());

        Assert.assertEquals("CREATE INDEX idx_payload_region ON example(id, (lower(`name`)))", result[0].toSql());
    }

    @Test
    public void generateSql_nonComputedColumnOnMySQL_isNotWrapped() {
        CreateIndexStatement statement = newStatement(newColumn("id", false));

        Sql[] result = generateSql(statement, new MySQLDatabase());

        Assert.assertEquals("CREATE INDEX idx_payload_region ON example(id)", result[0].toSql());
    }

    @Test
    public void generateSql_computedColumnOnMariaDB_isLeftUntouched() {
        // MariaDB has no functional key parts, so its syntax must not change
        CreateIndexStatement statement = newStatement(newColumn("lower(`name`)", true));

        Sql[] result = generateSql(statement, new MariaDBDatabase());

        Assert.assertEquals("CREATE INDEX idx_payload_region ON example(lower(`name`))", result[0].toSql());
    }

    private static CreateIndexStatement newStatement(AddColumnConfig... columns) {
        return new CreateIndexStatement("idx_payload_region", null, null, "example", false, null, columns);
    }

    private static AddColumnConfig newColumn(String name, Boolean computed) {
        AddColumnConfig column = new AddColumnConfig();
        column.setName(name);
        column.setComputed(computed);
        return column;
    }

    private static Sql[] generateSql(CreateIndexStatement statement, Database database) {
        return new CreateIndexGenerator().generateSql(statement, database, null);
    }
}
