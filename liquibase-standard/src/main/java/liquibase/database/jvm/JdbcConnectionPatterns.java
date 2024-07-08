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
    private static final String FILTER_ACCOUNT_KEY = "(?i)(^cosmosdb:\\/\\/.+?):(.*?)@.*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PRIVATE_KEY_FILE_PWD = "(?i)(.+?)private_key_file_pwd=([^;&?]+)[;&]*?(.*?)$";

    private static final String FILTER_CREDS = "(?i)/(.*)((?=@))";

    private static final String FILTER_CREDS_MYSQL_TO_OBFUSCATE = "(?i).+://(.*?)([:])(.*?)((?=@))";
    private static final String FILTER_CREDS_ORACLE_TO_OBFUSCATE = "(?i)jdbc:oracle:thin:(.*?)([/])(.*?)((?=@))";

    public JdbcConnectionPatterns() {
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PW_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_TO_BLANK)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)jdbc:oracle:thin(.*)"), Pattern.compile(FILTER_CREDS)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)jdbc:mysql(.*)"), Pattern.compile(FILTER_CREDS)));
        addJdbcBlankPatterns(PatternPair.of(Pattern.compile("(?i)jdbc:mariadb(.*)"), Pattern.compile(FILTER_CREDS)));

        addJdbcBlankToObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)jdbc:oracle:thin(.*)"), Pattern.compile(FILTER_CREDS_ORACLE_TO_OBFUSCATE)));
        addJdbcBlankToObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)jdbc:mysql(.*)"), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));
        addJdbcBlankToObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)jdbc:mariadb(.*)"), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));

        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PASSWORD)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE_PWD)));
        addJdbcObfuscatePatterns(PatternPair.of(Pattern.compile("(?i)^cosmosdb:\\/\\/.*"), Pattern.compile(FILTER_ACCOUNT_KEY)));
    }
}
