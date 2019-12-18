package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.AddDefaultValueStatement;
import org.junit.Test;

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
}
