package liquibase.database.jvm;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A base class that holds connection string patterns with connection credentials that should be hidden in logs.
 * Derived classes add their own connection string patterns.
 */
public abstract class ConnectionPatterns {

    public static class PatternPair {
        // Return a map entry (key-value pair) from the specified values
        public static <T, U> Map.Entry<T, U> of(T first, U second) {
            return new AbstractMap.SimpleEntry<>(first, second);
        }
    }
    private final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK = new HashSet<>();
    private final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK_TO_OBFUSCATE = new HashSet<>();
    private final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_OBFUSCATE = new HashSet<>();

    public Set<Map.Entry<Pattern, Pattern>> getJdbcBlankPatterns() {
        return PATTERN_JDBC_BLANK;
    }

    public Set<Map.Entry<Pattern, Pattern>> getJdbcBlankToObfuscatePatterns() {
        return PATTERN_JDBC_BLANK_TO_OBFUSCATE;
    }

    public Set<Map.Entry<Pattern, Pattern>> getJdbcObfuscatePatterns() {
        return PATTERN_JDBC_OBFUSCATE;
    }

    /**
     * Holds credential patterns that should be replaced by empty string.
     * <p>
     * jdbc:mysql://localhost:3306/lbcat?<b>user=username&password=password</b>
     * </p>
     * <p>
     * jdbc:mariadb://<b>username:password</b>@localhost:3306/lbcat
     * </p>
     */
    public void addJdbcBlankPatterns(Map.Entry<Pattern, Pattern> jdbcBlankPatterns) {
        PATTERN_JDBC_BLANK.add(jdbcBlankPatterns);
    }

    /**
     * Holds credential patterns that are located before the domain or subdomain part.
     * They should be replaced by asterisks.
     * <p>
     * jdbc:mariadb://<b>username:password</b>@localhost:3306/lbcat
     * </p>
     */
    public void addJdbcBlankToObfuscatePatterns(Map.Entry<Pattern, Pattern> patternJdbcBlankToObfuscate) {
        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(patternJdbcBlankToObfuscate);
    }

    /**
     * Holds credential patterns that are located in the URL parameters.
     * They should be replaced by asterisks.
     * <p>
     * jdbc:postgresql://localhost:6432/lbcat?<b>user=username&password=password</b>
     * </p>
     */
    public void addJdbcObfuscatePatterns(Map.Entry<Pattern, Pattern> jdbcObfuscatePatterns) {
        PATTERN_JDBC_OBFUSCATE.add(jdbcObfuscatePatterns);
    }
}
