package liquibase.change.core;

import java.math.BigInteger;

import liquibase.change.AbstractChangeTest;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.UpdateStatement;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link AddColumnChange}
 */
public class AddColumnChangeTest extends AbstractChangeTest {


    @Override
    public void validate() throws Exception {
        super.validate();    //To change body of overridden methods use File | Settings | File Templates.
    }

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

        AddColumnConfig column = new AddColumnConfig();
        column.setName("a");
		change.addColumn(column);
        assertEquals(1, change.getColumns().size());

        column = new AddColumnConfig();
        column.setName("b");
        change.addColumn(column);
        assertEquals(2, change.getColumns().size());
    }

    @Test
    public void removeColumn() throws Exception {
    	AddColumnConfig columnA = new AddColumnConfig();
    	columnA.setName("a");

    	AddColumnConfig columnB = new AddColumnConfig();
    	columnB.setName("b");

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

    @Override
    @Test
    public void generateStatement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");

        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        //Add the second column def to the same refactoring
        column = new AddColumnConfig();
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
        AddColumnConfig column = new AddColumnConfig();
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
        AddColumnConfig column = new AddColumnConfig();
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
        AddColumnConfig column = new AddColumnConfig();
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
    public void generateStatement_foreignKey() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        constraints.setForeignKeyName("fk_name");
        constraints.setReferences("ref_table(id)");

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
                boolean foundFkInfo = false;
                for (ColumnConstraint constraint : ((AddColumnStatement) sqlStatements[0]).getConstraints()) {
                    if (constraint instanceof ForeignKeyConstraint) {
                        foundFkInfo = true;
                    }
                }
                assertTrue("Did not find foreign key info", foundFkInfo);
            }
        });
    }

    @Test
    public void generateStatement_withDefaultValue() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
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
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);
        column.setStartWith(BigInteger.valueOf(2));
        column.setIncrementBy(BigInteger.TEN);

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
                assertNotNull(((AddColumnStatement) sqlStatements[0]).getAutoIncrementConstraint());
                assertEquals(BigInteger.valueOf(2), ((AddColumnStatement) sqlStatements[0]).getAutoIncrementConstraint().getStartWith());
                assertEquals(BigInteger.TEN, ((AddColumnStatement) sqlStatements[0]).getAutoIncrementConstraint().getIncrementBy());
            }
        });
    }

    @Test
    public void createInverses_singleColumn() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
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
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        column = new AddColumnConfig();
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
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        assertEquals("Columns NEWCOL(TYP) added to TAB", refactoring.getConfirmationMessage());
    }

}
