package liquibase.parser.core.formattedsql;

import liquibase.Scope;
import liquibase.license.LicenseServiceUtils;

import java.util.regex.Pattern;

/**
 * Utility class for handling invalid formatted SQL patterns for OSS distribution.
 */
public class InvalidFormattedSqlPatternsForOssUtil {

    private static String SINGLE_LINE_COMMENT_SEQUENCE = "\\-\\-";

    // FIXME those regex are duplicated in Liquibase PRO
    protected static final String ROLLBACK_SQL_FILE_REGEX = String.format("\\s*%s[\\s]*rollbackSqlFile[\\s]+(.*)", SINGLE_LINE_COMMENT_SEQUENCE);
    protected static final Pattern ROLLBACK_SQL_FILE_PATTERN = Pattern.compile(ROLLBACK_SQL_FILE_REGEX, Pattern.CASE_INSENSITIVE);
    protected static final String TAG_DATABASE_REGEX = String.format("\\s*?%s[\\s]*?tagDatabase:[\\s]+?(.*)", SINGLE_LINE_COMMENT_SEQUENCE);
    protected static final Pattern TAG_DATABASE_PATTERN = Pattern.compile(TAG_DATABASE_REGEX, Pattern.CASE_INSENSITIVE);
    protected static final String INCLUDE_REGEX = String.format("\\s*%s[\\s]*include[\\s]+file:.*", SINGLE_LINE_COMMENT_SEQUENCE);
    protected static final Pattern INCLUDE_PATTERN = Pattern.compile(INCLUDE_REGEX, Pattern.CASE_INSENSITIVE);
    protected static final String INCLUDE_ALL_REGEX = String.format("\\s*%s[\\s]*includeAll[\\s]+(.*)", SINGLE_LINE_COMMENT_SEQUENCE);
    protected static final Pattern INCLUDE_ALL_PATTERN = Pattern.compile(INCLUDE_ALL_REGEX, Pattern.CASE_INSENSITIVE);

    private InvalidFormattedSqlPatternsForOssUtil() {
    }

    /**
     * Displays a warning message if a Pro-only command is detected and no valid Liquibase Pro license is present.
     * This method checks the given line for specific patterns associated with Pro-only commands and,
     * if matched, informs the user that the command is not supported in the Liquibase Community Edition.
     *
     * @param line a string representing the command or input line to be checked for Pro-only patterns
     */
    public static void showWarnIfIsProCommandAndNoLicenseIsPresent(String line) {
        if (!LicenseServiceUtils.isProLicenseValid()) {
            if (ROLLBACK_SQL_FILE_PATTERN.matcher(line).matches()) {
                Scope.getCurrentScope().getUI().sendErrorMessage("rollbackSqlFile command is not supported in Liquibase Community Edition");
                return;
            }
            if (TAG_DATABASE_PATTERN.matcher(line).matches()) {
                Scope.getCurrentScope().getUI().sendErrorMessage("tagDatabase command is not supported in Liquibase Community Edition");
                return;
            }
            if (INCLUDE_PATTERN.matcher(line).matches()) {
                Scope.getCurrentScope().getUI().sendErrorMessage("include command is not supported in Liquibase Community Edition");
                return;
            }
            if (INCLUDE_ALL_PATTERN.matcher(line).matches()) {
                Scope.getCurrentScope().getUI().sendErrorMessage("includeAll command is not supported in Liquibase Community Edition");
            }
        }

    }
}

