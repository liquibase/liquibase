package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drops an existing column from a table.
 */
@DatabaseChange(name = "dropColumn", description = "Drop existing column(s).", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column")
public class DropColumnChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {
    
    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String tableName;
    @Setter
    private String columnName;
    private List<ColumnConfig> columns = new ArrayList<>();
    
    @Override
    public boolean generateStatementsVolatile(Database database) {
        if (database instanceof SQLiteDatabase) {
            return true;
        }
        return super.generateStatementsVolatile(database);
    }
    
    @Override
    public boolean supports(Database database) {
        return database instanceof SQLiteDatabase || !(database instanceof Db2zDatabase) && super.supports(database);
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
    
    @DatabaseChangeProperty(requiredForDatabase = "none", mustEqualExisting = "column",
        description = "Name of the column to drop, if dropping a single column. Ignore if nested columns are defined")
    public String getColumnName() {
        return columnName;
    }

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting = "column.relation.schema.catalog",
        description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table containing the column to drop",
        mustEqualExisting = "column.relation")
    public String getTableName() {
        return tableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        try {
            if (isMultiple()) {
                return generateMultipleColumns(database);
            } else {
                return generateSingleColumn(database);
            }
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
    
    private SqlStatement[] generateMultipleColumns(Database database) throws DatabaseException {
        List<SqlStatement> statements = new ArrayList<>();
        List<DropColumnStatement> dropStatements = new ArrayList<>();

        if (database instanceof SQLiteDatabase) {
            // SQLite is special in that it needs multiple SQL statements (i.e. a whole table recreation!) to drop
            // a single column.
            statements.addAll(Arrays.asList(generateStatementsForSQLiteDatabase(database)));
        } else {
            for (ColumnConfig column : columns) {
                dropStatements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), column
                .getName()));
            }
        }
        
        if (dropStatements.size() == 1) {
            statements.add(dropStatements.get(0));
        } else if (dropStatements.size() > 1) {
            statements.add(new DropColumnStatement(dropStatements));
        }
        
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
        }
        
        return statements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
    }
    
    private SqlStatement[] generateSingleColumn(Database database) throws DatabaseException {
        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(database);
        }
        
        List<SqlStatement> statements = new ArrayList<>();
        
        statements.add(new DropColumnStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
        }
        
        return statements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
    }
    
    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(
                !SnapshotGeneratorFactory.getInstance().has(
                    new Column(
                        Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()
                    ), database
                ), "Column exists"
            );
        } catch (InvalidExampleException|DatabaseException e) {
            return new ChangeStatus().unknown(e);
        }
    
    }
    
    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) throws DatabaseException {
        SqlStatement[] sqlStatements = null;
        // Since SQLite does not support a drop column statement, use alter table visitor to copy the table
        // except for the column (and index containing that column) to delete.

        Set<String> removedColumnNames = columns.stream().map(ColumnConfig::getName).collect(Collectors.toSet());

        SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
            @Override
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                return !removedColumnNames.contains(column.getName());
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                return !removedColumnNames.contains(column.getName());
            }

            @Override
            public boolean createThisIndex(Index index) {
                // don't create the index if it has the column we are dropping
                boolean indexContainsColumn = false;
                for (Column column : index.getColumns()) {
                    if (removedColumnNames.contains(column.getName())) {
                        indexContainsColumn = true;
                        break;
                    }
                }
                return !indexContainsColumn;
            }
        };
        List<SqlStatement> statements = SQLiteDatabase.getAlterTableStatements(alterTableVisitor, database, getCatalogName(), getSchemaName(), getTableName());
        if (statements.size() > 0) {
            sqlStatements = new SqlStatement[statements.size()];
            return statements.toArray(sqlStatements);
        }
        return sqlStatements;
    }
    
    @Override
    public String getConfirmationMessage() {
        if (isMultiple()) {
            List<String> names = new ArrayList<>(columns.size());
            for (ColumnConfig column : columns) {
                names.add(column.getName());
            }
            return "Columns " + StringUtil.join(names, ",") + " dropped from " + getTableName();
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
        if ("columns".equals(field) && ((List<?>) value).isEmpty()) {
            return null;
        }
        return value;
    }
    
    @Override
    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }
    
    @Override
    @DatabaseChangeProperty(
        description = "Columns to be dropped if dropping multiple columns. If this is populated, the columnName attribute is ignored.", requiredForDatabase = "none")
    public List<ColumnConfig> getColumns() {
        return columns;
    }
    
    @Override
    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }
    
    private boolean isMultiple() {
        return (columns != null) && !columns.isEmpty();
    }
}
