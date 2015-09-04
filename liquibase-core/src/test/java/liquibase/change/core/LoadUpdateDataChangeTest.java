package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertOrUpdateStatement;

/**
 * Created by IntelliJ IDEA.
 * User: bassettt
 * Date: Dec 1, 2009
 * Time: 9:29:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadUpdateDataChangeTest extends StandardChangeTest {

    LoadUpdateDataChange refactoring ;

    @Before
    public void setUp() throws Exception {
        refactoring = new LoadUpdateDataChange();
    }

    @Override
    public void getRefactoringName() throws Exception {
        assertEquals("loadUpdateData", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
    }

    @Override
    public void generateStatement() throws Exception {

        MockDatabase database = new MockDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setPrimaryKey("name");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());

        SqlStatement[] statements = change.generateStatements(database);


        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(2, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof InsertOrUpdateStatement);
        assertEquals("SCHEMA_NAME", ((InsertOrUpdateStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((InsertOrUpdateStatement) sqlStatements[0]).getTableName());
        assertEquals("name", ((InsertOrUpdateStatement) sqlStatements[0]).getPrimaryKey());
    }

    @Override
    public void getConfirmationMessage() throws Exception {
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        assertEquals("Data loaded from FILE_NAME into TABLE_NAME", refactoring.getConfirmationMessage());
    }


//    Proves that LoadUpdateDataChange creates InsertOrUpdateStatements
    @Test
    public void getStatements() throws Exception {
        MockDatabase database = new MockDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());

        SqlStatement[] statements = change.generateStatements(database);

        assertNotNull(statements);
        assertEquals(InsertOrUpdateStatement.class,statements[0].getClass());
    }

    @Test
    public void generateSql(){
        MockDatabase database = new MockDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());

        SqlStatement[] statements = change.generateStatements(database);

        assertNotNull(statements);
        assertEquals(InsertOrUpdateStatement.class,statements[0].getClass());

    }

    @Test
    public void primaryKey() throws LiquibaseException {
        LoadUpdateDataChange change = new LoadUpdateDataChange();
        String primaryKey = "myPrimaryKey";
        change.setPrimaryKey(primaryKey);
        assertEquals(primaryKey, change.getPrimaryKey());
    }

    @Test
    public void getWhereClause() throws LiquibaseException {
        MockDatabase database = new MockDatabase();
        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());
        change.setPrimaryKey("name");
        SqlStatement[] statements = change.generateStatements(database);

//        private String getWhere(InsertOrUpdateStatement insertOrUpdateStatement, Database database)

        Object[] args ;
        String result ;
        args = new Object[] { statements[0], database };
        result = (String)invokePrivateMethod(change,"getWhere",args);
        assertEquals("name = 'Bob Johnson'",result.trim());
        args = new Object[] { statements[1], database };
        result = (String)invokePrivateMethod(change,"getWhere",args);
        assertEquals("name = 'John Doe'",result.trim());

    }

    @Test
    public void generateRollbacksForData1CSV() throws Exception {
        MockDatabase database = new MockDatabase();

        LoadUpdateDataChange change = new LoadUpdateDataChange();

        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setFile("liquibase/change/core/sample.data1.csv");
        change.setResourceAccessor(new ClassLoaderResourceAccessor());
        change.setPrimaryKey("name");

        SqlStatement[] statements = change.generateRollbackStatements(database);


        assertNotNull(statements);
        assertEquals(DeleteStatement.class,statements[0].getClass());

        DeleteStatement delete ;

        delete = (DeleteStatement)statements[0];
        assertEquals( "name = 'Bob Johnson'", delete.getWhereClause().trim());
        delete = (DeleteStatement)statements[1];
        assertEquals( "name = 'John Doe'", delete.getWhereClause().trim());

    }

    @Override
    public void generateCheckSum() throws Exception {
        LoadUpdateDataChange refactoring = new LoadUpdateDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("liquibase/change/core/sample.data1.csv");
        refactoring.setResourceAccessor(new ClassLoaderResourceAccessor());

        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("liquibase/change/core/sample.data2.csv");
        String md5sum2 = refactoring.generateCheckSum().toString();

        assertTrue(!md5sum1.equals(md5sum2));
        assertEquals(md5sum2, refactoring.generateCheckSum().toString());

    }

    @Override
    public void isSupported() throws Exception {
        //TODO: To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void validate() throws Exception {
        //TODO: To change body of overridden methods use File | Settings | File Templates.
    }

    public static Object invokePrivateMethod (Object o, String methodName, Object[] params) {
             // Check we have valid arguments...
            Assert.assertNotNull(o);
            Assert.assertNotNull(methodName);
            Assert.assertNotNull(params);

            // Go and find the private method...
            final Method methods[] = o.getClass().getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
              if (methodName.equals(methods[i].getName())) {
                try {
                  methods[i].setAccessible(true);
                  return methods[i].invoke(o, params);
                }
                catch (IllegalAccessException ex) {
                  Assert.fail ("IllegalAccessException accessing " + methodName);
                }
                catch (InvocationTargetException ite) {
                    Assert.fail ("InvocationTargetException accessing " + methodName);
                }
              }
            }
            Assert.fail ("Method '" + methodName +"' not found");
            return null;
          }
}