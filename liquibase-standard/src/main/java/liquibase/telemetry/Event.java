package liquibase.telemetry;

import liquibase.Scope;
import liquibase.integration.IntegrationDetails;
import liquibase.util.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
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
        addExtensionsToProperties(properties);
        return properties;
    }

    private void addExtensionsToProperties(Map<String, Object> properties) {
        ExceptionUtil.doSilently(() -> {
            Map<String, VersionUtils.LibraryInfo> extensionInfoMap = EXTENSIONS_CACHE.get();
            for (Map.Entry<String, VersionUtils.LibraryInfo> extensionInfo : extensionInfoMap.entrySet()) {
                String name = extensionInfo.getKey();
                VersionUtils.LibraryInfo libraryInfo = extensionInfo.getValue();
                if (StringUtils.containsIgnoreCase(name, "liquibase")
                        || StringUtils.containsIgnoreCase(name, "commercial")
                        || StringUtils.containsIgnoreCase(libraryInfo.file.getName(), "liquibase")
                        || StringUtils.containsIgnoreCase(libraryInfo.file.getName(), "commercial")) {
                    properties.put("ext_" + StringUtil.toCamelCase(name).replace(" ", ""), libraryInfo.version);
                }
            }
        });
    }
}