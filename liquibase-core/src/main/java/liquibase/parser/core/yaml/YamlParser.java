package liquibase.parser.core.yaml;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.LiquibaseParser;
import liquibase.resource.ResourceAccessor;

public abstract class YamlParser implements LiquibaseParser {

    protected Logger log = LogFactory.getLogger();

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


}
