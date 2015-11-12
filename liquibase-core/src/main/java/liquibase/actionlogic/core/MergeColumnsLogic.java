package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.*;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergeColumnsLogic extends AbstractActionLogic<MergeColumnsAction> {

    @Override
    protected Class<MergeColumnsAction> getSupportedAction() {
        return MergeColumnsAction.class;
    }

    @Override
    public ActionResult execute(MergeColumnsAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        List<Action> actions = new ArrayList<>();

        ObjectReference tableName = action.tableName;
        String finalColumnName = action.finalColumnName;
        String finalColumnType = action.finalColumnType;
        String column1Name = action.column1Name;
        String column2Name = action.column2Name;

        AddColumnsAction addColumnsAction = new AddColumnsAction();
        addColumnsAction.columns = Collections.singletonList(new Column(new ObjectReference(action.tableName, finalColumnName), finalColumnType));
        actions.add(addColumnsAction);

        actions.add(new UpdateSqlAction("UPDATE " + database.escapeObjectName(tableName) +
                " SET " + database.escapeObjectName(finalColumnName, Column.class)
                + " = " + database.getConcatSql(database.escapeObjectName(column1Name, Column.class)
                , "'" + action.joinString + "'", database.escapeObjectName(column2Name, Column.class))));

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

        DropColumnsAction dropColumn1Action = new DropColumnsAction();
        dropColumn1Action.tableName = tableName;
        dropColumn1Action.columnNames = Collections.singletonList(column1Name);
        actions.add(dropColumn1Action);

        DropColumnsAction dropColumn2Action = new DropColumnsAction();
        dropColumn2Action.tableName = tableName;
        dropColumn2Action.columnNames = Collections.singletonList(column2Name);
        actions.add(dropColumn2Action);

        return new DelegateResult(actions);
    }
}
