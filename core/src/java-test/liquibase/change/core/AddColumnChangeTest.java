package liquibase.change.core;

import liquibase.database.core.DB2Database;
import liquibase.database.Database;
import liquibase.statement.AddColumnStatement;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.UpdateStatement;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.AbstractChangeTest;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.Change;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link AddColumnChange}
 */
public class AddColumnChangeTest extends AbstractChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        assertEquals("Add Column", refactoring.getChangeMetaData().getDescription());
    }

    @Test
    public void addColumn() throws Exception {
        AddColumnChange change = new AddColumnChange();
        assertEquals(0, change.getColumns().size());

        change.addColumn(new ColumnConfig().setName("a"));
        assertEquals(1, change.getColumns().size());

        change.addColumn(new ColumnConfig().setName("b"));
        assertEquals(2, change.getColumns().size());
    }

    @Test
    public void removeColumn() throws Exception {
        ColumnConfig columnA = new ColumnConfig().setName("a");
        ColumnConfig columnB = new ColumnConfig().setName("b");

        AddColumnChange change = new AddColumnChange();
        assertEquals(0, change.getColumns().size());

        change.removeColumn(columnA);
        assertEquals(0, change.getColumns().size());

        change.addColumn(columnA);
        assertEquals(1, change.getColumns().size());

        change.removeColumn(columnB);
        assertEquals(1, change.getColumns().size());

        change.removeColumn(columnA);
        assertEquals(0, change.getColumns().size());
    }

    @Test
    public void getLastColumn() {
        ColumnConfig columnA = new ColumnConfig().setName("a");
        ColumnConfig columnB = new ColumnConfig().setName("b");

        AddColumnChange change = new AddColumnChange();
        assertEquals(0, change.getColumns().size());
        assertNull(change.getLastColumn());

        change.addColumn(columnA);
        assertEquals(1, change.getColumns().size());
        assertEquals(columnA, change.getLastColumn());

        change.addColumn(columnB);
        assertEquals(2, change.getColumns().size());
        assertEquals(columnB, change.getLastColumn());

    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");

        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        //Add the second column def to the same refactoring
        column = new ColumnConfig();
        column.setName("NEWCOL2");
        column.setType("TYP2");
        column.setConstraints(new ConstraintsConfig());
        refactoring.addColumn(column);

        testChangeOnAll(refactoring, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {

                if (database instanceof DB2Database) {
                    assertEquals(4, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    assertTrue(sqlStatements[2] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[3] instanceof ReorganizeTableStatement);
                } else {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof AddColumnStatement);
                }

                AddColumnStatement firstAddColumnStatement = (AddColumnStatement) sqlStatements[0];
                AddColumnStatement secondAddColumnStatement = null;
                if (database instanceof DB2Database) {
                    secondAddColumnStatement = (AddColumnStatement) sqlStatements[2];
                } else {
                    secondAddColumnStatement = (AddColumnStatement) sqlStatements[1];
                }

                assertEquals("SCHEMA", firstAddColumnStatement.getSchemaName());
                assertEquals("TAB", firstAddColumnStatement.getTableName());
                assertEquals("NEWCOL", firstAddColumnStatement.getColumnName());
                assertEquals("TYP", firstAddColumnStatement.getColumnType());
                assertFalse(firstAddColumnStatement.isPrimaryKey());
                assertFalse(firstAddColumnStatement.isNullable());

                assertEquals("SCHEMA", secondAddColumnStatement.getSchemaName());
                assertEquals("TAB", secondAddColumnStatement.getTableName());
                assertEquals("NEWCOL2", secondAddColumnStatement.getColumnName());
                assertEquals("TYP2", secondAddColumnStatement.getColumnType());
                assertTrue(secondAddColumnStatement.isNullable());
            }
        });
    }

    @Test
    public void generateStatement_nullable() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testChangeOnAll(refactoring, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {

                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                }

                assertEquals("SCHEMA", ((AddColumnStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TAB", ((AddColumnStatement) sqlStatements[0]).getTableName());
                assertEquals("NEWCOL", ((AddColumnStatement) sqlStatements[0]).getColumnName());
                assertEquals("TYP", ((AddColumnStatement) sqlStatements[0]).getColumnType());
                assertFalse(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
                assertTrue(((AddColumnStatement) sqlStatements[0]).isNullable());
            }
        });
    }

    @Test
    public void generateStatement_notNull() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testChangeOnAll(refactoring, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {
                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                }

                assertEquals("SCHEMA", ((AddColumnStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TAB", ((AddColumnStatement) sqlStatements[0]).getTableName());
                assertEquals("NEWCOL", ((AddColumnStatement) sqlStatements[0]).getColumnName());
                assertEquals("TYP", ((AddColumnStatement) sqlStatements[0]).getColumnType());
                assertFalse(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
                assertFalse(((AddColumnStatement) sqlStatements[0]).isNullable());
            }
        });
    }

    @Test
    public void generateStatement_primaryKey() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testChangeOnAll(refactoring, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {
                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                }

                assertTrue(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
            }
        });
    }

    @Test
    public void generateStatement_withDefaultValue() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        column.setValue("SOME VALUE");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testChangeOnAll(refactoring, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {
                if (database instanceof DB2Database) {
                    assertEquals(3, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    assertTrue(sqlStatements[2] instanceof UpdateStatement);
                } else {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof UpdateStatement);
                }

                assertTrue(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
                assertTrue(((AddColumnStatement) sqlStatements[0]).isAutoIncrement());

                assertEquals("TAB", ((UpdateStatement) sqlStatements[sqlStatements.length - 1]).getTableName());
                assertEquals("SOME VALUE", ((UpdateStatement) sqlStatements[sqlStatements.length - 1]).getNewColumnValues().get("NEWCOL"));
            }
        });
    }

    @Test
    public void generateStatement_autoIncrement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testChangeOnAll(refactoring, new GenerateAllValidator() {
            public void validate(SqlStatement[] sqlStatements, Database database) {
                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                }

                assertTrue(((AddColumnStatement) sqlStatements[0]).isPrimaryKey());
                assertTrue(((AddColumnStatement) sqlStatements[0]).isAutoIncrement());
            }
        });
    }

    @Test
    public void createInverses_singleColumn() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testInverseOnAll(refactoring, new InverseValidator() {
            public void validate(Change[] changes) {
                assertEquals(1, changes.length);
                assertTrue(changes[0] instanceof DropColumnChange);
                assertEquals("TAB", ((DropColumnChange) changes[0]).getTableName());
                assertEquals("NEWCOL", ((DropColumnChange) changes[0]).getColumnName());

            }
        });
    }

    @Test
    public void createInverses_multiColumn() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        column = new ColumnConfig();
        column.setName("NEWCOL2");
        column.setType("TYP");
        refactoring.addColumn(column);

        testInverseOnAll(refactoring, new InverseValidator() {
            public void validate(Change[] changes) {
                assertEquals(2, changes.length);
                assertTrue(changes[0] instanceof DropColumnChange);
                assertEquals("NEWCOL", ((DropColumnChange) changes[0]).getColumnName());
                assertTrue(changes[1] instanceof DropColumnChange);
                assertEquals("NEWCOL2", ((DropColumnChange) changes[1]).getColumnName());
            }
        });
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        assertEquals("Columns NEWCOL(TYP) added to TAB", refactoring.getConfirmationMessage());
    }

}
