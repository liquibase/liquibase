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
        private PatternPair() { }

        // Return a map entry (key-value pair) from the specified values
        public static <T, U> Map.Entry<T, U> of(T first, U second) {
            return new AbstractMap.SimpleEntry<>(first, second);
        }
    }
    private final Set<Map.Entry<Pattern, Pattern>> patternJdbcBlank = new HashSet<>();
    private final Set<Map.Entry<Pattern, Pattern>> patternJdbcBlankToObfuscate = new HashSet<>();
    private final Set<Map.Entry<Pattern, Pattern>> patternJdbcObfuscate = new HashSet<>();

    public Set<Map.Entry<Pattern, Pattern>> getJdbcBlankPatterns() {
        return patternJdbcBlank;
    }

    public Set<Map.Entry<Pattern, Pattern>> getJdbcBlankToObfuscatePatterns() {
        return patternJdbcBlankToObfuscate;
    }

    public Set<Map.Entry<Pattern, Pattern>> getJdbcObfuscatePatterns() {
        return patternJdbcObfuscate;
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
    @SuppressWarnings("secrets:S6703")
    public void addJdbcBlankPatterns(Map.Entry<Pattern, Pattern> jdbcBlankPatterns) {
        patternJdbcBlank.add(jdbcBlankPatterns);
    }

    /**
     * Holds credential patterns that are located before the domain or subdomain part.
     * They should be replaced by asterisks.
     * <p>
     * jdbc:mariadb://<b>username:password</b>@localhost:3306/lbcat
     * </p>
     */
    public void addJdbcBlankToObfuscatePatterns(Map.Entry<Pattern, Pattern> patternJdbcBlankToObfuscate) {
        this.patternJdbcBlankToObfuscate.add(patternJdbcBlankToObfuscate);
    }

    /**
     * Holds credential patterns that are located in the URL parameters.
     * They should be replaced by asterisks.
     * <p>
     * jdbc:postgresql://localhost:6432/lbcat?<b>user=username&password=password</b>
     * </p>
     */
    @SuppressWarnings("secrets:S6703")
    public void addJdbcObfuscatePatterns(Map.Entry<Pattern, Pattern> jdbcObfuscatePatterns) {
        patternJdbcObfuscate.add(jdbcObfuscatePatterns);
    }
}
