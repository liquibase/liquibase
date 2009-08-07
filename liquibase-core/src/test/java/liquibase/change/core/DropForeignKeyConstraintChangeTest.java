package liquibase.change.core;

import liquibase.change.AbstractChangeTest;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropForeignKeyConstraintChangeTest extends AbstractChangeTest {
 @Override
 @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Foreign Key Constraint", new DropForeignKeyConstraintChange().getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {

//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
//                change.setBaseTableSchemaName("SCHEMA_NAME");
//                change.setBaseTableName("TABLE_NAME");
//                change.setConstraintName("FK_NAME");
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof DropForeignKeyConstraintStatement);
//
//                assertEquals("SCHEMA_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getBaseTableSchemaName());
//                assertEquals("TABLE_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getBaseTableName());
//                assertEquals("FK_NAME", ((DropForeignKeyConstraintStatement) sqlStatements[0]).getConstraintName());
//            }
//        });
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        assertEquals("Foreign key FK_NAME dropped", change.getConfirmationMessage());
    }
}
