package liquibase.change;

import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.test.JUnitResourceAccessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LoadDataOtherTest {

  LoadDataChange loadDataChange = new LoadDataChange();

  @Test
  public void testOtherColumnLoadData() throws Exception {
    loadDataChange.setResourceAccessor(new JUnitResourceAccessor());
    List<LoadDataColumnConfig> columnConfigs = new ArrayList<>();
    LoadDataColumnConfig col1 = new LoadDataColumnConfig();
    col1.setHeader("int_col");
    col1.setName("int_col");
    col1.setType("NUMERIC");
    columnConfigs.add(col1);
    LoadDataColumnConfig col2 = new LoadDataColumnConfig();
    col2.setHeader("str_col");
    col2.setName("str_col");
    col2.setType("STRING");
    columnConfigs.add(col2);
    LoadDataColumnConfig col3 = new LoadDataColumnConfig();
    col3.setHeader("enum_col");
    col3.setName("enum_col");
    col3.setType("OTHER");
    columnConfigs.add(col3);
    loadDataChange.setColumns(columnConfigs);
    loadDataChange.setFile("liquibase/change/core/enum-data.csv");
    SqlStatement[] statements = loadDataChange.generateStatements(new PostgresDatabase());
    Assert.assertEquals(1, statements.length);
    Assert.assertNotNull(statements[0]);
    Assert.assertEquals(3, ((InsertSetStatement) statements[0]).getStatementsArray().length);
    Assert.assertEquals("EnumValOne", ((InsertSetStatement) statements[0]).getStatementsArray()[0].getColumnValues().get("enum_col"));
    Assert.assertEquals("EnumValTwo", ((InsertSetStatement) statements[0]).getStatementsArray()[1].getColumnValues().get("enum_col"));
    Assert.assertEquals("NULL", ((InsertSetStatement) statements[0]).getStatementsArray()[2].getColumnValues().get("enum_col"));
  }
}
