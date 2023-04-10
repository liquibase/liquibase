package liquibase.change.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.Scope;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

import static liquibase.statement.SqlStatement.EMPTY_SQL_STATEMENT;

/**
 * Extracts data from an existing column to create a lookup table.
 * A foreign key is created between the old column and the new lookup table.
 */
@DatabaseChange(name = "addLookupTable", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column",
        description = "Creates a lookup table containing values stored in a column and creates a foreign key to the new table.")
public class AddLookupTableChange extends AbstractChange {

    private String existingTableCatalogName;
    private String existingTableSchemaName;
    private String existingTableName;
    private String existingColumnName;

    private String newTableCatalogName;
    private String newTableSchemaName;
    private String newTableName;
    private String newColumnName;
    private String newColumnDataType;
    private String constraintName;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        if (database instanceof Db2zDatabase) {
            if (this.getNewColumnDataType() == null) {
                errors.addError("newColumnDataType is required for " + Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(this).getName() + " on " + database.getShortName());
            }
        }
        return errors;
    }

    @DatabaseChangeProperty(description = "Name of the database catalog of the existing table")
    public String getExistingTableCatalogName() {
        return existingTableCatalogName;
    }

    public void setExistingTableCatalogName(String existingTableCatalogName) {
        this.existingTableCatalogName = existingTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema", description = "Name of the database schema where the table containing data to extract resides")
    public String getExistingTableSchemaName() {
        return existingTableSchemaName;
    }

    public void setExistingTableSchemaName(String existingTableSchemaName) {
        this.existingTableSchemaName = existingTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing the data to extract",
        exampleValue = "address")
    public String getExistingTableName() {
        return existingTableName;
    }

    public void setExistingTableName(String existingTableName) {
        this.existingTableName = existingTableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column containing the data to extract",
        exampleValue = "state")
    public String getExistingColumnName() {
        return existingColumnName;
    }

    public void setExistingColumnName(String existingColumnName) {
        this.existingColumnName = existingColumnName;
    }

    @DatabaseChangeProperty(since = "3.0", description = "Name of the database catalog for the lookup table")
    public String getNewTableCatalogName() {
        return newTableCatalogName;
    }

    public void setNewTableCatalogName(String newTableCatalogName) {
        this.newTableCatalogName = newTableCatalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema for the lookup table")
    public String getNewTableSchemaName() {
        return newTableSchemaName;
    }

    public void setNewTableSchemaName(String newTableSchemaName) {
        this.newTableSchemaName = newTableSchemaName;
    }

    @DatabaseChangeProperty(description = "Name of lookup table to create", exampleValue = "state")
    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    @DatabaseChangeProperty(description = "Name of the column in the new table to create", exampleValue = "abbreviation")
    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    @DatabaseChangeProperty(description = "Data type of the new table column", exampleValue = "char(2)")
    public String getNewColumnDataType() {
        return newColumnDataType;
    }

    public void setNewColumnDataType(String newColumnDataType) {
        this.newColumnDataType = newColumnDataType;
    }

    @DatabaseChangeProperty(description = "Name of the foreign key constraint to create between the existing table and the lookup table",
        exampleValue = "fk_address_state")
    public String getConstraintName() {
        return constraintName;
    }

    public String getFinalConstraintName() {
        if (constraintName == null) {
            return ("FK_" + getExistingTableName() + "_" + getNewTableName()).toUpperCase();
        } else {
            return constraintName;
        }
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    @Override
    public boolean supports(Database database) {
        if (database instanceof HsqlDatabase) {
            return false;
        }
        return super.supports(database);
    }

    @Override
    protected Change[] createInverses() {
        DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
        dropFK.setBaseTableSchemaName(getExistingTableSchemaName());
        dropFK.setBaseTableName(getExistingTableName());
        dropFK.setConstraintName(getFinalConstraintName());

        DropTableChange dropTable = new DropTableChange();
        dropTable.setSchemaName(getNewTableSchemaName());
        dropTable.setTableName(getNewTableName());

        return new Change[]{
                dropFK,
                dropTable,
        };
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        String newTableCatalogName = getNewTableCatalogName();
        String newTableSchemaName = getNewTableSchemaName();

        String existingTableCatalogName = getExistingTableCatalogName();
        String existingTableSchemaName = getExistingTableSchemaName();

        SqlStatement[] createTablesSQL = {new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " AS SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL")};
        if (database instanceof MSSQLDatabase) {
            createTablesSQL = new SqlStatement[]{new RawSqlStatement("SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),};
        } else if (database instanceof SybaseASADatabase) {
            createTablesSQL = new SqlStatement[]{new RawSqlStatement("SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),};
        } else if (database instanceof Db2zDatabase) {
            CreateTableStatement tableStatement = new CreateTableStatement(newTableCatalogName, newTableSchemaName, getNewTableName());
            if (getNewColumnName() != null) {
                tableStatement.addColumn(getNewColumnName(), DataTypeFactory.getInstance().fromDescription(getNewColumnDataType(), database));
                tableStatement.addColumnConstraint(new NotNullConstraint(getNewColumnName()));
            }
            createTablesSQL = new SqlStatement[]{
                    tableStatement,
                    new RawSqlStatement("INSERT INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),
            };
        } else if (database instanceof DB2Database) {
            createTablesSQL = new SqlStatement[]{
                    new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " AS (SELECT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " AS " + database.escapeObjectName(getNewColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + ") WITH NO DATA"),
                    new RawSqlStatement("INSERT INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " SELECT DISTINCT " + database.escapeObjectName(getExistingColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),
            };
        } else if (database instanceof InformixDatabase) {
            createTablesSQL = new SqlStatement[] {
                    new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " ( "  + database.escapeObjectName(getNewColumnName(), Column.class) + " " + getNewColumnDataType() + " )"),
                    new RawSqlStatement("INSERT INTO " + database.escapeTableName(newTableCatalogName, newTableSchemaName, getNewTableName()) + " ( "  + database.escapeObjectName(getNewColumnName(), Column.class) + " ) SELECT DISTINCT "  + database.escapeObjectName(getExistingColumnName(), Column.class) + " FROM " + database.escapeTableName(existingTableCatalogName, existingTableSchemaName, getExistingTableName()) + " WHERE " + database.escapeObjectName(getExistingColumnName(), Column.class) + " IS NOT NULL"),
            };
        }

        List<SqlStatement> statements = new ArrayList<>(Arrays.asList(createTablesSQL));

        if (!(database instanceof OracleDatabase) && !(database instanceof Db2zDatabase)) {
            AddNotNullConstraintChange addNotNullChange = new AddNotNullConstraintChange();
            addNotNullChange.setSchemaName(newTableSchemaName);
            addNotNullChange.setTableName(getNewTableName());
            addNotNullChange.setColumnName(getNewColumnName());
            addNotNullChange.setColumnDataType(getNewColumnDataType());
            statements.addAll(Arrays.asList(addNotNullChange.generateStatements(database)));
        }

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(newTableCatalogName, newTableSchemaName, getNewTableName()));
        }

        AddPrimaryKeyChange addPKChange = new AddPrimaryKeyChange();
        addPKChange.setSchemaName(newTableSchemaName);
        addPKChange.setTableName(getNewTableName());
        addPKChange.setColumnNames(getNewColumnName());
        statements.addAll(Arrays.asList(addPKChange.generateStatements(database)));

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(newTableCatalogName,newTableSchemaName, getNewTableName()));
        }

        AddForeignKeyConstraintChange addFKChange = new AddForeignKeyConstraintChange();
        addFKChange.setBaseTableSchemaName(existingTableSchemaName);
        addFKChange.setBaseTableName(getExistingTableName());
        addFKChange.setBaseColumnNames(getExistingColumnName());
        addFKChange.setReferencedTableSchemaName(newTableSchemaName);
        addFKChange.setReferencedTableName(getNewTableName());
        addFKChange.setReferencedColumnNames(getNewColumnName());

        addFKChange.setConstraintName(getFinalConstraintName());
        statements.addAll(Arrays.asList(addFKChange.generateStatements(database)));

        return statements.toArray(EMPTY_SQL_STATEMENT);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Table newTableExample = new Table(getNewTableCatalogName(), getNewTableSchemaName(), getNewTableName());
            Column newColumnExample = new Column(Table.class, getNewTableCatalogName(), getNewTableSchemaName(), getNewTableName(), getNewColumnName());

            ForeignKey foreignKeyExample = new ForeignKey(getConstraintName(), getExistingTableCatalogName(), getExistingTableSchemaName(), getExistingTableName());
            foreignKeyExample.setPrimaryKeyTable(newTableExample);
            foreignKeyExample.setForeignKeyColumns(Column.listFromNames(getExistingColumnName()));
            foreignKeyExample.setPrimaryKeyColumns(Column.listFromNames(getNewColumnName()));

            result.assertComplete(SnapshotGeneratorFactory.getInstance().has(newTableExample, database), "New table does not exist");
            result.assertComplete(SnapshotGeneratorFactory.getInstance().has(newColumnExample, database), "New column does not exist");
            result.assertComplete(SnapshotGeneratorFactory.getInstance().has(foreignKeyExample, database), "Foreign key does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Lookup table added for "+getExistingTableName()+"."+getExistingColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
