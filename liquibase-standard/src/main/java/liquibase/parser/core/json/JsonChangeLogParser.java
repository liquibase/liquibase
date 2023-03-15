package liquibase.parser.core.json;

import liquibase.parser.core.yaml.YamlChangeLogParser;

public class JsonChangeLogParser extends YamlChangeLogParser {

    @Override
    protected String[] getSupportedFileExtensions() {
        return new String[] {"json"};
    }
}