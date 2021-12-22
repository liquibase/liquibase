package liquibase.sqlgenerator.core.util;

import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.structure.core.Relation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MSSQL Util for generator to handle SET methods for now.
 * Additional utility methods can be added here
 */
public class MSSQLUtil {
    public static String IS_SET_REGEX = "(?i)SET\\s+(?i)(ANSI_NULLS|QUOTED_IDENTIFIER)\\s(?i)(ON|OFF)(\\s*\\t*\\n*);?";

    /**
     * General add Sql Statement Util
     * @param sql
     * @param sqlText
     * @param delimiter
     */
    public static void addSqlStatementsToList(List<Sql> sql, String sqlText, String delimiter) {
        Matcher hasSetMatcher =  getMatcher(sqlText);
        if (hasSetMatcher.find()) {
            String cleanSqlText = sqlText;
            hasSetMatcher.reset(); // reset to start to make the find and replace fit in a single loop
            while (hasSetMatcher.find()) {
                String curSetSql = hasSetMatcher.group();
                sql.add(new UnparsedSql(curSetSql, delimiter));
                cleanSqlText = cleanSqlText.replace(curSetSql, "");
            }
            sql.add(new UnparsedSql(cleanSqlText, delimiter));
        } else {
            sql.add(new UnparsedSql(sqlText, delimiter));
        }
    }

    /**
     * View Specific
     * @param sql
     * @param sqlText
     * @param relation
     */
    public static void addSqlStatementsToList(List<Sql> sql, String sqlText, Relation relation) {
        Matcher hasSetMatcher = getMatcher(sqlText);
        if (hasSetMatcher.find()) {
            String cleanSqlTest = sqlText;
            hasSetMatcher.reset(); // reset to start to make the find and replace fit in a single loop
            while (hasSetMatcher.find()) {
                String curSetSql = hasSetMatcher.group();
                sql.add(new UnparsedSql(curSetSql, ";")); // adding a default end delimiter of ;
                cleanSqlTest= cleanSqlTest.replace(curSetSql, "");
            }
            sql.add(new UnparsedSql(cleanSqlTest, relation));
        } else {
            sql.add(new UnparsedSql(sqlText, relation));
        }
    }

    public static Matcher getMatcher(String sqlText) {
        Pattern hasSetPattern = Pattern.compile(IS_SET_REGEX);
        Matcher hasSetMatcher = hasSetPattern.matcher(sqlText);
        return hasSetMatcher;
    }
}
