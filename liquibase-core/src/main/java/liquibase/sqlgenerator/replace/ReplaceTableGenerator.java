package liquibase.sqlgenerator.replace;

import liquibase.database.Database;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
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
            String databaseTablespaceName = "\"" + tableInfo.get("DBNAME") + "\".\"" + tableInfo.get("TABLESPACE") + "\"";
            String templateId = getTemplateId("DATA");

            StringBuilder sql = new StringBuilder("call DSNUTILU('REPLACE', 'NO', '");
            appendCopySql(sql, schemaTableName);
            appendUnloadSql(sql, schemaTableName, templateId);
            appendDropTableSql(sql, schemaTableName);
            appendCreateSql(sql, statementSql);
            appendLoadSql(sql, schemaTableName, templateId);
//           todo: database name can change after re-creating a table
//           appendCheckSql(sql, databaseTablespaceName);
//           appendRunStatsSql(sql, databaseTablespaceName, schemaTableName);
            sql.append("', 1)");

            List<Sql> sqlList = new ArrayList<>();
            sqlList.add(new CallableSql(sql.toString(), "DSNU010I"));
            sqlList.addAll(getRebindPackagesSql(database, tableInfo.get("SCHEMA"), tableInfo.get("NAME"), ObjectType.TABLE));
            return sqlList;
        }
        return statementSql;
    }

    @Override
    protected String getObjectQuery() {
        String sql = "SELECT CREATOR AS SCHEMA, " +
                "NAME, " +
                "TSNAME AS TABLESPACE, " +
                "DBNAME " +
                "FROM  SYSIBM.SYSTABLES " +
                "WHERE TYPE = 'T' AND NAME='" + statement.getTableName() + "'";
        if (statement.getSchemaName() != null) {
            sql += "AND CREATOR='" + statement.getSchemaName() + "'";
        }
        return sql;
    }

    private static String getTemplateId(String prefix) {
        String format = new SimpleDateFormat(":HH:mm:ss").format(new Date());
        return prefix + format.replaceAll(":", ".T");
    }

    private void appendCopySql(StringBuilder sql, String schemaTableName) {
        sql.append("LISTDEF LST INCLUDE TABLESPACES TABLE ").append(schemaTableName)
                .append(" TEMPLATE COPYT RETPD 1 DSN (&DB..&TS..M&MO..D&DA..Y&YE..T&TI.) DISP (NEW,CATLG,CATLG) COPY LIST LST COPYDDN (COPYT) PARALLEL (2)");
    }

    private void appendUnloadSql(StringBuilder sql, String schemaTableName, String templateId) {
        sql.append(" TEMPLATE UNLOADT RETPD 1 DSN (").append(templateId).append(") DISP (NEW,CATLG,DELETE) ")
                .append("TEMPLATE PUNCHT DSN (UNLAOD.USER) DISP (NEW,DELETE,DELETE) ")
                .append("UNLOAD FROM TABLE ").append(schemaTableName).append(" PUNCHDDN PUNCHT UNLDDN UNLOADT");
    }

    private void appendDropTableSql(StringBuilder sql, String schemaTableName) {
        sql.append(" EXEC SQL DROP TABLE ").append(schemaTableName).append(" ENDEXEC");
    }

    private void appendCreateSql(StringBuilder sql, List<Sql> statementSql) {
        sql.append(" EXEC SQL ");
        for (Sql statement : statementSql) {
            sql.append(statement.toSql()).append(";");
        }
        sql.append("ENDEXEC");
    }

    private void appendLoadSql(StringBuilder sql, String schemaTableName, String templateId) {
        sql.append(" TEMPLATE LOADT DSN (").append(templateId).append(") DISP (SHR,DELETE,DELETE) ")
                .append("TEMPLATE IS DSN (LOAD.IS) DISP (NEW,DELETE,DELETE) ")
                .append("TEMPLATE OS DSN (LOAD.OS) DISP (NEW,DELETE,DELETE)")
                .append("LOAD DATA INDDN(LOADT) WORKDDN(IS, OS) INTO TABLE ").append(schemaTableName).append(" IGNOREFIELDS YES FORMAT UNLOAD");
    }

    private void appendCheckSql(StringBuilder sql, String databaseTablespaceName) {
        sql.append(" TEMPLATE PU UNIT SYSDA DSN (CHECK.SYSUT1.TEMP) DISP (NEW,DELETE,DELETE) ")
                .append("TEMPLATE SO UNIT SYSDA DSN (CHECK.SORTOUT.TEMP) DISP (NEW,DELETE,DELETE) ")
                .append("TEMPLATE ER UNIT SYSDA DSN (CHECK.SYSERR.ERRORS) DISP (NEW,CATLG,CATLG) ")
                .append("CHECK DATA TABLESPACE ").append(databaseTablespaceName).append(" INCLUDE XML TABLESPACES SCOPE ALL ERRDDN ER WORKDDN PU, SO");
    }

    private void appendRunStatsSql(StringBuilder sql, String databaseTablespaceName, String schemaTableName) {
        sql.append(" RUNSTATS TABLESPACE(").append(databaseTablespaceName).append(") TABLE (").append(schemaTableName).append(")");
    }
}
