package liquibase.analytics;

import liquibase.Scope;
import liquibase.integration.IntegrationDetails;
import liquibase.util.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static liquibase.util.VersionUtils.getLibraryInfoMap;

@FieldNameConstants(asEnum = true)
@Data
public class Event {

    private final String command;
    private boolean reportsEnabled;
    private boolean dbclhEnabled;
    private boolean structuredLogsEnabled;
    private String operationOutcome;
    private String exceptionClass;
    /**
     * SQL changelogs are different from formatted SQL. SQL changelogs are essentially raw SQL files, with no Liquibase headers or comments.
     */
    private int sqlChangelogCount;
    private int formattedSqlChangelogCount;
    private int xmlChangelogCount;
    private int jsonChangelogCount;
    private int yamlChangelogCount;
    private String databasePlatform;
    private String databaseVersion;
    private String liquibaseVersion = ExceptionUtil.doSilently(() -> {
        return LiquibaseUtil.getBuildVersionInfo();
    });
    // liquibase-mongodb-4.29.1
    private String mongoDbVersion;
    private String dynamoDbVersion = getExtensionVersion("Liquibase DynamoDB Commercial Extension");
    private String checksVersion = getExtensionVersion("Checks Extension");
    private String awsSecretsVersion = getExtensionVersion("AWS Secrets Manager Extension");
    private String awsS3Version = getExtensionVersion("S3 Remote Accessor Extension");
    private String hashicorpVaultVersion = getExtensionVersion("HashiCorp Vault Extension");
    // liquibase-bigquery-0-SNAPSHOT
    private String googleBigQueryVersion;
    private String liquibaseInterface;
    private String javaVersion = ExceptionUtil.doSilently(() -> {
        return SystemUtil.getJavaVersion();
    });
    private String os = ExceptionUtil.doSilently(() -> {
        return System.getProperty("os.name");
    });
    private String osVersion = ExceptionUtil.doSilently(() -> {
        return System.getProperty("os.version");
    });
    private String osArch = ExceptionUtil.doSilently(() -> {
        return System.getProperty("os.arch");
    });
    // Thinking that this could be a list of events created via flow command?
    // private List<Event> childEvents;

    public Event(String command) {
        this.command = command;
        liquibaseInterface = ExceptionUtil.doSilently(() -> {
            IntegrationDetails integrationDetails = Scope.getCurrentScope().get(Scope.Attr.integrationDetails, IntegrationDetails.class);
            return integrationDetails.getName();
        });
    }

    public void incrementFormattedSqlChangelogCount() {
        formattedSqlChangelogCount++;
    }

    public void incrementSqlChangelogCount() {
        sqlChangelogCount++;
    }

    public void incrementYamlChangelogCount() {
        yamlChangelogCount++;
    }

    public void incrementJsonChangelogCount() {
        jsonChangelogCount++;
    }

    public void incrementXmlChangelogCount() {
        xmlChangelogCount++;
    }

    public Map<String, ?> getPropertiesAsMap() {
        Map<String, Object> properties = new HashMap<>();
        for (Fields field : Fields.values()) {
            try {
                Field refField = this.getClass().getDeclaredField(field.toString());
                refField.setAccessible(true);
                Object value = refField.get(this);
                properties.put(field.toString(), value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    // todo caching here

    /**
     * Given the "Implementation-Name" of an extension, return its version. This is the same mechanism used by
     * the liquibase --version output.
     */
    private static String getExtensionVersion(String extensionName) {
        return ExceptionUtil.doSilently(() -> {
            final Path workingDirectory = Paths.get(".").toAbsolutePath();
            Map<String, VersionUtils.LibraryInfo> libraries = getLibraryInfoMap();
            return libraries.get(extensionName).version;
        });
    }
}