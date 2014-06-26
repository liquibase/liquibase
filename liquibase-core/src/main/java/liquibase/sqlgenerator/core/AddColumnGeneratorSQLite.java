package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.change.ColumnConfig;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddColumnStatement;
import liquibase.structure.core.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddColumnGeneratorSQLite extends AddColumnGenerator {
     @Override
     public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddColumnStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof SQLiteDatabase;
    }

    @Override
    public boolean generateActionsIsVolatile(ExecutionEnvironment env) {
        return true;
    }

    @Override
    public Action[] generateActions(final AddColumnStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        // SQLite does not support this ALTER TABLE operation until now.
        // For more information see: http://www.sqlite.org/omitted.html.
        // This is a small work around...

        List<Action> sql = new ArrayList<Action>();

        // define alter table logic
        SQLiteDatabase.AlterTableVisitor rename_alter_visitor =
        new SQLiteDatabase.AlterTableVisitor() {
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[] {
                    new ColumnConfig()
                            .setName(statement.getColumnName())
                        .setType(statement.getColumnType())
                        .setAutoIncrement(statement.isAutoIncrement())
                };
            }
            public boolean copyThisColumn(ColumnConfig column) {
                return !column.getName().equals(statement.getColumnName());
            }
            public boolean createThisColumn(ColumnConfig column) {
                return true;
            }
            public boolean createThisIndex(Index index) {
                return true;
            }
        };

        try {
            // alter table
            List<Statement> alterTableStatements = SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, env, statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
            sql.addAll(Arrays.asList(StatementLogicFactory.getInstance().generateActions(alterTableStatements.toArray(new Statement[alterTableStatements.size()]), env)));
        } catch (DatabaseException e) {
            System.err.println(e);
            e.printStackTrace();
        }

        return sql.toArray(new Action[sql.size()]);
    }
}
