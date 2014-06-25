package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.core.Index;

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
    public boolean supports(AddAutoIncrementStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof SQLiteDatabase;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            ExecutionEnvironment env,
            StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());


        return validationErrors;
    }

    @Override
    public boolean generateActionsIsVolatile(ExecutionEnvironment env) {
        return true;
    }

    @Override
    public Action[] generateActions(final AddAutoIncrementStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
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
            List<Statement> alterTableStatements = SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, env, statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
            statements.addAll( Arrays.asList(StatementLogicFactory.getInstance().generateActions(alterTableStatements.toArray(new Statement[alterTableStatements.size()]), env)));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        return statements.toArray(new Action[statements.size()]);
    }
}