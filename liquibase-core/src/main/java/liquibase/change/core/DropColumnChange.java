package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterTableStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

/**
 * Drops an existing column from a table.
 */
@DatabaseChange(name = "dropColumn", description = "Drop an existing column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class DropColumnChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

    private List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

    @Override
    public boolean generateStatementsVolatile(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return super.generateStatementsVolatile(database);
    }

    @Override
    public boolean supports(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return super.supports(database);
    }

    @Override
    public ValidationErrors validate(Database database) {
        if (database instanceof SQLiteDatabase) {
            ValidationErrors validationErrors = new ValidationErrors();
            validationErrors.checkRequiredField("tableName", tableName);
            validationErrors.checkRequiredField("columnName", columnName);

            return validationErrors;
        }
        return super.validate(database);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to drop")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @DatabaseChangeProperty(description = "Columns to be dropped.", requiredForDatabase = "none")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing the column to drop")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(database);
        } else if (database instanceof MySQLDatabase) {
            return generateStatementsForMySQLDatabase();
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        if (!columns.isEmpty()) {
            for (ColumnConfig columnConfig : columns) {
                statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), columnConfig.getName()));
                if (database instanceof DB2Database) {
                    statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
                }
            }
        } else {
            statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName()));
            if (database instanceof DB2Database) {
                statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
            }
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), database), "Column exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {

        // SQLite does not support this ALTER TABLE operation until now.
        // For more information see: http://www.sqlite.org/omitted.html.
        // This is a small work around...

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        // define alter table logic
        SQLiteDatabase.AlterTableVisitor rename_alter_visitor = new SQLiteDatabase.AlterTableVisitor() {
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            public boolean createThisColumn(ColumnConfig column) {
                return !column.getName().equals(getColumnName());
            }

            public boolean copyThisColumn(ColumnConfig column) {
                return !column.getName().equals(getColumnName());
            }

            public boolean createThisIndex(Index index) {
                return !index.getColumns().contains(getColumnName());
            }
        };

        try {
            // alter table
            statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor,
                    database, getCatalogName(), getSchemaName(), getTableName()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateStatementsForMySQLDatabase() {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        AlterTableStatement statement = new AlterTableStatement(getCatalogName(), getSchemaName(), getTableName());
        if (!columns.isEmpty()) {
            for (ColumnConfig columnConfig : columns) {
                statement.dropColumn(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), columnConfig.getName()));
            }
        } else {
            statement.dropColumn(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName()));
        }
        statements.add(statement);
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        if (!columns.isEmpty()) {
            List<String> names = new ArrayList<String>(columns.size());
            for (ColumnConfig col : columns) {
                names.add(col.getName());
            }
            return "Columns " + StringUtils.join(names, ",") + " dropped from " + tableName;
        } else {
            return "Column " + getTableName() + "." + getColumnName() + " dropped";
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
