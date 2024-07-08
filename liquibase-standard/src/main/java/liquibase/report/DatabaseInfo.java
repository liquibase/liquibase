package liquibase.report;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static liquibase.util.UrlUtil.handleSqlServerDbUrlParameters;

@Data
public class DatabaseInfo {
    private static final int CONNECTION_URL_MAX_LENGTH = 128;
    public static final List<String> DB_URL_VISIBLE_KEYS = Arrays.asList("servername", "database", "databaseName");
    private String databaseType;
    private String version;
    private String databaseUrl;

    /**
     * Used in mustache template.
     *
     * @return the visible url string.
     */
    public String getVisibleDatabaseUrl() {
        return getVisibleUrl(this.databaseUrl);
    }

    /**
     * Builds a simpler version of the jdbc url if the url is longer than {@link CONNECTION_URL_MAX_LENGTH}
     * If the original url was "jdbc:sqlserver://localhost:1433;someParam=nothing;databaseName=blah;someParam2=nothing;someParam3=nothing;..." (greater than CONNECTION_URL_MAX_LENGTH chars)
     * will be shuffled to read "jdbc:sqlserver://localhost:1433;databaseName=blah..." for presentation
     *
     * @param originalUrl the original jdbc url
     * @return the modified url if longer than {@link CONNECTION_URL_MAX_LENGTH}
     */
    protected String getVisibleUrl(String originalUrl) {
        if (originalUrl == null) {
            return "";
        }
        final String modifiedUrl = originalUrl.length() >= CONNECTION_URL_MAX_LENGTH ? handleSqlServerDbUrlParameters(DB_URL_VISIBLE_KEYS, originalUrl) : originalUrl;
        return appendEllipsisIfDifferent(originalUrl, hideConnectionUrlLength(modifiedUrl));
    }

    /**
     * If the url is longer than 128 characters, remove the remaining characters
     *
     * @param url the url to modify
     * @return the modified url if over 128 characters
     */
    private String hideConnectionUrlLength(String url) {
        if (url.length() >= CONNECTION_URL_MAX_LENGTH) {
            return url.substring(0, 127);
        }
        return url;
    }

    private String appendEllipsisIfDifferent(String originalUrl, String maybeModifiedUrl) {
        if (!originalUrl.equalsIgnoreCase(maybeModifiedUrl)) {
            return String.format("%s...", maybeModifiedUrl);
        }
        return originalUrl;
    }
}
