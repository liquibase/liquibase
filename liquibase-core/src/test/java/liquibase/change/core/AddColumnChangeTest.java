package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ConstraintsConfig;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AlterTableStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.UpdateStatement;

import org.junit.Test;

/**
 * Tests for {@link AddColumnChange}
 */
public class AddColumnChangeTest extends StandardChangeTest {


    @Override
    public void validate() throws Exception {
        super.validate();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        assertEquals("addColumn", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {

                if (database instanceof DB2Database) {
                    assertEquals(4, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    assertTrue(sqlStatements[2] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[3] instanceof ReorganizeTableStatement);
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                } else {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof AddColumnStatement);
                }

                if (database instanceof MySQLDatabase) {
                    AlterTableStatement alterTableStatement = (AlterTableStatement) sqlStatements[0];
                    assertEquals("SCHEMA", alterTableStatement.getSchemaName());
                    assertEquals("TAB", alterTableStatement.getTableName());

                    AddColumnStatement firstAddColumnStatement = alterTableStatement.getColumns().get(0);
                    assertEquals("NEWCOL", firstAddColumnStatement.getColumnName());
                    assertEquals("TYP", firstAddColumnStatement.getColumnType());
                    assertFalse(firstAddColumnStatement.isPrimaryKey());
                    assertFalse(firstAddColumnStatement.isNullable());

                    AddColumnStatement secondAddColumnStatement = alterTableStatement.getColumns().get(1);
                    assertEquals("SCHEMA", secondAddColumnStatement.getSchemaName());
                    assertEquals("TAB", secondAddColumnStatement.getTableName());
                    assertEquals("NEWCOL2", secondAddColumnStatement.getColumnName());
                    assertEquals("TYP2", secondAddColumnStatement.getColumnType());
                    assertTrue(secondAddColumnStatement.isNullable());
                } else {
                    AddColumnStatement firstAddColumnStatement = (AddColumnStatement) sqlStatements[0];

                    assertEquals("SCHEMA", firstAddColumnStatement.getSchemaName());
                    assertEquals("TAB", firstAddColumnStatement.getTableName());
                    assertEquals("NEWCOL", firstAddColumnStatement.getColumnName());
                    assertEquals("TYP", firstAddColumnStatement.getColumnType());
                    assertFalse(firstAddColumnStatement.isPrimaryKey());
                    assertFalse(firstAddColumnStatement.isNullable());

                    AddColumnStatement secondAddColumnStatement = null;
                    if (database instanceof DB2Database) {
                        secondAddColumnStatement = (AddColumnStatement) sqlStatements[2];
                    } else {
                        secondAddColumnStatement = (AddColumnStatement) sqlStatements[1];
                    }

                    assertEquals("SCHEMA", secondAddColumnStatement.getSchemaName());
                    assertEquals("TAB", secondAddColumnStatement.getTableName());
                    assertEquals("NEWCOL2", secondAddColumnStatement.getColumnName());
                    assertEquals("TYP2", secondAddColumnStatement.getColumnType());
                    assertTrue(secondAddColumnStatement.isNullable());
                }
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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {

                AddColumnStatement addColumnStatement;

                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    addColumnStatement = ((AlterTableStatement) sqlStatements[0]).getColumns().get(0);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                }

                assertEquals("SCHEMA", addColumnStatement.getSchemaName());
                assertEquals("TAB", addColumnStatement.getTableName());
                assertEquals("NEWCOL", addColumnStatement.getColumnName());
                assertEquals("TYP", addColumnStatement.getColumnType());
                assertFalse(addColumnStatement.isPrimaryKey());
                assertTrue(addColumnStatement.isNullable());
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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {
                AddColumnStatement addColumnStatement;

                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    addColumnStatement = ((AlterTableStatement) sqlStatements[0]).getColumns().get(0);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                }

                assertEquals("SCHEMA", addColumnStatement.getSchemaName());
                assertEquals("TAB", addColumnStatement.getTableName());
                assertEquals("NEWCOL", addColumnStatement.getColumnName());
                assertEquals("TYP", addColumnStatement.getColumnType());
                assertFalse(addColumnStatement.isPrimaryKey());
                assertFalse(addColumnStatement.isNullable());
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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {
                AddColumnStatement addColumnStatement;

                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    addColumnStatement = ((AlterTableStatement) sqlStatements[0]).getColumns().get(0);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                }

                assertTrue(addColumnStatement.isPrimaryKey());
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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {
                AddColumnStatement addColumnStatement;

                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    addColumnStatement = ((AlterTableStatement) sqlStatements[0]).getColumns().get(0);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                }

                assertTrue(addColumnStatement.isPrimaryKey());
                boolean foundFkInfo = false;
                for (ColumnConstraint constraint : addColumnStatement.getConstraints()) {
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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {
                AddColumnStatement addColumnStatement;

                if (database instanceof DB2Database) {
                    assertEquals(3, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    assertTrue(sqlStatements[2] instanceof UpdateStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    assertTrue(sqlStatements[1] instanceof UpdateStatement);
                    addColumnStatement = ((AlterTableStatement) sqlStatements[0]).getColumns().get(0);
                } else {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof UpdateStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                }

                assertTrue(addColumnStatement.isPrimaryKey());
                assertTrue(addColumnStatement.isAutoIncrement());

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
            @Override
            public void validate(SqlStatement[] sqlStatements, Database database) {
                AddColumnStatement addColumnStatement;

                if (database instanceof DB2Database) {
                    assertEquals(2, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    assertTrue(sqlStatements[1] instanceof ReorganizeTableStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                } else if (database instanceof MySQLDatabase) {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AlterTableStatement);
                    addColumnStatement = ((AlterTableStatement) sqlStatements[0]).getColumns().get(0);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddColumnStatement);
                    addColumnStatement = (AddColumnStatement) sqlStatements[0];
                }

                assertTrue(addColumnStatement.isPrimaryKey());
                assertTrue(addColumnStatement.isAutoIncrement());
                assertNotNull(addColumnStatement.getAutoIncrementConstraint());
                assertEquals(BigInteger.valueOf(2), addColumnStatement.getAutoIncrementConstraint().getStartWith());
                assertEquals(BigInteger.TEN, addColumnStatement.getAutoIncrementConstraint().getIncrementBy());
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
            @Override
            public void validate(Change[] changes) {
                assertEquals(1, changes.length);
                assertTrue(changes[0] instanceof DropColumnChange);
                assertEquals("TAB", ((DropColumnChange) changes[0]).getTableName());
                assertEquals("NEWCOL", ((DropColumnChange) changes[0]).getColumnName());

            }
        });
    }

    @Test
    public void createInverses_defaultValue() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setSchemaName("SCHEMA");
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        column.setDefaultValue("DEFAULT");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setNullable(Boolean.FALSE);
        constraints.setPrimaryKey(Boolean.TRUE);
        column.setAutoIncrement(Boolean.TRUE);

        column.setConstraints(constraints);

        refactoring.addColumn(column);

        testInverseOnAll(refactoring, new InverseValidator() {
            @Override
            public void validate(Change[] changes) {
                assertEquals(2, changes.length);
                assertTrue(changes[0] instanceof DropDefaultValueChange);
                assertEquals("TAB", ((DropDefaultValueChange) changes[0]).getTableName());
                assertEquals("NEWCOL", ((DropDefaultValueChange) changes[0]).getColumnName());
                assertEquals("SCHEMA", ((DropDefaultValueChange) changes[0]).getSchemaName());

                assertTrue(changes[1] instanceof DropColumnChange);
                assertEquals("TAB", ((DropColumnChange) changes[1]).getTableName());
                assertEquals("NEWCOL", ((DropColumnChange) changes[1]).getColumnName());
                assertEquals("SCHEMA", ((DropColumnChange) changes[1]).getSchemaName());
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
            @Override
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
