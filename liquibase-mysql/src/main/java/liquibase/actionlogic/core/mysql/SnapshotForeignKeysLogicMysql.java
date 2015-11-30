package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.QuerySqlAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.actionlogic.core.SnapshotForeignKeysLogicJdbc;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringClauses;

public class SnapshotForeignKeysLogicMysql extends SnapshotForeignKeysLogicJdbc {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    public int getPriority(SnapshotDatabaseObjectsAction action, Scope scope) {
        return super.getPriority(action, scope);
    }

    @Override
    protected Action createSnapshotAction(SnapshotDatabaseObjectsAction action, Scope scope) throws ActionPerformException {
        StringClauses query = new StringClauses(" ").append("SELECT " +
                "KEY_COL.CONSTRAINT_SCHEMA AS FKTABLE_CAT, " +
                "KEY_COL.CONSTRAINT_NAME AS FK_NAME, " +
                "KEY_COL.REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT, " +
                "KEY_COL.REFERENCED_TABLE_NAME AS PKTABLE_NAME, " +
                "KEY_COL.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, " +
                "KEY_COL.TABLE_SCHEMA AS FKTABLE_CAT, " +
                "KEY_COL.TABLE_NAME AS FKTABLE_NAME, " +
                "KEY_COL.COLUMN_NAME AS FKCOLUMN_NAME, " +
                "KEY_COL.ORDINAL_POSITION AS KEY_SEQ, " +
                "CON.UPDATE_RULE AS UPDATE_RULE_STRING, "+
                "CON.DELETE_RULE AS DELETE_RULE_STRING "+
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE KEY_COL " +
                "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS CON " +
                "ON KEY_COL.CONSTRAINT_SCHEMA=CON.CONSTRAINT_SCHEMA " +
                "AND KEY_COL.CONSTRAINT_NAME=CON.CONSTRAINT_NAME "+
                "AND KEY_COL.TABLE_NAME=CON.TABLE_NAME "+
                "WHERE KEY_COL.REFERENCED_COLUMN_NAME IS NOT NULL");
        if (action.relatedTo.instanceOf(ForeignKey.class)) {
            if (action.relatedTo.name== null) {
                ObjectReference baseTable = ((ForeignKey.ForeignKeyReference) action.relatedTo).table;
                query.append("AND KEY_COL.TABLE_NAME='" + baseTable.name + "'")
                        .append("AND KEY_COL.TABLE_SCHEMA='" + baseTable.container.name + "'");
            } else {
                query.append("AND KEY_COL.CONSTRAINT_NAME='" + action.relatedTo.name + "'")
                        .append("AND KEY_COL.CONSTRAINT_SCHEMA='" + action.relatedTo.container.name + "'");
            }
        } else if (action.relatedTo.instanceOf(Table.class)) {
            query.append("AND KEY_COL.TABLE_NAME_NAME='" + action.relatedTo.name + "'")
                    .append("AND KEY_COL.TABLE_SCHEMA='" + action.relatedTo.container.name + "'");
        } else if (action.relatedTo.instanceOf(Schema.class)) {
            query.append("AND KEY_COL.CONSTRAINT_SCHEMA='" + action.relatedTo.name + "'");
        } else {
            throw new ActionPerformException("Unexpected relatedTo type: " + action.relatedTo.getClass().getName());
        }
        return new QuerySqlAction(query);
    }

    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, SnapshotDatabaseObjectsAction originalAction, Scope scope) throws ActionPerformException {
        ForeignKey fk = (ForeignKey) super.convertToObject(row, originalAction, scope);

        String updateRule = row.get("UPDATE_RULE_STRING", String.class);
        if (updateRule != null) {
            switch (updateRule) {
                case "CASCADE": fk.updateRule = ForeignKeyConstraintType.importedKeyCascade; break;
                case "SET NULL": fk.updateRule = ForeignKeyConstraintType.importedKeySetNull; break;
                case "SET DEFAULT": fk.updateRule = ForeignKeyConstraintType.importedKeySetDefault; break;
                case "RESTRICT": fk.updateRule = ForeignKeyConstraintType.importedKeyRestrict; break;
                case "NO ACTION": fk.updateRule = ForeignKeyConstraintType.importedKeyNoAction; break;
                default: throw new ActionPerformException("Unknown update rule: "+updateRule);
            }
        }

        String deleteRule = row.get("DELETE_RULE_STRING", String.class);
        if (deleteRule != null) {
            switch (deleteRule) {
                case "CASCADE": fk.deleteRule = ForeignKeyConstraintType.importedKeyCascade; break;
                case "SET NULL": fk.deleteRule = ForeignKeyConstraintType.importedKeySetNull; break;
                case "SET DEFAULT": fk.deleteRule = ForeignKeyConstraintType.importedKeySetDefault; break;
                case "RESTRICT": fk.deleteRule = ForeignKeyConstraintType.importedKeyRestrict; break;
                case "NO ACTION": fk.deleteRule = ForeignKeyConstraintType.importedKeyNoAction; break;
                default: throw new ActionPerformException("Unknown delete rule: "+deleteRule);
            }
        }

        return fk;
    }
}
