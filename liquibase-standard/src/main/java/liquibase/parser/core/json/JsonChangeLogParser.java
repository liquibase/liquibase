package liquibase.parser.core.json;

import liquibase.parser.core.yaml.YamlChangeLogParser;

import java.util.Collections;
import java.util.Set;

public class JsonChangeLogParser extends YamlChangeLogParser {

    public static final Set<String> SUPPORTED_EXTENSIONS = Collections.singleton("json");

    @Override
    protected String[] getSupportedFileExtensions() {
        return SUPPORTED_EXTENSIONS.toArray(new String[0]);
    }
}
