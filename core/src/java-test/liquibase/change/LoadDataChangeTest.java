package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.InsertStatement;
import liquibase.test.JUnitFileOpener;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link liquibase.change.AlterSequenceChange}
 */
public class LoadDataChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Load Data", new LoadDataChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("changelogs/sample.data1.csv");
        refactoring.setFileOpener(new JUnitFileOpener());

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());

        assertEquals(2, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof InsertStatement);
        assertTrue(sqlStatements[1] instanceof InsertStatement);

        assertEquals("SCHEMA_NAME", ((InsertStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((InsertStatement) sqlStatements[0]).getTableName());
        assertEquals("Bob Johnson", ((InsertStatement) sqlStatements[0]).getColumnValue("name"));
        assertEquals("bjohnson", ((InsertStatement) sqlStatements[0]).getColumnValue("username"));

        assertEquals("SCHEMA_NAME", ((InsertStatement) sqlStatements[1]).getSchemaName());
        assertEquals("TABLE_NAME", ((InsertStatement) sqlStatements[1]).getTableName());
        assertEquals("John Doe", ((InsertStatement) sqlStatements[1]).getColumnValue("name"));
        assertEquals("jdoe", ((InsertStatement) sqlStatements[1]).getColumnValue("username"));
    }

    @Test
    public void generateStatement_excel() throws Exception {
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("changelogs/sample.data1-excel.csv");
        refactoring.setFileOpener(new JUnitFileOpener());

        LoadDataColumnConfig ageConfig = new LoadDataColumnConfig();
        ageConfig.setHeader("age");
        ageConfig.setType("NUMERIC");
        refactoring.addColumn(ageConfig);

        LoadDataColumnConfig activeConfig = new LoadDataColumnConfig();
        activeConfig.setHeader("active");
        activeConfig.setType("BOOLEAN");
        refactoring.addColumn(activeConfig);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());

        assertEquals(2, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof InsertStatement);
        assertTrue(sqlStatements[1] instanceof InsertStatement);

        assertEquals("SCHEMA_NAME", ((InsertStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((InsertStatement) sqlStatements[0]).getTableName());
        assertEquals("Bob Johnson", ((InsertStatement) sqlStatements[0]).getColumnValue("name"));
        assertEquals("bjohnson", ((InsertStatement) sqlStatements[0]).getColumnValue("username"));
        assertEquals("15", ((InsertStatement) sqlStatements[0]).getColumnValue("age").toString());
        assertEquals(Boolean.TRUE, ((InsertStatement) sqlStatements[0]).getColumnValue("active"));

        assertEquals("SCHEMA_NAME", ((InsertStatement) sqlStatements[1]).getSchemaName());
        assertEquals("TABLE_NAME", ((InsertStatement) sqlStatements[1]).getTableName());
        assertEquals("John Doe", ((InsertStatement) sqlStatements[1]).getColumnValue("name"));
        assertEquals("jdoe", ((InsertStatement) sqlStatements[1]).getColumnValue("username"));
        assertEquals("21", ((InsertStatement) sqlStatements[1]).getColumnValue("age").toString());
        assertEquals(Boolean.FALSE, ((InsertStatement) sqlStatements[1]).getColumnValue("active"));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("FILE_NAME");

        assertEquals("Data loaded from FILE_NAME into TABLE_NAME", refactoring.getConfirmationMessage());
    }
    
    @Test
    public void generateCheckSum() throws Exception {
        LoadDataChange refactoring = new LoadDataChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setFile("changelogs/sample.data1.csv");
        refactoring.setFileOpener(new JUnitFileOpener());

        String md5sum1 = refactoring.generateCheckSum().toString();

        refactoring.setFile("changelogs/sample.data2.csv");
        String md5sum2 = refactoring.generateCheckSum().toString();

        assertTrue(!md5sum1.equals(md5sum2));
        assertEquals(md5sum2, refactoring.generateCheckSum().toString());
    }

}