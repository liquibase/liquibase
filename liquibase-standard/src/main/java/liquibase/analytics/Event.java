package liquibase.analytics;

import liquibase.Scope;
import liquibase.analytics.configuration.AnalyticsArgs;
import liquibase.analytics.configuration.AnalyticsConfigurationFactory;
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration;
import liquibase.analytics.configuration.RemoteAnalyticsConfiguration;
import liquibase.integration.IntegrationDetails;
import liquibase.util.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.BooleanUtils;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static liquibase.util.VersionUtils.getLibraryInfoMap;

@FieldNameConstants(asEnum = true)
@Data
public class Event {

    public static final String JAVA_API_INTEGRATION_NAME = "JavaAPI";

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
    /**
     * Is the code running in GitHub actions? See
     * https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables:~:text=actions/checkout.-,GITHUB_ACTIONS,-Always%20set%20to
     */
    private boolean isGithubActions = Boolean.TRUE.equals(ExceptionUtil.doSilently(() -> BooleanUtils.toBoolean(System.getenv("GITHUB_ACTIONS"))));
    /**
     * Is the code running in a CI environment? See
     * https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables:~:text=Description-,CI,-Always%20set%20to
     */
    private boolean isCi = Boolean.TRUE.equals(ExceptionUtil.doSilently(() -> BooleanUtils.toBoolean(System.getenv("CI"))));
    /**
     * Is the code running in Liquibase IO?
     */
    private boolean isIO = Boolean.TRUE.equals(ExceptionUtil.doSilently(() -> BooleanUtils.toBoolean(System.getenv("isIO"))));
    /**
     * The event might be transmitted at a different time than it was cached, so set the timestamp here, in UTC time.
     * see https://segment.com/docs/connections/spec/common/#timestamps for more info
     */
    private String timestamp = Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    // Save the isDocker value statically to avoid executing it multiple times
    private static volatile Boolean cachedIsDocker = null;

    public Event(String command) {
        this.command = command;
        liquibaseInterface = ExceptionUtil.doSilently(() -> {
            IntegrationDetails integrationDetails = Scope.getCurrentScope().get(Scope.Attr.integrationDetails, IntegrationDetails.class);
            if (integrationDetails != null) {
                return integrationDetails.getName();
            } else {
                // If no integration details exist in the scope, we assume that the CommandScope is being constructed
                // directly and thus is a JavaAPI.
                return JAVA_API_INTEGRATION_NAME;
            }
        });

        isDocker = detectDockerEnvironment();
        isLiquibaseDocker = isDocker && BooleanUtils.toBoolean(System.getenv("DOCKER_LIQUIBASE"));
        isAwsLiquibaseDocker = isDocker && BooleanUtils.toBoolean(System.getenv("DOCKER_AWS_LIQUIBASE"));
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
        Map<String, Object> properties = new LinkedHashMap<>();
        // Exclude the childEvents field because it should be handled separately
        // Exclude the timestamp field because it is POSTed in the AnalyticsTrackEvent object
        for (Fields field : Arrays.stream(Fields.values()).filter(f -> !Arrays.asList(Fields.childEvents, Fields.timestamp).contains(f)).collect(Collectors.toList())) {
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
            LiquibaseRemoteAnalyticsConfiguration analyticsConfiguration = ((LiquibaseRemoteAnalyticsConfiguration) analyticsConfigurationFactory.getPlugin());
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

    private static boolean detectDockerEnvironment() {
        if (cachedIsDocker != null) {
            return cachedIsDocker;
        }

        // Double-check locking for thread safety
        synchronized (Event.class) {
            if (cachedIsDocker != null) {
                return cachedIsDocker;
            }

            cachedIsDocker = Boolean.TRUE.equals(ExceptionUtil.doSilently(() -> {
                if (BooleanUtils.toBoolean(System.getenv("DOCKER_LIQUIBASE")) ||
                        BooleanUtils.toBoolean(System.getenv("DOCKER_AWS_LIQUIBASE"))) {
                    return true;
                }

                // Only do file I/O on Linux/Unix systems
                String osName = System.getProperty("os.name", "").toLowerCase();
                if (!osName.contains("linux") && !osName.contains("unix")) {
                    return false;
                }

                if (Files.exists(Paths.get("/.dockerenv"))) {
                    return true;
                }

                // Only check cgroup if all else fails
                Path cgroupPath = Paths.get("/proc/1/cgroup");
                if (Files.exists(cgroupPath)) {
                    String cgroupContent = new String(Files.readAllBytes(cgroupPath));
                    return cgroupContent.contains("docker") || cgroupContent.contains("kubepods");
                }

                return false;
            }));

            return cachedIsDocker;
        }
    }

}