package liquibase.actionlogic.core.sqlite;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.sqlite.SQLiteDatabase;
import liquibase.exception.ActionPerformException;

public class AddColumnsLogicSQLite extends AddColumnsLogic {

    @Override
     public int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database,Database.class) instanceof SQLiteDatabase;
    }

//    @Override
//    public boolean generateStatementsIsVolatile(Database database) {
//        return true;
//    }


    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
//        // SQLite does not support this ALTER TABLE operation until now.
//        // For more information see: http://www.sqlite.org/omitted.html.
//        // This is a small work around...
//
//        List<Sql> sql = new ArrayList<Sql>();
//
//        // define alter table logic
//        SQLiteDatabase.AlterTableVisitor rename_alter_visitor =
//        new SQLiteDatabase.AlterTableVisitor() {
//            public ColumnConfig[] getColumnsToAdd() {
//                return new ColumnConfig[] {
//                    new ColumnConfig()
//                            .setName(statement.getColumnName())
//                        .setType(statement.getColumnType())
//                        .setAutoIncrement(statement.isAutoIncrement())
//                };
//            }
//            public boolean copyThisColumn(ColumnConfig column) {
//                return !column.getName().equals(statement.getColumnName());
//            }
//            public boolean createThisColumn(ColumnConfig column) {
//                return true;
//            }
//            public boolean createThisIndex(Index index) {
//                return true;
//            }
//        };
//
//        try {
//            // alter table
//            List<SqlStatement> alterTableStatements = SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, database, statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
//            sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(alterTableStatements.toArray(new SqlStatement[alterTableStatements.size()]), database)));
//        } catch (DatabaseException e) {
//            System.err.println(e);
//            e.printStackTrace();
//        }
//
//        return sql.toArray(new Sql[sql.size()]);

        return super.execute(action, scope);
    }
}
