package liquibase.parser.core.yaml;

import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogNodeParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlChangeLogParser extends YamlParser implements ChangeLogNodeParser {
    @Override
    public ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
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
            if (!parsedYaml.containsKey(DATABASE_CHANGE_LOG)) {
                throw new ChangeLogParseException("Could not find databaseChangeLog node");
            }

            Object rootList = parsedYaml.get(DATABASE_CHANGE_LOG);

            if (rootList != null && !(rootList instanceof List)) {
                throw new ChangeLogParseException("databaseChangeLog does not contain a list of entries. Each changeSet must begin ' - changeSet:'");
            }
            ParsedNode databaseChangeLogNode = new ParsedNode(null, DATABASE_CHANGE_LOG);
            databaseChangeLogNode.setValue(rootList);
            return databaseChangeLogNode;
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
