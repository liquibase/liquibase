package liquibase.actionlogic.core;

import ch.qos.logback.classic.db.names.TableName;
import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.*;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.DropColumnChange;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeColumnsLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return MergeColumnsAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        List<Action> actions = new ArrayList<>();

        ObjectName tableName = action.get(MergeColumnsAction.Attr.tableName, ObjectName.class);
        String finalColumnName = action.get(MergeColumnsAction.Attr.finalColumnName, String.class);
        String finalColumnType = action.get(MergeColumnsAction.Attr.finalColumnType, String.class);
        String column1Name = action.get(MergeColumnsAction.Attr.column1Name, String.class);
        String column2Name = action.get(MergeColumnsAction.Attr.column2Name, String.class);

        actions.add((Action) new AddColumnsAction()
                        .set(AddColumnsAction.Attr.tableName, tableName)
                        .add(AddColumnsAction.Attr.columnDefinitions, new ColumnDefinition(finalColumnName, finalColumnType))
        );

        actions.add((Action) new UpdateSqlAction("UPDATE " + database.escapeObjectName(tableName, Table.class) +
                " SET " + database.escapeObjectName(finalColumnName, Column.class)
                + " = " + database.getConcatSql(database.escapeObjectName(column1Name, Column.class)
                , "'" + action.get(MergeColumnsAction.Attr.joinString, String.class) + "'", database.escapeObjectName(column2Name, Column.class))));

//        if (database instanceof SQLiteDatabase) {
            // SQLite does not support this ALTER TABLE operation until now.
            // For more information see: http://www.sqlite.org/omitted.html
            // This is a small work around...

            // define alter table logic
//todo: move with action            AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
//                @Override
//                public ColumnConfig[] getColumnsToAdd() {
//                    ColumnConfig[] new_columns = new ColumnConfig[1];
//                    ColumnConfig new_column = new ColumnConfig();
//                    new_column.setName(getFinalColumnName());
//                    new_column.setType(getFinalColumnType());
//                    new_columns[0] = new_column;
//                    return new_columns;
//                }
//
//                @Override
//                public boolean copyThisColumn(ColumnConfig column) {
//                    return !(column.getName().equals(getColumn1Name()) ||
//                            column.getName().equals(getColumn2Name()));
//                }
//
//                @Override
//                public boolean createThisColumn(ColumnConfig column) {
//                    return !(column.getName().equals(getColumn1Name()) ||
//                            column.getName().equals(getColumn2Name()));
//                }
//
//                @Override
//                public boolean createThisIndex(Index index) {
//                    return !(index.getColumns().contains(getColumn1Name()) ||
//                            index.getColumns().contains(getColumn2Name()));
//                }
//            };
//
//            try {
//                // alter table
//                statements.addAll(SQLiteDatabase.getAlterTableStatements(
//                        rename_alter_visitor,
//                        database, getCatalogName(), getSchemaName(), getTableName()));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        } else {
            // ...if it is not a SQLite database

            actions.add((Action) new DropColumnsAction()
                    .set(DropColumnsAction.Attr.tableName, tableName)
                    .set(DropColumnsAction.Attr.columnNames, new String[]{column1Name}));

        actions.add((Action) new DropColumnsAction()
                .set(DropColumnsAction.Attr.tableName, tableName)
                .set(DropColumnsAction.Attr.columnNames, new String[] {column2Name}));

        return new DelegateResult(actions);
    }
}
