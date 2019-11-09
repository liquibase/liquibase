package liquibase.sqlgenerator.core;

import liquibase.database.core.InformixDatabase;
import liquibase.statement.core.InsertOrUpdateStatement;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class InsertOrUpdateGeneratorInformixTest {

  private InsertOrUpdateGeneratorInformix generator;
  private InsertOrUpdateStatement statement;
  private InformixDatabase database;

  @Before
  public void setUp() throws Exception {
    generator = new InsertOrUpdateGeneratorInformix();
    database = new InformixDatabase();

    // Setup the test statement to use throughout the tests
    statement = new InsertOrUpdateStatement("mycatalog", "myschema", "mytable", "pk_col1,pk_col2");
    statement.addColumnValue("pk_col1", 1);
    statement.addColumnValue("pk_col2", 2);
    statement.addColumnValue("col2", "value2");
    statement.addColumnValue("col3", null);
  }

  @Test
  public void getRecordCheck() throws Exception {
    String recordCheck = (String)invokePrivateMethod(generator,"getRecordCheck", new Object[] { statement, database, null });
    assertNotNull(recordCheck);

    Integer lineNumber = 0;
    String[] lines = recordCheck.split("\n");

    assertEquals("MERGE INTO mycatalog:myschema.mytable AS dst", lines[lineNumber]);
    lineNumber++;
    assertEquals("USING (", lines[lineNumber]);
    lineNumber++;
    assertEquals("\tSELECT 1 AS pk_col1, 2 AS pk_col2, 'value2' AS col2, NULL::INTEGER AS col3", lines[lineNumber]);
    lineNumber++;
    assertEquals("\tFROM sysmaster:informix.sysdual", lines[lineNumber]);
    lineNumber++;
    assertEquals(") AS src", lines[lineNumber]);
    lineNumber++;
    assertEquals("ON dst.pk_col1 = src.pk_col1 AND dst.pk_col2 = src.pk_col2", lines[lineNumber]);
    lineNumber++;
    assertEquals("WHEN NOT MATCHED THEN", lines[lineNumber]);
  }

  @Test
  public void getInsert() throws Exception {
    String insertStatement = (String)invokePrivateMethod(generator,"getInsertStatement", new Object[] { statement, database, null });
    assertNotNull(insertStatement);

    Integer lineNumber = 0;
    String[] lines = insertStatement.split("\n");

    assertEquals("INSERT (dst.pk_col1, dst.pk_col2, dst.col2, dst.col3) VALUES (src.pk_col1, src.pk_col2, src.col2, src.col3)", lines[lineNumber]);
  }

  @Test
  public void getElse() throws Exception {
    String elseStatement = (String)invokePrivateMethod(generator,"getElse", new Object[] { database });
    assertNotNull(elseStatement);

    Integer lineNumber = 0;
    String[] lines = elseStatement.split("\n");

    assertEquals("", lines[lineNumber]);
  }

  @Test
  public void getUpdateStatement() throws Exception {
    String updateStatement = (String)invokePrivateMethod(generator,"getUpdateStatement", new Object[] { statement, database, null, null });
    assertNotNull(updateStatement);

    Integer lineNumber = 0;
    String[] lines = updateStatement.split("\n");

    assertEquals("WHEN MATCHED THEN", lines[lineNumber]);
    lineNumber++;
    assertEquals("UPDATE SET dst.col2 = src.col2, dst.col3 = src.col3", lines[lineNumber]);
  }

  /**
   * When the table data is only keys, there will be no WHEN MATCHED THEN UPDATE... statement.
   * @throws Exception Throws exception
   */
  @Test
  public void getUpdateStatementKeysOnly() throws Exception {
    statement = new InsertOrUpdateStatement("mycatalog", "myschema", "mytable", "pk_col1,pk_col2");
    statement.addColumnValue("pk_col1", 1);
    statement.addColumnValue("pk_col2", 2);

    String updateStatement = (String)invokePrivateMethod(generator,"getUpdateStatement", new Object[] { statement, database, null, null });
    assertNotNull(updateStatement);

    Integer lineNumber = 0;

    String[] lines = updateStatement.split("\n");
    assertEquals("", lines[lineNumber]);
  }


  private static Object invokePrivateMethod(Object o, String methodName, Object[] params) {
    // Check we have valid arguments...
    assertNotNull(o);
    assertNotNull(methodName);

    // Go and find the private method...
    final Method methods[] = o.getClass().getDeclaredMethods();
    for (int i = 0; i < methods.length; ++i) {
      if (methodName.equals(methods[i].getName())) {
        try {
          methods[i].setAccessible(true);
          return methods[i].invoke(o, params);
        } catch (IllegalAccessException ex) {
          fail("IllegalAccessException accessing " + methodName);
        } catch (InvocationTargetException ite) {
          fail("InvocationTargetException accessing " + methodName);
        }
      }
    }
    fail("Method '" + methodName + "' not found");
    return null;
  }
}
