package liquibase.integration;

import liquibase.configuration.ConfigurationValueObfuscator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Stores information about the integration running Liquibase.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationDetails {
    /**
     * A name which defines what integration is executing Liquibase. An example might be "cli" or "maven". This is not
     * representative of the environnment that Liquibase is executing inside of, so "docker" would not be a valid name.
     */
    private String name;

    private final Map<String, String> parameters = new HashMap<>();

    /**
     * Producer-side prefixes that the CLI / Maven plugin / defaults-file loader attach to argument names
     * before passing them here. Stripped before matching against {@link #CREDENTIAL_KEY_TOKENS}.
     */
    private static final String[] KEY_PREFIXES = {"argument__", "maven__", "defaultsFile__"};

    /**
     * Lowercase substrings that mark a parameter key as carrying a credential value. Conservative
     * list matched against the lowercased key (with producer prefixes stripped).
     */
    private static final Set<String> CREDENTIAL_KEY_TOKENS = Set.of(
            "password", "passwd", "secret", "token", "apikey", "accesskey"
    );

    public void setParameter(String key, String value) {
        this.parameters.put(key, sanitize(key, value));
    }

    /**
     * Defense-in-depth redaction at the boundary into the {@link #parameters} map. Producers (the CLI
     * argument parser, defaults-file loader, Maven plugin Mojos) call {@link #setParameter} for every
     * parameter they observe — including raw passwords and JDBC URLs containing credentials. The
     * {@code IntegrationDetails} object is then attached to {@code Scope.Attr.integrationDetails} and is
     * reachable across the JVM for the run; any current or future telemetry / report / MDC sink that
     * serializes the parameters map would otherwise leak those credentials.
     *
     * <p>Redaction rules, applied in order:</p>
     * <ul>
     *   <li>Keys whose lowercase form (with any known producer prefix stripped) contains one of
     *       {@link #CREDENTIAL_KEY_TOKENS} → value replaced with {@code "*****"} via
     *       {@link ConfigurationValueObfuscator#STANDARD}.</li>
     *   <li>Otherwise, values starting with {@code "jdbc:"} → passed through
     *       {@link ConfigurationValueObfuscator#URL_OBFUSCATOR} (delegating to
     *       {@link liquibase.database.jvm.JdbcConnection#sanitizeUrl}). Catches producers that put a
     *       connection string under any key name.</li>
     *   <li>Otherwise the value passes through unchanged.</li>
     * </ul>
     */
    private static String sanitize(String key, String value) {
        if (value == null) {
            return null;
        }
        if (key != null) {
            String matchable = stripPrefix(key).toLowerCase(Locale.ROOT);
            for (String token : CREDENTIAL_KEY_TOKENS) {
                if (matchable.contains(token)) {
                    return ConfigurationValueObfuscator.STANDARD.obfuscate(value);
                }
            }
        }
        if (value.startsWith("jdbc:")) {
            return ConfigurationValueObfuscator.URL_OBFUSCATOR.obfuscate(value);
        }
        return value;
    }

    private static String stripPrefix(String key) {
        for (String prefix : KEY_PREFIXES) {
            if (key.startsWith(prefix)) {
                return key.substring(prefix.length());
            }
        }
        return key;
    }
}
