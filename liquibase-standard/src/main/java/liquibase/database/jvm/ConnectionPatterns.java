package liquibase.database.jvm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class ConnectionPatterns {
    protected final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK = new HashSet<>();
    protected final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK_TO_OBFUSCATE = new HashSet<>();
    protected final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_OBFUSCATE = new HashSet<>();

    public Set<Map.Entry<Pattern, Pattern>> getJdbcBlankPatterns() {
        return PATTERN_JDBC_BLANK;
    }

    public Set<Map.Entry<Pattern, Pattern>> getJdbcBlankToObfuscatePatterns() {
        return PATTERN_JDBC_BLANK_TO_OBFUSCATE;
    }

    public Set<Map.Entry<Pattern, Pattern>> getJdbcObfuscatePatterns() {
        return PATTERN_JDBC_OBFUSCATE;
    }
}
