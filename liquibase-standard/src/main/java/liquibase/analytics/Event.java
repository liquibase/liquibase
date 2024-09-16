package liquibase.analytics;

import liquibase.Scope;
import liquibase.integration.IntegrationDetails;
import liquibase.util.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static liquibase.util.VersionUtils.getLibraryInfoMap;

@FieldNameConstants(asEnum = true)
@Data
public class Event {

    /**
     * Cached extensions, to avoid costly lookups for each extension.
     */
    private final static Cache<Map<String, VersionUtils.LibraryInfo>> EXTENSIONS_CACHE = new Cache<>(() -> {
        return getLibraryInfoMap();
    });

    private final String command;
    private boolean reportsEnabled;
    private boolean dbclhEnabled;
    private boolean structuredLogsEnabled;
    private String operationOutcome;
    private String exceptionClass;
    /**
     * SQL changelogs are different from formatted SQL. SQL changelogs are essentially raw SQL files, with no Liquibase headers or comments.
     */
    private int chlog_sql;
    private int chlog_formattedSql;
    private int chlog_xml;
    private int chlog_json;
    private int chlog_yaml;
    private String databasePlatform;
    private String databaseVersion;
    private String liquibaseVersion = ExceptionUtil.doSilently(() -> {
        return LiquibaseUtil.getBuildVersionInfo();
    });
    private String mongoDbVersion = getExtensionVersion("Liquibase MongoDB Commercial Extension");
    private String ext_dynamoDb = getExtensionVersion("Liquibase DynamoDB Commercial Extension");
    private String ext_checks = getExtensionVersion("Checks Extension");
    private String ext_awsSecrets = getExtensionVersion("AWS Secrets Manager Extension");
    private String ext_awsS3 = getExtensionVersion("S3 Remote Accessor Extension");
    private String ext_hashicorpVault = getExtensionVersion("HashiCorp Vault Extension");
    private String ext_googleBigQuery = getExtensionVersion("Liquibase BigQuery Commercial Extension");
    private String ext_databricks = getExtensionVersion("Liquibase Commercial Databricks Extension");
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
    /**
     * Is the code running in a docker container?
     */
    private boolean isDocker;
    /**
     * Is the code running in a docker container that is created/maintained by Liquibase?
     */
    private boolean isLiquibaseDocker;
    /**
     * Is the code running in the AWS specific docker container that is created/maintained by Liquibase. Note that this
     * is a different from the container that is published to the public ECR. This is the container that someone uses
     * when they purchase Liquibase Pro through the AWS Marketplace.
     */
    private boolean isAwsLiquibaseDocker;

    public Event(String command) {
        this.command = command;
        liquibaseInterface = ExceptionUtil.doSilently(() -> {
            IntegrationDetails integrationDetails = Scope.getCurrentScope().get(Scope.Attr.integrationDetails, IntegrationDetails.class);
            return integrationDetails.getName();
        });
        isLiquibaseDocker = ExceptionUtil.doSilently(() -> {
            return BooleanUtils.toBoolean(System.getenv("DOCKER_LIQUIBASE"));
        });
        isAwsLiquibaseDocker = ExceptionUtil.doSilently(() -> {
            return BooleanUtils.toBoolean(System.getenv("DOCKER_AWS_LIQUIBASE"));
        });
        isDocker = ExceptionUtil.doSilently(() -> {
            if (isLiquibaseDocker || isAwsLiquibaseDocker) {
                return true;
            }
            boolean dockerenvExists = Files.exists(Paths.get("/.dockerenv"));
            if (dockerenvExists) {
                return true;
            }
            String cgroupContent = new String(Files.readAllBytes(Paths.get("/proc/1/cgroup")));
            return cgroupContent.contains("docker") || cgroupContent.contains("kubepods");
        });
    }

    public void incrementFormattedSqlChangelogCount() {
        chlog_formattedSql++;
    }

    public void incrementSqlChangelogCount() {
        chlog_sql++;
    }

    public void incrementYamlChangelogCount() {
        chlog_yaml++;
    }

    public void incrementJsonChangelogCount() {
        chlog_json++;
    }

    public void incrementXmlChangelogCount() {
        chlog_xml++;
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

    /**
     * Given the "Implementation-Name" of an extension, return its version. This is the same mechanism used by
     * the liquibase --version output.
     */
    private static String getExtensionVersion(String extensionName) {
        return ExceptionUtil.doSilently(() -> {
            Map<String, VersionUtils.LibraryInfo> libraries = EXTENSIONS_CACHE.get();
            return libraries.get(extensionName).version;
        });
    }
}