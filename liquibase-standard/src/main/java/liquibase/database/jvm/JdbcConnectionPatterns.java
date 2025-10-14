package liquibase.database.jvm;

import java.util.regex.Pattern;

public class JdbcConnectionPatterns extends ConnectionPatterns {

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PW_TO_BLANK = "(?i)[?&:;]password=[^;&]*";
    private static final String FILTER_CREDS_USER_TO_BLANK = "(?i)[?&:;]user(.*?)=(.+)[^;&]";
    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_TO_BLANK = "(?i)[?&:;]private_key_file(.*?)=[^;&]*";
    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_OAUTH2_SECRET_TO_BLANK = "(?i)[?&:;]OAuth2Secret=[^;&]*";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PASSWORD = "(?i)(.+?)password=([^;&?]+)[;&]*?(.*?)$";

    private static final String FILTER_CREDS_USER = "(?i)(.+?)user[name]*?=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_FILE = "(?i)(.+?)private_key_file=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_ACCOUNT_KEY = "(?i)(^cosmosdb:\\/\\/.+?):(.*?)@.*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_FILE_PWD = "(?i)(.+?)private_key_file_pwd=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_OAUTH2_SECRET = "(?i)(.+?)OAuth2Secret=([^;&?]+)[;&]*?(.*?)$";

    private static final String FILTER_CREDS = "(?i)/(.*)((?=@))";

    private static final String FILTER_CREDS_MYSQL_TO_OBFUSCATE = "(?i).+://(.*?)([:])(.*?)((?=@))";
    private static final String FILTER_CREDS_ORACLE_TO_OBFUSCATE = "(?i)jdbc:oracle:thin:(.*?)([/])(.*?)((?=@))";

    private static final String FILTER_CREDS_ORACLE_TO_OBFUSCATE_EMPTY = "(?i)jdbc:oracle:thin:(.*?)([/])(.*?)((?=@))";
    private static final String FILTER_CREDS_MYSQL_TO_OBFUSCATE_EMPTY = "(?i).+://(.*?)([:])(.*?@)";
    private static final String FILTER_CREDS_MARIADB_TO_OBFUSCATE_EMPTY = "(?i).+://(.*?)([:])(.*?@)";

    public JdbcConnectionPatterns() {
        final String oracleThinMatcherRegex = "(?i)jdbc:oracle:thin(.*)";
        String mysqlMatcherRegex = "(?i)jdbc:mysql(.*)";
        String mariadbMatcherRegex = "(?i)jdbc:mariadb(.*)";

        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PW_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_OAUTH2_SECRET_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile(oracleThinMatcherRegex), Pattern.compile(FILTER_CREDS)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile(mysqlMatcherRegex), Pattern.compile(FILTER_CREDS)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile(mariadbMatcherRegex), Pattern.compile(FILTER_CREDS)));

        addJdbcBlankToObfuscatePatterns(PatternPair.of(Pattern.compile(oracleThinMatcherRegex), Pattern.compile(FILTER_CREDS_ORACLE_TO_OBFUSCATE)));
        addJdbcBlankToObfuscatePatterns(PatternPair.of(Pattern.compile(mysqlMatcherRegex), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));
        addJdbcBlankToObfuscatePatterns(PatternPair.of(Pattern.compile(mariadbMatcherRegex), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));

        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PASSWORD)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE_PWD)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_OAUTH2_SECRET)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)^cosmosdb:\\/\\/.*"), Pattern.compile(FILTER_ACCOUNT_KEY)));

        addJdbcBlankToObfuscatePatternsReplaceWithEmpty(PatternPair.of(Pattern.compile(oracleThinMatcherRegex), Pattern.compile(FILTER_CREDS_ORACLE_TO_OBFUSCATE_EMPTY)));
        addJdbcBlankToObfuscatePatternsReplaceWithEmpty(PatternPair.of(Pattern.compile(mysqlMatcherRegex), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE_EMPTY)));
        addJdbcBlankToObfuscatePatternsReplaceWithEmpty(PatternPair.of(Pattern.compile(mariadbMatcherRegex), Pattern.compile(FILTER_CREDS_MARIADB_TO_OBFUSCATE_EMPTY)));
    }
}
