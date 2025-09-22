package liquibase.parser.core.formattedsql;

import liquibase.exception.ChangeLogParseException;
import liquibase.license.LicenseServiceUtils;

import java.util.regex.Pattern;

/**
 * Utility class for handling invalid formatted SQL patterns for OSS distribution.
 */
public class InvalidFormattedSqlPatternsForOssUtil {

    private static String SINGLE_LINE_COMMENT_SEQUENCE = "\\-\\-";

    private static String ERROR_MESSAGE = "Error parsing command line: Using '%s in Formatted SQL changelog' requires a valid Liquibase license key. Get a Liquibase license key and free trial at https://liquibase.com/trial.";

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
     * Raise an exception if a Pro-only command is detected and no valid Liquibase Pro license is present.
     * This method checks the given line for specific patterns associated with Pro-only commands and,
     * if matched, informs the user that the command is not supported in the Liquibase Community Edition.
     *
     * @param line a string representing the command or input line to be checked for Pro-only patterns
     */
    public static void interruptIfIsProCommandAndNoLicenseIsPresent(String line) throws ChangeLogParseException {
        if (!LicenseServiceUtils.isProLicenseValid()) {
            if (ROLLBACK_SQL_FILE_PATTERN.matcher(line).matches()) {
                throw new ChangeLogParseException(String.format(ERROR_MESSAGE, "rollbackSqlFile"));
            }
            if (TAG_DATABASE_PATTERN.matcher(line).matches()) {
                throw new ChangeLogParseException(String.format(ERROR_MESSAGE, "tagDatabase"));
            }
            if (INCLUDE_PATTERN.matcher(line).matches()) {
                throw new ChangeLogParseException(String.format(ERROR_MESSAGE, "include"));
            }
            if (INCLUDE_ALL_PATTERN.matcher(line).matches()) {
                throw new ChangeLogParseException(String.format(ERROR_MESSAGE, "includeAll"));
            }
        }

    }
}

