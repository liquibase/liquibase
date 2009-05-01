package liquibase.change;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.database.structure.Column;
import liquibase.database.statement.*;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class AddAutoIncrementChangeTest extends AbstractChangeTest {

    @Test
    public void constructor() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        assertEquals("addAutoIncrement", change.getChangeMetaData().getName());
        assertEquals("Set Column as Auto-Increment", change.getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        testChangeOnAllExcept(change, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {

                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof AddAutoIncrementStatement);
                assertEquals("SCHEMA_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TABLE_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getTableName());
                assertEquals("COLUMN_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnName());
                assertEquals("DATATYPE(255)", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnDataType());
            }
        }, PostgresDatabase.class);
        testChange(change, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {

                assertEquals(3, sqlStatements.length);
                //todo: improve test as statements are no longer raw statements
                assertTrue(sqlStatements[0] instanceof CreateSequenceStatement);
                assertTrue(sqlStatements[1] instanceof SetNullableStatement);
                assertTrue(sqlStatements[2] instanceof AddDefaultValueStatement);
            }
        }, PostgresDatabase.class);
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Set Column as Auto-Increment", new AddAutoIncrementChange().getChangeMetaData().getDescription());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        assertEquals("Auto-increment added to TABLE_NAME.COLUMN_NAME", change.getConfirmationMessage());
    }
}
