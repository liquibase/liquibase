package liquibase.database.jvm;

import java.util.regex.Pattern;

public class JdbcConnectionPatterns extends ConnectionPatterns {

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PW_TO_BLANK = "(?i)[?&:;]password=[^;&]*";
    private static final String FILTER_CREDS_USER_TO_BLANK = "(?i)[?&:;]user(.*?)=(.+)[^;&]";
    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_TO_BLANK = "(?i)[?&:;]private_key_file(.*?)=[^;&]*";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PASSWORD = "(?i)(.+?)password=([^;&?]+)[;&]*?(.*?)$";

    private static final String FILTER_CREDS_USER = "(?i)(.+?)user[name]*?=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_FILE = "(?i)(.+?)private_key_file=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_FILE_PWD = "(?i)(.+?)private_key_file_pwd=([^;&?]+)[;&]*?(.*?)$";

    private static final String FILTER_CREDS = "(?i)/(.*)((?=@))";

    private static final String FILTER_CREDS_MYSQL_TO_OBFUSCATE = "(?i).+://(.*?)([:])(.*?)((?=@))";
    private static final String FILTER_CREDS_ORACLE_TO_OBFUSCATE = "(?i)jdbc:oracle:thin:(.*?)([/])(.*?)((?=@))";

    public JdbcConnectionPatterns() {
        PATTERN_JDBC_BLANK.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PW_TO_BLANK)));
        PATTERN_JDBC_BLANK.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER_TO_BLANK)));
        PATTERN_JDBC_BLANK.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_TO_BLANK)));
        PATTERN_JDBC_BLANK.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)jdbc:oracle:thin(.*)"), Pattern.compile(FILTER_CREDS)));
        PATTERN_JDBC_BLANK.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)jdbc:mysql(.*)"), Pattern.compile(FILTER_CREDS)));
        PATTERN_JDBC_BLANK.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)jdbc:mariadb(.*)"), Pattern.compile(FILTER_CREDS)));

        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)jdbc:oracle:thin(.*)"), Pattern.compile(FILTER_CREDS_ORACLE_TO_OBFUSCATE)));
        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)jdbc:mysql(.*)"), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));
        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)jdbc:mariadb(.*)"), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));

        PATTERN_JDBC_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PASSWORD)));
        PATTERN_JDBC_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER)));
        PATTERN_JDBC_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE)));
        PATTERN_JDBC_OBFUSCATE.add(JdbcConnection.PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE_PWD)));
    }
}
