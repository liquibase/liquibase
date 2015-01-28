package liquibase.actionlogic.core.sqlite;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.core.AddAutoIncrementLogic;
import liquibase.database.Database;
import liquibase.database.core.sqlite.SQLiteDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

/**
 * SQLite does not support this ALTER TABLE operation until now.
 * For more information see: http://www.sqlite.org/omitted.html.
 * This is a small work around...
 */
public class AddAutoIncrementLogicSQLite extends AddAutoIncrementLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SQLiteDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .removeRequiredField(AddAutoIncrementAction.Attr.columnDataType);
    }

//    @Override
//    public boolean generateStatementsIsVolatile(Database database) {
//        return true;
//    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
//        List<Sql> statements = new ArrayList<Sql>();
//
//        // define alter table logic
//        SQLiteDatabase.AlterTableVisitor rename_alter_visitor = new SQLiteDatabase.AlterTableVisitor() {
//            @Override
//            public ColumnConfig[] getColumnsToAdd() {
//                return new ColumnConfig[0];
//            }
//
//            @Override
//            public boolean copyThisColumn(ColumnConfig column) {
//                return true;
//            }
//
//            @Override
//            public boolean createThisColumn(ColumnConfig column) {
//                if (column.getName().equals(statement.getColumnName())) {
//                    column.setAutoIncrement(true);
//                    column.setConstraints(new ConstraintsConfig().setPrimaryKey(true));
//                    column.setType("INTEGER");
//                }
//                return true;
//            }
//
//            @Override
//            public boolean createThisIndex(Index index) {
//                return true;
//            }
//        };
//
//        try {
//            // alter table
//            List<SqlStatement> alterTableStatements = SQLiteDatabase.getAlterTableStatements(rename_alter_visitor, database, statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
//            statements.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(alterTableStatements.toArray(new SqlStatement[alterTableStatements.size()]), database)));
//        } catch (DatabaseException e) {
//            e.printStackTrace();
//        }
//
//        return statements.toArray(new Sql[statements.size()]);

        return null; //todo
    }
}