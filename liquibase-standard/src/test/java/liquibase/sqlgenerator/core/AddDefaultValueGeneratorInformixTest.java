package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class AddDefaultValueGeneratorInformixTest {
    private AddDefaultValueGeneratorInformix generator = new AddDefaultValueGeneratorInformix();

    @Test
    public void generateSql() {
        AddDefaultValueStatement statement = new AddDefaultValueStatement("catalog1", "schema2", "table3", "column4", "type5", "default-value-6");
        Database database = new InformixDatabase();

        Sql[] sql = generator.generateSql(statement, database, null);

        assertEquals(1, sql.length);
        assertEquals("ALTER TABLE catalog1:schema2.table3 MODIFY (column4 TYPE5 DEFAULT 'default-value-6')", sql[0].toSql());
    }

    @Test
    public void shouldGenerateINT8TypeForBIGINTAndDefaultValue() throws Exception {
        AddDefaultValueGeneratorInformix informix = new AddDefaultValueGeneratorInformix();

        AddDefaultValueStatement statement = new AddDefaultValueStatement(null, null, "tbl1", "id", "BIGINT", 1);
        InformixDatabase database = new InformixDatabase();
        SortedSet<SqlGenerator> sqlGenerators = new TreeSet<SqlGenerator>();
        SqlGeneratorChain sqlGenerationChain = new SqlGeneratorChain(sqlGenerators);
        Sql[] sqls = informix.generateSql(statement, database, sqlGenerationChain);
        assertEquals("ALTER TABLE tbl1 MODIFY (id INT8 DEFAULT 1)", sqls[0].toSql());

    }
}
