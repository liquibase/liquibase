package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.action.Action;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
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
    public boolean supports(AddColumnStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase;
    }

    @Override
    public boolean generateStatementsIsVolatile(ExecutionOptions options) {
        return true;
    }

    @Override
    public Sql[] generateSql(final AddColumnStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
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
            List<SqlStatement> alterTableStatements = SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, options, statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
            sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(alterTableStatements.toArray(new SqlStatement[alterTableStatements.size()]), options)));
        } catch (DatabaseException e) {
            System.err.println(e);
            e.printStackTrace();
        }

        return sql.toArray(new Sql[sql.size()]);
    }
}
