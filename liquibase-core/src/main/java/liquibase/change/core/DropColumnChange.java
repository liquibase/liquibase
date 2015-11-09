package liquibase.change.core;

import liquibase.action.ActionStatus;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Drops an existing column from a table.
 */
@DatabaseChange(name = "dropColumn", description = "Drop existing column(s)", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class DropColumnChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return super.generateStatementsVolatile(database);
    }

    @Override
    public boolean supports(Database database) {
        return super.supports(database);
    }

    @Override
    public ValidationErrors validate(Database database) {
        return super.validate(database);
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to drop", requiredForDatabase = "none")
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
    public SqlStatement[] generateStatements(Database database) {
        if (isMultiple()) {
            return generateMultipeColumns(database);
        } else {
            return generateSingleColumn(database);
        }
    }

    private SqlStatement[] generateMultipeColumns(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        List<DropColumnStatement> dropStatements = new ArrayList<DropColumnStatement>();

        for (ColumnConfig column : columns) {
//            if (database instanceof SQLiteDatabase) {
//                statements.addAll(Arrays.asList(generateStatementsForSQLiteDatabase(database, column.getName())));
//            } else {
                dropStatements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), column.getName()));
//            }
        }

        if (dropStatements.size() == 1) {
            statements.add(dropStatements.get(0));
        } else {
            statements.add(new DropColumnStatement(dropStatements));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateSingleColumn(Database database) {

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName()));
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    public ActionStatus checkStatus(Database database) {
        try {
            return new ActionStatus().assertApplied(!SnapshotGeneratorFactory.getInstance().has(new Column(new ObjectReference(getCatalogName(), getSchemaName(), getTableName(), getColumnName())), database), "Column exists");
        } catch (Exception e) {
            return new ActionStatus().unknown(e);
        }

    }

//TODO    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database, final String columnName) {
//
//        // SQLite does not support this ALTER TABLE operation until now.
//        // For more information see: http://www.sqlite.org/omitted.html.
//        // This is a small work around...
//
//        List<SqlStatement> statements = new ArrayList<SqlStatement>();
//
//        // define alter table logic
//        SQLiteDatabase.AlterTableVisitor rename_alter_visitor = new SQLiteDatabase.AlterTableVisitor() {
//            public ColumnConfig[] getColumnsToAdd() {
//                return new ColumnConfig[0];
//            }
//
//            public boolean createThisColumn(ColumnConfig column) {
//                return !column.getName().equals(columnName);
//            }
//
//            public boolean copyThisColumn(ColumnConfig column) {
//                return !column.getName().equals(columnName);
//            }
//
//            public boolean createThisIndex(Index index) {
//                return !index.getColumns().contains(columnName);
//            }
//        };
//
//        try {
//            // alter table
//            statements.addAll(SQLiteDatabase.getAlterTableStatements(rename_alter_visitor,
//                    database, getCatalogName(), getSchemaName(), getTableName()));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return statements.toArray(new SqlStatement[statements.size()]);
//    }

    @Override
    public String getConfirmationMessage() {
        if (isMultiple()) {
            List<String> names = new ArrayList<String>(columns.size());
            for (ColumnConfig column : columns) {
                names.add(column.getName());
            }
            return "Columns " + StringUtils.join(names, ",") + " dropped from " + getTableName();
        } else {
            return "Column " + getTableName() + "." + getColumnName() + " dropped";
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object value = super.getSerializableFieldValue(field);
        if (field.equals("columns") && ((List) value).size() == 0) {
            return null;
        }
        return value;
    }

    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }

    @Override
    @DatabaseChangeProperty(description = "Columns to be dropped.", requiredForDatabase = "none")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    private boolean isMultiple() {
        return columns != null && !columns.isEmpty();
    }
}
