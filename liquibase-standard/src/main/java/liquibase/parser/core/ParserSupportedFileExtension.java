package liquibase.parser.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Liquibase supported file extensions for parsing
 */
public class ParserSupportedFileExtension {

    private ParserSupportedFileExtension() {
        // Prevent instantiation
    }

    public static final Set<String> XML_SUPPORTED_EXTENSIONS = Collections.singleton("xml");
    public static final Set<String> JSON_SUPPORTED_EXTENSIONS = Collections.singleton("json");
    public static final Set<String> YAML_SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList("yaml", "yml"));


}
