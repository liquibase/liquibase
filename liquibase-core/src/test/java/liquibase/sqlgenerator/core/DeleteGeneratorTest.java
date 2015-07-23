package liquibase.sqlgenerator.core;

import static org.junit.Assert.*;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.DeleteStatement;

import org.junit.Test;

public class DeleteGeneratorTest {
    @Test
    public void testGenerateSql() {
        // given
        DeleteStatement statement = new DeleteStatement(null, null, "DATABASECHANGELOG");
        statement.setWhere(":name = :value");
        statement.addWhereColumnName("FILENAME");
        statement.addWhereParameter("server_principals/BUILTIN$Administrators.xml");

        Database database = new MSSQLDatabase();
        DeleteGenerator generator = new DeleteGenerator();

        // when
        Sql[] sqls = generator.generateSql(statement, database, null);

        // then
        assertEquals(
                "DELETE FROM [DATABASECHANGELOG] WHERE [FILENAME] = 'server_principals/BUILTIN$Administrators.xml'",
                sqls[0].toSql());
    }
}
