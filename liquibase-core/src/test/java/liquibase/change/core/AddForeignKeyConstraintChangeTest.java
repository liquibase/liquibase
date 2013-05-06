package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddForeignKeyConstraintChangeTest  extends StandardChangeTest {


    @Override
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
        assertEquals("CASCADE", statement.getOnDelete());
    }

      @Override
      public void getRefactoringName() throws Exception {
        assertEquals("addForeignKeyConstraint", ChangeFactory.getInstance().getChangeMetaData(new AddForeignKeyConstraintChange()).getName());
    }

    @Override
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
