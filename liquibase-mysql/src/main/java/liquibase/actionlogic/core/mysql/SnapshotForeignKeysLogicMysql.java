package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.QuerySqlAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.core.SnapshotForeignKeysLogicJdbc;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.ForeignKey;
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
                "CONSTRAINT_SCHEMA AS FKTABLE_CAT, " +
                "CONSTRAINT_NAME AS FK_NAME, " +
                "REFERENCED_TABLE_SCHEMA AS PKTABLE_CAT, " +
                "REFERENCED_TABLE_NAME AS PKTABLE_NAME, " +
                "REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME, " +
                "TABLE_SCHEMA AS FKTABLE_CAT, " +
                "TABLE_NAME AS FKTABLE_NAME, " +
                "COLUMN_NAME AS FKCOLUMN_NAME, " +
                "ORDINAL_POSITION AS KEY_SEQ " +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE REFERENCED_COLUMN_NAME IS NOT NULL");
        if (action.relatedTo.instanceOf(ForeignKey.class)) {
            query.append("AND CONSTRAINT_NAME='" + action.relatedTo.getSimpleName() + "'")
                    .append("AND CONSTRAINT_SCHEMA='" + action.relatedTo.objectName.container + "'");
        } else if (action.relatedTo.instanceOf(Table.class)) {
            query.append("AND TABLE_NAME_NAME='" + action.relatedTo.getSimpleName() + "'")
                    .append("AND TABLE_SCHEMA='" + action.relatedTo.objectName.container + "'");
        } else if (action.relatedTo.instanceOf(Schema.class)) {
            query.append("AND CONSTRAINT_SCHEMA='" + action.relatedTo.getSimpleName() + "'");
        } else {
            throw new ActionPerformException("Unexpected relatedTo type: " + action.relatedTo.objectType.getName());
        }
        return new QuerySqlAction(query);
    }
}
