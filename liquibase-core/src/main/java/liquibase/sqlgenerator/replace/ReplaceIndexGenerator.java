package liquibase.sqlgenerator.replace;

import liquibase.database.Database;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateIndexStatement;

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
            String ownerIndexName = "\"" + indexInfo.get("OWNER") + "\".\"" + indexInfo.get("INDEX_NAME") + "\"";
            String databaseTablespaceName = "\"" + indexInfo.get("DBNAME") + "\".\"" + indexInfo.get("TABLESPACE_NAME") + "\"";

            StringBuilder sql = new StringBuilder("call DSNUTILU('REPLACE', 'NO', '");
            appendDropIndexSql(sql, ownerIndexName);
            appendCreateIndexSql(sql, statementSql);
            appendRebuildIndex(sql, ownerIndexName);
            appendCheckSql(sql, databaseTablespaceName);
            appendRunStatsSql(sql, ownerIndexName);
            sql.append("', 1)");

            List<Sql> sqlList = new ArrayList<>();
            sqlList.add(new CallableSql(sql.toString(), "DSNU010I"));
            sqlList.addAll(getRebindPackagesSql(database, indexInfo.get("TABLE_SCHEMA"), indexInfo.get("TBNAME"), ObjectType.INDEX));
            return sqlList;
        }
        return statementSql;
    }

    private void appendDropIndexSql(StringBuilder sql, String ownerIndexName) {
        sql.append("EXEC SQL DROP INDEX ").append(ownerIndexName).append(" ENDEXEC;");
    }

    private void appendCreateIndexSql(StringBuilder sql, List<Sql> statementSql) {
        sql.append("EXEC SQL ");
        for (Sql statement : statementSql) {
            sql.append(statement.toSql()).append(" DEFER YES").append("; ");
        }
        sql.append("ENDEXEC; ");
    }

    private void appendRebuildIndex(StringBuilder sql, String ownerIndexName) {
        sql.append("REBUILD INDEX (").append(ownerIndexName).append(");");
    }

    private void appendCheckSql(StringBuilder sql, String databaseTablespaceName) {
        sql.append(" TEMPLATE PU UNIT SYSDA DSN (CHECK.SYSUT1.TEMP) DISP (NEW,DELETE,DELETE) ")
                .append("TEMPLATE SO UNIT SYSDA DSN (CHECK.SORTOUT.TEMP) DISP (NEW,DELETE,DELETE) ")
                .append("TEMPLATE ER UNIT SYSDA DSN (CHECK.SYSERR.ERRORS) DISP (NEW,CATLG,CATLG) ")
                .append("CHECK DATA TABLESPACE ").append(databaseTablespaceName).append(" INCLUDE XML TABLESPACES SCOPE ALL ERRDDN ER WORKDDN PU, SO;");
    }

    private void appendRunStatsSql(StringBuilder sql, String ownerIndexName) {
        sql.append(" RUNSTATS INDEX(").append(ownerIndexName).append(")");
    }

    @Override
    protected String getObjectQuery() {
        String sql = "SELECT indx.DBNAME, " +
                "indx.NAME AS INDEX_NAME," +
                "indx.OWNER, " +
                "indx.TBCREATOR AS TABLE_SCHEMA, " +
                "sp.NAME AS TABLESPACE_NAME " +
                "FROM SYSIBM.SYSTABLESPACE sp " +
                "INNER JOIN SYSIBM.SYSTABLES tb " +
                "ON sp.DBNAME = tb.DBNAME " +
                "AND tb.TSNAME = sp.NAME " +
                "INNER JOIN SYSIBM.SYSINDEXES indx " +
                "ON tb.NAME = indx.TBNAME " +
                "WHERE tb.NAME = '" + statement.getTableName() + "'" +
                "AND indx.NAME ='" + statement.getIndexName() + "'";
        if (statement.getTableSchemaName() != null) {
            sql += "AND indx.TBCREATOR= '" + statement.getTableSchemaName() + "'";
        }
        return sql;
    }
}
