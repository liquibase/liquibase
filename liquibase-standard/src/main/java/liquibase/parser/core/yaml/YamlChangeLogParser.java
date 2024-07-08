package liquibase.parser.core.yaml;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class YamlChangeLogParser extends YamlParser implements ChangeLogParser {
    private static final String DATABASE_CHANGE_LOG = "databaseChangeLog";

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        Yaml yaml = new Yaml(new CustomSafeConstructor(createLoaderOptions()));

        try {
            Resource changelog = resourceAccessor.get(physicalChangeLogLocation);
            if (!changelog.exists()) {
                throw new ChangeLogParseException(physicalChangeLogLocation + " does not exist");
            }

            Map parsedYaml;
            try (InputStream changeLogStream = changelog.openInputStream()) {
                parsedYaml = parseYamlStream(physicalChangeLogLocation, yaml, changeLogStream);
            }

            if ((parsedYaml == null) || parsedYaml.isEmpty()) {
                throw new ChangeLogParseException("Empty file " + physicalChangeLogLocation);
            }
            DatabaseChangeLog changeLog = new DatabaseChangeLog(DatabaseChangeLog.normalizePath(physicalChangeLogLocation));

            if (!parsedYaml.containsKey(DATABASE_CHANGE_LOG)) {
                throw new ChangeLogParseException("Could not find databaseChangeLog node");
            }

            Object rootList = parsedYaml.get(DATABASE_CHANGE_LOG);
            if (rootList == null) {
                changeLog.setChangeLogParameters(changeLogParameters);
                return changeLog;
            }

            if (!(rootList instanceof List)) {
                throw new ChangeLogParseException("databaseChangeLog does not contain a list of entries. Each changeSet must begin ' - changeSet:'");
            }

            for (Object obj : (List) rootList) {
                if (obj instanceof Map) {
                    if (((Map<?, ?>) obj).containsKey("property")) {
                        Map property = (Map) ((Map<?, ?>) obj).get("property");
                        ContextExpression context = new ContextExpression((String) property.get("context"));
                        Labels labels = new Labels((String) property.get("labels"));

                        Boolean global = getGlobalParam(property);

                        if (property.containsKey("name")) {
                            Object value = property.get("value");

                            changeLogParameters.set((String) property.get("name"), value, context, labels, (String) property.get("dbms"), global, changeLog);
                        } else if (property.containsKey("file")) {
                            loadChangeLogParametersFromFile(changeLogParameters, resourceAccessor, changeLog, property,
                                    context, labels, global);
                        }
                    }
                }
            }


            replaceParameters(parsedYaml, changeLogParameters, changeLog);

            changeLog.setChangeLogParameters(changeLogParameters);
            ParsedNode databaseChangeLogNode = new ParsedNode(null, DATABASE_CHANGE_LOG);
            databaseChangeLogNode.setValue(rootList);

            changeLog.load(databaseChangeLogNode, resourceAccessor);

            return changeLog;
        } catch (ChangeLogParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ChangeLogParseException("Error parsing " + physicalChangeLogLocation + " : " + e.getMessage(), e);
        }
    }

    private Map parseYamlStream(String physicalChangeLogLocation, Yaml yaml, InputStream changeLogStream) throws ChangeLogParseException {
        Map parsedYaml;
        try {
            parsedYaml = yaml.load(changeLogStream);
        } catch (Exception e) {
            throw new ChangeLogParseException("Syntax error in file " + physicalChangeLogLocation + ": " + e.getMessage(), e);
        }
        return parsedYaml;
    }

    private void loadChangeLogParametersFromFile(ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor, DatabaseChangeLog changeLog, Map property, ContextExpression context, Labels labels, Boolean global) throws IOException, LiquibaseException {
        Properties props = new Properties();
        Boolean relativeToChangelogFile = (Boolean) property.get("relativeToChangelogFile");
        Boolean errorIfMissing = (Boolean) property.get("errorIfMissing");
        String file = (String) property.get("file");

        if (relativeToChangelogFile == null) {
            relativeToChangelogFile = false;
        }

        if (errorIfMissing == null) {
            errorIfMissing = true;
        }

        Resource resource;

        if (relativeToChangelogFile) {
            resource = resourceAccessor.get(changeLog.getPhysicalFilePath()).resolveSibling(file);
        } else {
            resource = resourceAccessor.get(file);
        }

        if (!resource.exists()) {
            if (errorIfMissing) {
                throw new UnexpectedLiquibaseException(FileUtil.getFileNotFoundMessage(file));
            }
            else {
                Scope.getCurrentScope().getLog(getClass()).warning(FileUtil.getFileNotFoundMessage(file));
            }
        } else {
            try (InputStream stream = resource.openInputStream()) {
                props.load(stream);
            }

            for (Map.Entry entry : props.entrySet()) {
                changeLogParameters.set(entry.getKey().toString(), entry.getValue().toString(), context, labels, (String) property.get("dbms"), global, changeLog);
            }
        }
    }

    /**
     * Extract the global parameter from the properties.
     *
     * @param property the map of props
     * @return the global param
     */
    private Boolean getGlobalParam(Map property) {
        Boolean global = null;
        Object globalObj = property.get("global");
        if (globalObj == null) {
            global = true;
        } else {
            global = (Boolean) globalObj;
        }
        return global;
    }

    protected void replaceParameters(Object obj, ChangeLogParameters changeLogParameters, DatabaseChangeLog changeLog) throws ChangeLogParseException {
        if (obj instanceof Map) {
            for (Map.Entry entry : (Set<Map.Entry>) ((Map) obj).entrySet()) {
                if ((entry.getValue() instanceof Map) || (entry.getValue() instanceof Collection)) {
                    replaceParameters(entry.getValue(), changeLogParameters, changeLog);
                } else if (entry.getValue() instanceof String) {
                    entry.setValue(changeLogParameters.expandExpressions((String) entry.getValue(), changeLog));
                }
            }
        } else if (obj instanceof Collection) {
            ListIterator iterator = ((List) obj).listIterator();
            while (iterator.hasNext()) {
                Object child = iterator.next();
                if ((child instanceof Map) || (child instanceof Collection)) {
                    replaceParameters(child, changeLogParameters, changeLog);
                } else if (child instanceof String) {
                    iterator.set(changeLogParameters.expandExpressions((String) child, changeLog));
                }
            }
        }
    }

    static class CustomSafeConstructor extends SafeConstructor {
        /**
         * Create an instance
         *
         * @param loaderOptions - the configuration options
         */
        public CustomSafeConstructor(LoaderOptions loaderOptions) {
            super(loaderOptions);
            this.yamlConstructors.put(Tag.TIMESTAMP, new CustomConstructYamlTimestamp());
        }
    }
}
