package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.action.Action;
import liquibase.executor.ExecutionOptions;
import liquibase.structure.core.Index;
import liquibase.exception.DatabaseException;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQLite does not support this ALTER TABLE operation until now.
 * For more information see: http://www.sqlite.org/omitted.html.
 * This is a small work around...
 */
public class AddAutoIncrementGeneratorSQLite extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            ExecutionOptions options,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());


        return validationErrors;
    }

    @Override
    public boolean generateStatementsIsVolatile(ExecutionOptions options) {
        return true;
    }

    @Override
    public Sql[] generateSql(final AddAutoIncrementStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        List<Action> statements = new ArrayList<Action>();

        // define alter table logic
        SQLiteDatabase.AlterTableVisitor rename_alter_visitor = new SQLiteDatabase.AlterTableVisitor() {
            @Override
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                return true;
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                if (column.getName().equals(statement.getColumnName())) {
                    column.setAutoIncrement(true);
                    column.setConstraints(new ConstraintsConfig().setPrimaryKey(true));
                    column.setType("INTEGER");
                }
                return true;
            }

            @Override
            public boolean createThisIndex(Index index) {
                return true;
            }
        };

        try {
            // alter table
            List<SqlStatement> alterTableStatements = SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, options, statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
            statements.addAll( Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(alterTableStatements.toArray(new SqlStatement[alterTableStatements.size()]), options)));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return statements.toArray(new Sql[statements.size()]);
    }
}