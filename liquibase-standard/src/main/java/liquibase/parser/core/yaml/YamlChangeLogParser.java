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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class YamlChangeLogParser extends YamlParser implements ChangeLogParser {

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        Yaml yaml = CustomTimestampSafeConstructor.createYaml();

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

    /**
     * A simple "interceptor" InputStream to ensure that the YAML stream doesn't contain two "databaseChangeLog" tags,
     * which would otherwise cause silent omissions if something is copy/pasted wrongly into the file.
     */
    static class DoubleChangelogCheckerInputStream extends FilterInputStream {

        byte[] databaseChangeLog = "databaseChangeLog".getBytes(StandardCharsets.UTF_8);
        int scanPos;
        boolean databaseChangeLogSeen;

        DoubleChangelogCheckerInputStream(InputStream in) {
            super(in);
            scanPos = 0;
            databaseChangeLogSeen = false;
        }

        private void processByte(int nextByte) {
            // scanPos will be -1 if we aren't in a position where "databaseChangeLog" would cause a problem
            // i.e. the beginning of a file or of a line
            if (nextByte == 10) {
                // a new line means we should expect to see it again
                scanPos = 0;
            } else if (scanPos > -1 && nextByte == databaseChangeLog[scanPos]) {
                scanPos++;
                if (scanPos == databaseChangeLog.length) {
                    if (databaseChangeLogSeen) {
                        throw new IllegalStateException("Two 'databaseChangeLog' tags found in a single file!");
                    } else {
                        databaseChangeLogSeen = true;
                        scanPos = -1;
                    }
                }
            } else {
                // no point in scanning since the string was not found
                scanPos = -1;
            }
        }

        @Override
        public int read() throws IOException {
            int nextByte = super.read();
            processByte(nextByte);
            return nextByte;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int numBytes = super.read(b);

            for (int i=0; i<numBytes; i++) {
                processByte(b[i]);
            }

            return numBytes;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int numBytes = super.read(b, off, len);

            for (int i=off; i < off + numBytes; i++) {
                processByte(b[i]);
            }
            return numBytes;
        }
    }

    private Map parseYamlStream(String physicalChangeLogLocation, Yaml yaml, InputStream changeLogStream) throws ChangeLogParseException {
        Map parsedYaml;
        try {
            parsedYaml = yaml.load(new DoubleChangelogCheckerInputStream(changeLogStream));
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

    private static class CustomTimestampSafeConstructor extends SafeConstructor {
        /**
         * Create an instance
         *
         * @param loaderOptions - the configuration options
         */
        public CustomTimestampSafeConstructor(LoaderOptions loaderOptions) {
            super(loaderOptions);
            this.yamlConstructors.put(Tag.TIMESTAMP, new CustomConstructYamlTimestamp());
        }

        private static Yaml createYaml() {
            return new Yaml(new CustomTimestampSafeConstructor(createLoaderOptions()));
        }
    }
}
