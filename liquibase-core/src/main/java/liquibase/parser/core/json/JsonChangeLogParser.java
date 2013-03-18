package liquibase.parser.core.json;

import liquibase.parser.core.yaml.YamlChangeLogParser;
import liquibase.resource.ResourceAccessor;

public class JsonChangeLogParser extends YamlChangeLogParser {

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.endsWith(".json");
    }
}
