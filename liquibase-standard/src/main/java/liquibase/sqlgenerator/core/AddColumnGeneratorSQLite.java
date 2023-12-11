package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.core.AddColumnStatement;
import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Workaround for adding column on existing table for SQLite.
 */
public class AddColumnGeneratorSQLite extends AddColumnGenerator {

    @Override
    public ValidationErrors validate(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        validationErrors.checkRequiredField("tableName", statement);
        validationErrors.checkRequiredField("columnName", statement);
        return validationErrors;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        // need metadata for copying the table
        return true;
    }

    @Override
    public boolean supports(AddColumnStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public Sql[] generateSql(final AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Workaround implemented by replacing a table with a new one (duplicate)
        // with a new column added

        final List<AddColumnStatement> columns = new ArrayList<>(statement.getColumns());
        if (columns.size() == 0) {
            columns.add(statement);
        }

        Set<String> newColumnNames = columns.stream().map(AddColumnStatement::getColumnName).collect(Collectors.toSet());

        Sql[] generatedSqls;
        SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
            @Override
            public ColumnConfig[] getColumnsToAdd() {

                ColumnConfig[] columnConfigs = new ColumnConfig[columns.size()];

                int i = 0;
                for (AddColumnStatement column : columns) {
                    ColumnConfig newColumn = new ColumnConfig();
                    newColumn.setName(column.getColumnName());
                    newColumn.setType(column.getColumnType());
                    newColumn.setAutoIncrement(column.isAutoIncrement());
                    ConstraintsConfig constraintsConfig = new ConstraintsConfig();
                    if (column.isPrimaryKey()) {
                        constraintsConfig.setPrimaryKey(true);
                    }
                    if (column.isNullable()) {
                        constraintsConfig.setNullable(true);
                    }
                    if (column.isUnique()) {
                        constraintsConfig.setUnique(true);
                    }
                    newColumn.setConstraints(constraintsConfig);

                    for (ColumnConstraint constraint : column.getConstraints()) {
                        if (constraint instanceof ForeignKeyConstraint) {
                            final ForeignKeyConstraint fkConstraint = (ForeignKeyConstraint) constraint;
                            constraintsConfig.setReferencedTableCatalogName(fkConstraint.getReferencedTableCatalogName());
                            constraintsConfig.setReferencedTableSchemaName(fkConstraint.getReferencedTableSchemaName());
                            constraintsConfig.setReferencedTableName(fkConstraint.getReferencedTableName());
                            constraintsConfig.setReferencedColumnNames(fkConstraint.getReferencedColumnNames());
                            constraintsConfig.setReferences(fkConstraint.getReferences());

                            constraintsConfig.setForeignKeyName(fkConstraint.getForeignKeyName());
                            if (fkConstraint.isDeleteCascade()) {
                                constraintsConfig.setDeleteCascade(true);
                            }
                        }
                    }

                    columnConfigs[i++] = newColumn;
                }

                return columnConfigs;
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                return !newColumnNames.contains(column.getName());
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                return true;
            }

            @Override
            public boolean createThisIndex(Index index) {
                return true;
            }

        };

        final String catalogName = columns.get(0).getCatalogName();
        final String schemaName = columns.get(0).getSchemaName();
        final String tableName = columns.get(0).getTableName();

        generatedSqls = SQLiteDatabase.getAlterTableSqls(database, alterTableVisitor, catalogName,
                schemaName, tableName);

        return generatedSqls;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
}
