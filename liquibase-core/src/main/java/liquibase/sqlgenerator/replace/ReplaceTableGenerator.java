package liquibase.sqlgenerator.replace;

import liquibase.database.Database;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.CreateTableStatement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReplaceTableGenerator extends AbstractDb2ZosReplaceGenerator {
    private final CreateTableStatement statement;

    public ReplaceTableGenerator(CreateTableStatement statement) {
        this.statement = statement;
    }

    @Override
    public List<Sql> generateReplacementSql(Database database, List<Sql> statementSql) {
        Map<String, ?> tableInfo = getObjectInfo(database);
        if (tableInfo != null) {
            String schemaTableName = "\"" + tableInfo.get("SCHEMA") + "\".\"" + tableInfo.get("NAME") + "\"";

            List<Sql> sqlList = new ArrayList<>();
            sqlList.add(getCopySql(schemaTableName));
            sqlList.add(getUnloadSql(schemaTableName, tableInfo.get("ENCODING")));
            sqlList.add(getDropTableSql(schemaTableName));
            sqlList.addAll(statementSql);
            sqlList.add(getLoadSql(schemaTableName, tableInfo.get("ENCODING")));
            if (tableInfo.get("DBNAME") != null) {
                sqlList.add(getCheckSql(tableInfo.get("DBNAME"), tableInfo.get("TABLESPACE")));
            }
            sqlList.add(getRunStatsSql(schemaTableName));
            sqlList.addAll(getRebindPackagesSql(database, tableInfo.get("SCHEMA"), tableInfo.get("NAME"), ObjectType.TABLE));
            return sqlList;
        }
        return statementSql;
    }

    @Override
    protected String getObjectQuery() {
        String sql = "SELECT tb.CREATOR AS SCHEMA, " +
                "tb.NAME, " +
                "tb.TSNAME AS TABLESPACE, " +
                "db.NAME AS DBNAME, " +
                "CASE tb.ENCODING_SCHEME " +
                "WHEN 'A' THEN 'ASCII' " +
                "WHEN 'U' THEN 'UNICODE' " +
                "ELSE null END AS ENCODING " +
                "FROM SYSIBM.SYSTABLES tb " +
                "LEFT JOIN SYSIBM.SYSDATABASE db ON tb.DBNAME = db.NAME AND db.IMPLICIT = 'N' " +
                "WHERE tb.TYPE = 'T' AND tb.NAME='" + statement.getTableName() + "'";
        if (statement.getSchemaName() != null) {
            sql += "AND tb.CREATOR='" + statement.getSchemaName() + "'";
        }
        return sql;
    }

    private Sql getCopySql(String schemaTableName) {
        String sql = "call DSNUTILU('COPY', 'NO', 'LISTDEF LST INCLUDE TABLESPACES TABLE " + schemaTableName + " TEMPLATE COPYT RETPD 1 DSN (&DB..&TS..M&MO..D&DA..Y&YE..T&TI.) DISP (NEW,CATLG,CATLG) COPY LIST LST COPYDDN (COPYT) PARALLEL (2)', 1)";
        return new CallableSql(sql, DSNUTIL_SUCCESS_STATUS);
    }

    private Sql getUnloadSql(String schemaTableName, Object encoding) {
        String sql = "call DSNUTILU('UNLOAD', 'NO', 'TEMPLATE UNLOADT RETPD 1 DSN (UNLOAD.PUNCH) DISP (NEW,CATLG,DELETE) TEMPLATE PUNCHT DSN (UNLAOD.USER) DISP (NEW,DELETE,DELETE) UNLOAD FROM TABLE " + schemaTableName + " PUNCHDDN PUNCHT UNLDDN UNLOADT DELIMITED ";
        if (encoding != null) {
            sql += encoding + " ";
        }
        sql += "', 1)";
        return new CallableSql(sql, DSNUTIL_SUCCESS_STATUS);
    }

    private Sql getDropTableSql(String schemaTableName) {
        String sql = "DROP TABLE " + schemaTableName;
        return new UnparsedSql(sql);
    }

    private Sql getLoadSql(String schemaTableName, Object encoding) {
        String sql = "call DSNUTILU('LOAD', 'NO', 'TEMPLATE LOADT DSN (UNLOAD.PUNCH) DISP (SHR,DELETE,DELETE) LOAD DATA FORMAT DELIMITED ";
        if (encoding != null) {
            sql += encoding + " ";
        }
        sql += "INDDN(LOADT) INTO TABLE " + schemaTableName + " IGNOREFIELDS YES', 1)";
        return new CallableSql(sql, DSNUTIL_SUCCESS_STATUS);
    }

    private Sql getCheckSql(Object dbName, Object tablespace) {
        String sql = "call DSNUTILU('CHECK', 'NO', 'TEMPLATE PU UNIT SYSDA DSN (CHECK.SYSUT1.TEMP) DISP (NEW,DELETE,DELETE) TEMPLATE SO UNIT SYSDA DSN (CHECK.SORTOUT.TEMP) DISP (NEW,DELETE,DELETE) TEMPLATE ER UNIT SYSDA DSN (CHECK.SYSERR.ERRORS) DISP (NEW,CATLG,CATLG) CHECK DATA TABLESPACE \"" + dbName + "\".\"" + tablespace + "\" INCLUDE XML TABLESPACES SCOPE ALL ERRDDN ER WORKDDN PU, SO', 1)";
        return new CallableSql(sql, null);
    }

    private Sql getRunStatsSql(String schemaTableName) {
        String sql = "call DSNUTILU('STATS', 'NO', 'LISTDEF LST INCLUDE TABLESPACES TABLE " + schemaTableName + " RUNSTATS TABLESPACE LIST(LST)', 1)";
        return new CallableSql(sql, DSNUTIL_SUCCESS_STATUS);
    }
}
