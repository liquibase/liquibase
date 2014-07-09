package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReindexStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Drops an existing column from a table.
 */
@DatabaseChange(name = "dropColumn", description = "Drop an existing column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class DropColumnChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

    @Override
    public boolean generateStatementsVolatile(ExecutionEnvironment env) {
        if (env.getTargetDatabase() instanceof SQLiteDatabase) {
            return true;
        }
        return super.generateStatementsVolatile(env);
    }

    @Override
    public boolean supports(ExecutionEnvironment env) {
        if (env.getTargetDatabase() instanceof SQLiteDatabase) {
            return true;
        }
        return super.supports(env);
    }

    @Override
    public ValidationErrors validate(ExecutionEnvironment env) {
        if (env.getTargetDatabase() instanceof SQLiteDatabase) {
            ValidationErrors validationErrors = new ValidationErrors();
            validationErrors.checkRequiredField("tableName", tableName);
            validationErrors.checkRequiredField("columnName", columnName);

            return validationErrors;
        }
        return super.validate(env);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to drop")
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
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
    public Statement[] generateStatements(ExecutionEnvironment env) {

        Database database = env.getTargetDatabase();

        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(env);
        }

        List<Statement> statements = new ArrayList<Statement>();

        statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName()));
        if (database instanceof DB2Database) {
            statements.add(new ReindexStatement(getCatalogName(), getSchemaName(), getTableName()));
        }

        return statements.toArray(new Statement[statements.size()]);
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), env.getTargetDatabase()), "Column exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    private Statement[] generateStatementsForSQLiteDatabase(ExecutionEnvironment env) {

        // SQLite does not support this ALTER TABLE operation until now.
        // For more information see: http://www.sqlite.org/omitted.html.
        // This is a small work around...

        List<Statement> statements = new ArrayList<Statement>();

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
                    env, getCatalogName(), getSchemaName(), getTableName()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return statements.toArray(new Statement[statements.size()]);
    }

    @Override
    public String getConfirmationMessage() {
        return "Column " + getTableName() + "." + getColumnName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
