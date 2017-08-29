package liquibase.sqlgenerator.replace;

import liquibase.database.Database;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplaceIndexGenerator extends AbstractDb2ZosReplaceGenerator {
    private CreateIndexStatement statement;

    public ReplaceIndexGenerator(CreateIndexStatement statement) {
        this.statement = statement;
    }

    @Override
    public List<Sql> generateReplacementSql(Database database, List<Sql> statementSql) {
        Map<String, ?> indexInfo = getObjectInfo(database);
        if (indexInfo != null) {
            String ownerIndexName = getIndexOwnerName(indexInfo);

            List<Sql> sqlList = new ArrayList<>();
            sqlList.add(getDropIndexSql(ownerIndexName));
            sqlList.add(getCreateIndexSql(statementSql.get(0)));
            sqlList.add(getRebuildIndex(ownerIndexName));
            sqlList.add(getRunStatsSql(ownerIndexName));
            sqlList.addAll(getRebindPackagesSql(database, indexInfo.get("TABLE_SCHEMA"), indexInfo.get("TBNAME"), ObjectType.INDEX));
            return sqlList;
        }
        return statementSql;
    }

    private String getIndexOwnerName(Map<String, ?> indexInfo) {
        String ownerIndexName = "\"";
        if(StringUtils.trimToNull(String.valueOf(indexInfo.get("OWNER")))!=null){
            ownerIndexName+= indexInfo.get("OWNER") + "\".\"";
        }
        ownerIndexName+=indexInfo.get("INDEX_NAME") + "\"";
        return ownerIndexName;
    }

    private Sql getDropIndexSql(String ownerIndexName) {
        String sql = "DROP INDEX " + ownerIndexName;
        return new UnparsedSql(sql);
    }

    private Sql getCreateIndexSql(Sql statement) {
        return new UnparsedSql(statement.toSql() + " DEFER YES", (DatabaseObject[]) statement.getAffectedDatabaseObjects().toArray(new DatabaseObject[statement.getAffectedDatabaseObjects().size()]));
    }

    private Sql getRebuildIndex(String ownerIndexName) {
        String sql = "call DSNUTILU('REBUILD', 'NO', 'REBUILD INDEX (" + ownerIndexName + ")', 1)";
        return new CallableSql(sql, DSNUTIL_SUCCESS_STATUS);
    }

    private Sql getRunStatsSql(String ownerIndexName) {
        String sql = "call DSNUTILU('STATS', 'NO', 'RUNSTATS INDEX(" + ownerIndexName + ")', 1)";
        return new CallableSql(sql, DSNUTIL_SUCCESS_STATUS);
    }

    @Override
    protected String getObjectQuery() {
        String sql = "SELECT indx.NAME AS INDEX_NAME, " +
                "indx.OWNER, " +
                "indx.TBCREATOR AS TABLE_SCHEMA " +
                "FROM SYSIBM.SYSINDEXES indx " +
                "INNER JOIN SYSIBM.SYSTABLES tb " +
                "ON indx.TBNAME = tb.NAME " +
                "WHERE tb.NAME = '" + statement.getTableName() + "' " +
                "AND indx.NAME ='" + statement.getIndexName() + "' ";
        if (statement.getTableSchemaName() != null) {
            sql += "AND indx.TBCREATOR= '" + statement.getTableSchemaName() + "'";
        }
        return sql;
    }
}
