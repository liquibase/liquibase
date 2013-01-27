package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for {@link CreateIndexChange}
 */
public class CreateIndexChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Create Index", new CreateIndexChange().getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                CreateIndexChange change = new CreateIndexChange();
//                change.setIndexName("IDX_NAME");
//                change.setSchemaName("SCHEMA_NAME");
//                change.setTableName("TABLE_NAME");
//                change.setTablespace("TABLESPACE_NAME");
//
//                ColumnConfig column = new ColumnConfig();
//                column.setName("COL_NAME");
//                change.addColumn(column);
//
//                ColumnConfig column2 = new ColumnConfig();
//                column2.setName("COL2_NAME");
//                change.addColumn(column2);
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof CreateIndexStatement);
//
//                assertEquals("IDX_NAME", ((CreateIndexStatement) sqlStatements[0]).getIndexName());
//                assertEquals("SCHEMA_NAME", ((CreateIndexStatement) sqlStatements[0]).getTableSchemaName());
//                assertEquals("TABLE_NAME", ((CreateIndexStatement) sqlStatements[0]).getTableName());
//                assertEquals("TABLESPACE_NAME", ((CreateIndexStatement) sqlStatements[0]).getTablespace());
//                assertEquals(2, ((CreateIndexStatement) sqlStatements[0]).getColumns().length);
//                assertEquals("COL_NAME", ((CreateIndexStatement) sqlStatements[0]).getColumns()[0]);
//                assertEquals("COL2_NAME", ((CreateIndexStatement) sqlStatements[0]).getColumns()[1]);
//            }
//        });
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        CreateIndexChange refactoring = new CreateIndexChange();
        refactoring.setIndexName("IDX_TEST");

        assertEquals("Index IDX_TEST created", refactoring.getConfirmationMessage());
    }
}