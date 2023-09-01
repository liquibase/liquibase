package liquibase.database.jvm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface JdbcConnectionPatterns {
    Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK = new HashSet<>();
    Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK_TO_OBFUSCATE = new HashSet<>();
    Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_OBFUSCATE = new HashSet<>();

    default Set<Map.Entry<Pattern, Pattern>> getJdbcBlankPatterns() {
        return PATTERN_JDBC_BLANK;
    }

    default Set<Map.Entry<Pattern, Pattern>> getJdbcBlankToObfuscatePatterns() {
        return PATTERN_JDBC_BLANK_TO_OBFUSCATE;
    }

    default Set<Map.Entry<Pattern, Pattern>> getJdbcObfuscatePatterns() {
        return PATTERN_JDBC_OBFUSCATE;
    }
}
