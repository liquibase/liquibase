package liquibase.sqlgenerator.core.util;

import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.structure.core.Relation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MssqlUtil for generator to handle SET methods for now.
 * Additional utility methods can be added here
 */
public class SqlGeneratorMssqlUtil {
    private static final String IS_SET_REGEX = "(?i)SET\\s+(?i)(ANSI_NULLS|QUOTED_IDENTIFIER)\\s+(?i)(ON|OFF)(\\s*\\t*\\n*);?";

    /**
     * Add sql SET QUOTED_IDENTIFIER and SET ANSI_NULLS to separate index in sql list
     * so that the code will individually run these sql
     */
    public static void addSqlStatementsToList(List<Sql> sql, String sqlText, String delimiter) {
        Matcher hasSetMatcher =  getSetRegexMatcher(sqlText);
        String cleanSqlText = sqlText;
        hasSetMatcher.reset(); // reset to start to make the find and replace fit in a single loop
        while (hasSetMatcher.find()) {
            String curSetSql = hasSetMatcher.group();
            sql.add(new UnparsedSql(curSetSql, delimiter));
            cleanSqlText = cleanSqlText.replace(curSetSql, "");
        }
        sql.add(new UnparsedSql(cleanSqlText, delimiter));
    }

    /**
     * Variation of the addSqlStatementsToList() function in the class, used for
     * a different use case
     */
    public static void addSqlStatementsToList(List<Sql> sql, String sqlText, Relation relation) {
        Matcher hasSetMatcher = getSetRegexMatcher(sqlText);
        String cleanSqlTest = sqlText;
        hasSetMatcher.reset(); // reset to start to make the find and replace fit in a single loop
        while (hasSetMatcher.find()) {
            String curSetSql = hasSetMatcher.group();
            sql.add(new UnparsedSql(curSetSql, ";")); // adding a default end delimiter of ;
            cleanSqlTest = cleanSqlTest.replace(curSetSql, "");
        }
        sql.add(new UnparsedSql(cleanSqlTest, relation));
    }

    /**
     * Prepares a regex matcher for SET QUOTED_IDENTIFIER and SET ANSI_NULLS
     */
    public static Matcher getSetRegexMatcher(String sqlText) {
        Pattern hasSetPattern = Pattern.compile(IS_SET_REGEX);
        return  hasSetPattern.matcher(sqlText);
    }
}
