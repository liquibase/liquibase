package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import liquibase.analytics.configuration.RemoteAnalyticsConfiguration;
import liquibase.analytics.configuration.SegmentAnalyticsConfiguration;
import liquibase.integration.IntegrationDetails;
import liquibase.util.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
    /**
     * This is a list of events created during the execution of the current command, because some commands
     * execute other commands inside of them, like the flow command.
     */
    private List<Event> childEvents = new ArrayList<>();
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
        // Exclude the childEvents field because it should be handled separately
        for (Fields field : Arrays.stream(Fields.values()).filter(f -> f != Fields.childEvents).collect(Collectors.toList())) {
            try {
                Field refField = this.getClass().getDeclaredField(field.toString());
                refField.setAccessible(true);
                Object value = refField.get(this);
                properties.put(field.toString(), value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        addExtensionsToProperties(properties);
        return properties;
    }

    private void addExtensionsToProperties(Map<String, Object> properties) {
        ExceptionUtil.doSilently(() -> {
            AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
            SegmentAnalyticsConfiguration analyticsConfiguration = ((SegmentAnalyticsConfiguration) analyticsConfigurationFactory.getPlugin());
            List<RemoteAnalyticsConfiguration.ExtensionName> extensionNames = analyticsConfiguration.getExtensionNames();
            if (extensionNames != null) {
                for (RemoteAnalyticsConfiguration.ExtensionName extensionName : extensionNames) {
                    String manifestName = extensionName.getManifestName();
                    String displayName = extensionName.getDisplayName();
                    String extensionVersion = getExtensionVersion(manifestName);

                    // Always insert the version if it's not null.
                    // If the version is null, the extension is not installed, or there are multiple versions of the
                    // same extension where the manifest name has changed over time. Thus, we should not replace any
                    // existing versions in the properties with a null, if a version already exists in the properties.
                    if (extensionVersion != null || !properties.containsKey(displayName)) {
                        properties.put(displayName, extensionVersion);
                    }
                }
            }
        });
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