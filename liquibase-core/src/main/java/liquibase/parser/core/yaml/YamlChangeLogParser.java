package liquibase.parser.core.yaml;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

public class YamlChangeLogParser implements ChangeLogParser {

    protected Logger log = LogFactory.getLogger();

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        for (String extension : getSupportedFileExtensions()) {
            if (changeLogFile.toLowerCase().endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    protected String[] getSupportedFileExtensions() {
        return new String[] {"yaml", "yml"};
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        Yaml yaml = new Yaml();

        try {
            InputStream changeLogStream = StreamUtil.singleInputStream(physicalChangeLogLocation, resourceAccessor);
            if (changeLogStream == null) {
                throw new ChangeLogParseException(physicalChangeLogLocation + " does not exist");
            }

            Map parsedYaml;
            try {
                parsedYaml = yaml.loadAs(changeLogStream, Map.class);
            } catch (Exception e) {
                throw new ChangeLogParseException("Syntax error in " + getSupportedFileExtensions()[0] + ": " + e.getMessage(), e);
            }

            if (parsedYaml == null || parsedYaml.size() == 0) {
                throw new ChangeLogParseException("Empty file " + physicalChangeLogLocation);
            }

            List rootList = (List) parsedYaml.get("databaseChangeLog");
            if (rootList == null) {
                throw new ChangeLogParseException("Could not find databaseChangeLog node");
            }
            for (Object obj : rootList) {
                if (obj instanceof Map && ((Map) obj).containsKey("property")) {
                    Map property = (Map) ((Map) obj).get("property");
                    ContextExpression context = new ContextExpression((String) property.get("context"));
                    Labels labels = new Labels((String) property.get("labels"));
                    if (property.containsKey("name")) {
                        Object value = property.get("value");
                        if (value != null) {
                            value = value.toString();
                        }
                        changeLogParameters.set((String) property.get("name"), (String) value, context, labels, (String) property.get("dbms"));
                    } else if (property.containsKey("file")) {
                        Properties props = new Properties();
                        InputStream propertiesStream = StreamUtil.singleInputStream((String) property.get("file"), resourceAccessor);
                        if (propertiesStream == null) {
                            log.info("Could not open properties file " + property.get("file"));
                        } else {
                            props.load(propertiesStream);

                            for (Map.Entry entry : props.entrySet()) {
                                changeLogParameters.set(entry.getKey().toString(), entry.getValue().toString(), context, labels, (String) property.get("dbms"));
                            }
                        }
                    }
                }
            }


            replaceParameters(parsedYaml, changeLogParameters);

            DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);
            changeLog.setChangeLogParameters(changeLogParameters);
            ParsedNode databaseChangeLogNode = new ParsedNode(null, "databaseChangeLog");
            databaseChangeLogNode.setValue(rootList);

            changeLog.load(databaseChangeLogNode, resourceAccessor);

            return changeLog;
        } catch (Throwable e) {
            if (e instanceof ChangeLogParseException) {
                throw (ChangeLogParseException) e;
            }
            throw new ChangeLogParseException(e);
        }
    }

    protected void replaceParameters(Object obj, ChangeLogParameters changeLogParameters) {
        if (obj instanceof Map) {
            for (Map.Entry entry : (Set<Map.Entry>) ((Map) obj).entrySet()) {
                if (entry.getValue() instanceof Map || entry.getValue() instanceof Collection) {
                    replaceParameters(entry.getValue(), changeLogParameters);
                } else if (entry.getValue() instanceof String) {
                    entry.setValue(changeLogParameters.expandExpressions((String) entry.getValue()));
                }
            }
        } else if (obj instanceof Collection) {
            ListIterator iterator = ((List) obj).listIterator();
            while (iterator.hasNext()) {
                Object child = iterator.next();
                if (child instanceof Map || child instanceof Collection) {
                    replaceParameters(child, changeLogParameters);
                } else if (child instanceof String) {
                    iterator.set(changeLogParameters.expandExpressions((String) child));
                }
            }
        }
    }
}
