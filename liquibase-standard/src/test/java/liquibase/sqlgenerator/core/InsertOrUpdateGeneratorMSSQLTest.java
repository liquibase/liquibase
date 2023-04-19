package liquibase.sqlgenerator.core;

import liquibase.database.core.MSSQLDatabase;
import liquibase.statement.core.InsertOrUpdateStatement;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InsertOrUpdateGeneratorMSSQLTest {


    public static Object invokePrivateMethod(Object o, String methodName, Object[] params) {
        // Check we have valid arguments...
        Assert.assertNotNull(o);
        Assert.assertNotNull(methodName);
//            Assert.assertNotNull(params);

        // Go and find the private method...
        final Method methods[] = o.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (methodName.equals(methods[i].getName())) {
                try {
                    methods[i].setAccessible(true);
                    return methods[i].invoke(o, params);
                } catch (IllegalAccessException ex) {
                    Assert.fail("IllegalAccessException accessing " + methodName);
                } catch (InvocationTargetException ite) {
                    Assert.fail("InvocationTargetException accessing " + methodName);
                }
            }
        }
        Assert.fail("Method '" + methodName + "' not found");
        return null;
    }

    @Test
    public void getRecordCheck(){
        InsertOrUpdateGeneratorMSSQL generator = new InsertOrUpdateGeneratorMSSQL();
        MSSQLDatabase database = new MSSQLDatabase();

        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema","mytable","pk_col1");
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");

        String where = "1 = 1";

        String recordCheck = (String)invokePrivateMethod(generator,"getRecordCheck", new Object[] {statement,database,where});

        Integer lineNumber = 0;
        String[] lines = recordCheck.split("\n");
        assertEquals("DECLARE @reccount integer", lines[lineNumber]);
        lineNumber++;
        assertEquals("SELECT @reccount = count(*) FROM mycatalog.myschema.mytable WHERE " + where, lines[lineNumber]);
        lineNumber++;
        assertEquals("IF @reccount = 0", lines[lineNumber]);

    }

    @Test
    public void getInsert(){
        InsertOrUpdateGeneratorMSSQL generator = new InsertOrUpdateGeneratorMSSQL();
        MSSQLDatabase database = new MSSQLDatabase();

        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema","mytable","pk_col1");
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");

        String where = "1 = 1";

        Class c = InsertOrUpdateGenerator.class.getClass();
        //InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain
        String insertStatement = (String)invokePrivateMethod(generator,"getInsertStatement", new Object[] {statement,database,null});

        Integer lineNumber = 0;
        String[] lines = insertStatement.split("\n");
        assertEquals("BEGIN", lines[lineNumber]);
        lineNumber++;
        assertTrue(lines[lineNumber].startsWith("INSERT"));
        lineNumber++;
        assertEquals("END", lines[lineNumber]);
    }

    @Test
    public void getElse(){
        InsertOrUpdateGeneratorMSSQL generator = new InsertOrUpdateGeneratorMSSQL();
        MSSQLDatabase database = new MSSQLDatabase();

        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema","mytable","pk_col1");
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");

        String where = "1 = 1";

        Class c = InsertOrUpdateGenerator.class.getClass();
        //InsertOrUpdateStatement insertOrUpdateStatement, Database database, SqlGeneratorChain sqlGeneratorChain
        String insertStatement = (String)invokePrivateMethod(generator,"getElse", new Object[] {database});

        Integer lineNumber = 0;
        String[] lines = insertStatement.split("\n");
        assertEquals("ELSE", lines[lineNumber]);
    }

    @Test
     public void getUpdate(){
         InsertOrUpdateGeneratorMSSQL generator = new InsertOrUpdateGeneratorMSSQL();
         MSSQLDatabase database = new MSSQLDatabase();

         InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema","mytable","pk_col1");
         statement.addColumnValue("col2","value2");

         String where = "1 = 1";

         Class c = InsertOrUpdateGenerator.class.getClass();
        //InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause, SqlGeneratorChain sqlGeneratorChain
         String insertStatement = (String)invokePrivateMethod(generator,"getUpdateStatement", new Object[] {statement,database,where,null});

         Integer lineNumber = 0;
         String[] lines = insertStatement.split("\n");
         assertEquals("BEGIN", lines[lineNumber]);
         lineNumber++;
         assertTrue(lines[lineNumber].startsWith("UPDATE"));
         lineNumber++;
         assertEquals("END", lines[lineNumber]);
     }


}
