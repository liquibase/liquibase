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
    private static final String SET_REGEX = "SET\\s+(ANSI_NULLS|QUOTED_IDENTIFIER)\\s+(ON|OFF)(\\s*\\t*\\n*);?";
    private static final String DEFAULT_END_DELIMITER = ";";

    /**
     * Split out (SET QUOTED_IDENTIFIER and SET ANSI_NULLS) into list as separate sql statements
     * so that the code will individually run these sql
     */
    public static void addSqlStatementsToList(List<Sql> sql, String sqlText, String delimiter) {
        Matcher hasSetMatcher =  getSetRegexMatcher(sqlText);
        Pattern patternToCheckIfContainsAnySql = Pattern.compile("[^\\s" + delimiter + "]");

        String cleanSqlText = sqlText;
        int removedFromSqlTextCount = 0;
        while (hasSetMatcher.find()) {
            String setStatementSql = hasSetMatcher.group();
            String beforeSetSql = cleanSqlText.substring(0, hasSetMatcher.start() - removedFromSqlTextCount);
            if (patternToCheckIfContainsAnySql.matcher(beforeSetSql).find()) {
                sql.add(new UnparsedSql(beforeSetSql, delimiter));
            }
            sql.add(new UnparsedSql(setStatementSql, delimiter));
            cleanSqlText = cleanSqlText.substring(hasSetMatcher.end() - removedFromSqlTextCount);
            removedFromSqlTextCount = hasSetMatcher.end();
        }

        if (patternToCheckIfContainsAnySql.matcher(cleanSqlText).find()) {
            sql.add(new UnparsedSql(cleanSqlText, delimiter));
        }
    }

    /**
     * Split out (SET QUOTED_IDENTIFIER and SET ANSI_NULLS) into list as separate sql statements
     * so that the code will individually run these sql
     */
    public static void addSqlStatementsToList(List<Sql> sql, String sqlText, Relation relation) {
        Matcher hasSetMatcher = getSetRegexMatcher(sqlText);
        Pattern patternToCheckIfContainsAnySql = Pattern.compile("[^\\s;]+");

        String cleanSqlText = sqlText;
        int removedFromSqlTextCount = 0;
        while (hasSetMatcher.find()) {
            String setStatementSql = hasSetMatcher.group();
            String beforeSetSql = cleanSqlText.substring(0, hasSetMatcher.start() - removedFromSqlTextCount);
            if (patternToCheckIfContainsAnySql.matcher(beforeSetSql).find()) {
                sql.add(new UnparsedSql(beforeSetSql, DEFAULT_END_DELIMITER, relation));
            }
            sql.add(new UnparsedSql(setStatementSql, DEFAULT_END_DELIMITER));
            cleanSqlText = cleanSqlText.substring(hasSetMatcher.end() - removedFromSqlTextCount);
            removedFromSqlTextCount = hasSetMatcher.end();
        }

        if (patternToCheckIfContainsAnySql.matcher(cleanSqlText).find()) {
            sql.add(new UnparsedSql(cleanSqlText, DEFAULT_END_DELIMITER, relation));
        }
    }

    /**
     * Prepares a regex matcher for SET QUOTED_IDENTIFIER and SET ANSI_NULLS
     */
    public static Matcher getSetRegexMatcher(String sqlText) {
        Pattern hasSetPattern = Pattern.compile(SET_REGEX, Pattern.CASE_INSENSITIVE);
        return  hasSetPattern.matcher(sqlText);
    }
}
