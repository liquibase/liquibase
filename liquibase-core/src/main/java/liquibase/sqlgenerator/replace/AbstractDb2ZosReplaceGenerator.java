package liquibase.sqlgenerator.replace;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.sql.CallableSql;
import liquibase.sql.Sql;
import liquibase.statement.core.RawSqlStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Basic class for complex DB2 on z/OS workflow which requires an object (f.e Table, Index) replacement
 */
public abstract class AbstractDb2ZosReplaceGenerator {

    /**
     * Generates sqls required for an object replacement.
     *
     * @param database     {@link Database} object
     * @param statementSql list of statement generated sql
     * @return list of required sql
     */
    public abstract List<Sql> generateReplacementSql(Database database, List<Sql> statementSql);

    /**
     * Default implementation for getting information for object
     *
     * @param database {@link Database} object
     * @return map of information columns
     */
    protected Map<String, ?> getObjectInfo(Database database) {
        Map<String, ?> result = null;
        try {
            List<Map<String, ?>> rows = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getObjectQuery()));
            if (!rows.isEmpty()) {
                result = rows.get(0);
            }
        } catch (DatabaseException e) {
            LogFactory.getInstance().getLog().warning("Failed to get object info.", e);
        }
        return result;
    }

    /**
     * Returns query for object information
     *
     * @return query string
     */
    protected abstract String getObjectQuery();

    /**
     * Returns list of sql to rebind dependent packages
     *
     * @param database   {@link Database} object
     * @param schemaName object schema
     * @param objectName object name
     * @param type       {@link ObjectType} object type
     * @return query string
     */
    protected List<Sql> getRebindPackagesSql(Database database, Object schemaName, Object objectName, ObjectType type) {
        List<Sql> rebindPackagesSql = new ArrayList<>();
        String packagesQuery = "SELECT DISTINCT " +
                "T1.DCOLLID AS COLLECTION, " +
                "T1.DNAME   AS NAME, " +
                "T2.VERSION " +
                "FROM SYSIBM.SYSPACKDEP T1 " +
                "INNER JOIN SYSIBM.SYSPACKAGE T2 ON (T1.DCOLLID = T2.COLLID AND T1.DNAME = T2.NAME AND T1.DCONTOKEN = T2.CONTOKEN) " +
                "WHERE T1.DTYPE = '' AND T1.BQUALIFIER = '" + schemaName + "' AND T1.BNAME = '" + objectName + "' AND T1.BTYPE = '" + type.getValue() + "'";
        try {
            List<Map<String, ?>> packages = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(packagesQuery));
            for (Map<String, ?> pckg : packages) {
                String sql = "call ADMIN_COMMAND_DSN('REBIND PACKAGE (" + pckg.get("COLLECTION") + "." + pckg.get("NAME") + ".(" + pckg.get("VERSION") + "))', 'Failed to execute REBIND')";
                rebindPackagesSql.add(new CallableSql(sql, "DSNT232I"));
            }
        } catch (DatabaseException e) {
            LogFactory.getInstance().getLog().warning("Failed to get dependent packages.", e);
        }
        return rebindPackagesSql;
    }

    protected enum ObjectType {
        TABLE('T'), INDEX('I');
        private char value;

        ObjectType(char value) {
            this.value = value;
        }

        public char getValue() {
            return value;
        }
    }
}
