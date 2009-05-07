package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.statement.AddForeignKeyConstraintStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.sql.DatabaseMetaData;

public class AddForeignKeyConstraintChangeTest  extends AbstractChangeTest {


    @Test
    public void generateStatement() throws Exception {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName("FK_NAME");

        change.setBaseTableSchemaName("BASE_SCHEMA_NAME");
        change.setBaseTableName("BASE_TABLE_NAME");
        change.setBaseColumnNames("BASE_COL_NAME");

        change.setReferencedTableSchemaName("REF_SCHEMA_NAME");
        change.setReferencedTableName("REF_TABLE_NAME");
        change.setReferencedColumnNames("REF_COL_NAME");

        change.setDeferrable(true);
        change.setDeleteCascade(true);
        change.setInitiallyDeferred(true);

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        assertEquals(1, statements.length);
        AddForeignKeyConstraintStatement statement = (AddForeignKeyConstraintStatement) statements[0];


        assertEquals("FK_NAME", statement.getConstraintName());

        assertEquals("BASE_SCHEMA_NAME", statement.getBaseTableSchemaName());
        assertEquals("BASE_TABLE_NAME", statement.getBaseTableName());
        assertEquals("BASE_COL_NAME", statement.getBaseColumnNames());

        assertEquals("REF_SCHEMA_NAME", statement.getReferencedTableSchemaName());
        assertEquals("REF_TABLE_NAME", statement.getReferencedTableName());
        assertEquals("REF_COL_NAME", statement.getReferencedColumnNames());

        assertEquals(true, statement.isDeferrable());
        assertEquals(true, statement.isInitiallyDeferred());
        assertEquals(new Integer(DatabaseMetaData.importedKeyCascade), statement.getDeleteRule());
    }

      public void getRefactoringName() throws Exception {
        assertEquals("Add Foreign Key Constraint", new AddForeignKeyConstraintChange().getChangeMetaData().getDescription());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName("FK_NAME");
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setBaseColumnNames("COL_NAME");

        assertEquals("Foreign key contraint added to TABLE_NAME (COL_NAME)", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }
}
