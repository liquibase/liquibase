package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.*;

public class AddLookupTableChangeTest extends StandardChangeTest {

    @Override
    public void getRefactoringName() throws Exception {
        assertEquals("addLookupTable", ChangeFactory.getInstance().getChangeMetaData(new AddLookupTableChange()).getName());
    }

    @Override
    public void generateStatement() throws Exception {
        AddLookupTableChange change = new AddLookupTableChange();
        change.setExistingTableName("OLD_TABLE_NAME");
        change.setExistingColumnName("OLD_COLUMN_NAME");
        change.setExistingTableSchemaName("OLD_SCHEMA");
        change.setConstraintName("FK_NAME");
        change.setNewColumnDataType("TYPE(255)");
        change.setNewTableName("NEW_TABLE");
        change.setNewColumnName("NEW_COL");
        change.setNewTableSchemaName("NEW_SCHEM");

        testChangeOnAll(change, new GenerateAllValidator() {
            @Override
            public void validate(SqlStatement[] statements, Database database) {
                
                // TODO this test should reorganized 
                // first statement is RawSql to create new (Lookup) Table
                // second - Set Not Null Constraint for the Field in the created Lookup table
                // third - primary key constraint of the Lookup Table
            	// forth - add foreign key reference from old (base) table to the lookup table
            	// Take into account that different databases could produce different number of the statements.
            	
                
//                assertEquals(4, statements.length);
//                AddDefaultValueStatement statement = (AddDefaultValueStatement) statements[0];


//                assertEquals("TABLE_NAME", statement.getTableName());
//                assertEquals("COLUMN_NAME", statement.getColumnName());
//                assertTrue(statement.getDefaultValue() instanceof Boolean);
//                assertEquals(Boolean.TRUE, statement.getDefaultValue());
            }
        });
    }

    @Override
    public void getConfirmationMessage() throws Exception {
        AddLookupTableChange change = new AddLookupTableChange();
        change.setExistingTableName("OLD_TABLE_NAME");
        change.setExistingColumnName("OLD_COLUMN_NAME");

        assertEquals("Lookup table added for OLD_TABLE_NAME.OLD_COLUMN_NAME",change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof FirebirdDatabase
                || database instanceof SQLiteDatabase
                || database instanceof HsqlDatabase;
    }
}
