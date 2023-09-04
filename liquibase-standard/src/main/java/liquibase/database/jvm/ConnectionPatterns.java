package liquibase.database.jvm;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A base class that holds connection string patterns with connection credentials that should be hidden in logs.
 * Derived classes add their own connection string patterns.
 */
public abstract class ConnectionPatterns {

    /**
     * Holds credential patterns that should be replaced by empty string.
     */
    protected final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK = new HashSet<>();

    /**
     * Holds credential patterns that are located before the domain or subdomain part.
     * They should be replaced by asterisks.
     * <p>
     * jdbc:mariadb://<b>username:password</b>@localhost:3306/lbcat
     * </p>
     */
    protected final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK_TO_OBFUSCATE = new HashSet<>();

    /**
     * Holds credential patterns that are located in the URL parameters.
     * They should be replaced by asterisks.
     * <p>
     * jdbc:postgresql://localhost:6432/lbcat?user=username&password=password
     * </p>
     */
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
