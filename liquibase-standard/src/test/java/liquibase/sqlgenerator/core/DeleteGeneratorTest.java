package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.DeleteStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeleteGeneratorTest {
    @Test
    public void testGenerateSql() {
        // given
        DeleteStatement statement = new DeleteStatement(null, null, "DATABASECHANGELOG");
        statement.setWhere(":name = :value AND :name = :value AND :name = :value");
        statement.addWhereColumnName("ID");
        statement.addWhereColumnName("AUTHOR");
        statement.addWhereColumnName("FILENAME");
        statement.addWhereParameter("1");
        statement.addWhereParameter("a");
        statement.addWhereParameter("server_principals/BUILTIN$Administrators.xml");

        Database database = new MSSQLDatabase();
        DeleteGenerator generator = new DeleteGenerator();

        // when
        Sql[] sqls = generator.generateSql(statement, database, null);

        // then
        assertEquals(
            "DELETE FROM DATABASECHANGELOG " +
                "WHERE ID = '1' " +
                "AND AUTHOR = 'a' " +
                "AND FILENAME = 'server_principals/BUILTIN$Administrators.xml'",
                sqls[0].toSql());
    }
}
