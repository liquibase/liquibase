package liquibase.parser.core.yaml;

import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.parser.LiquibaseParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.SnakeYamlUtil;
import org.yaml.snakeyaml.LoaderOptions;

public abstract class YamlParser implements LiquibaseParser {

    protected Logger log = Scope.getCurrentScope().getLog(getClass());

    public static LoaderOptions createLoaderOptions() {
        LoaderOptions options = new LoaderOptions();
        SnakeYamlUtil.setCodePointLimitSafely(options, Integer.MAX_VALUE);
        SnakeYamlUtil.setProcessCommentsSafely(options, false);
        // TODO: remove the below line when we have a general fix for the not allowed duplicated databaseChangelog and sql tags
        //        options.setAllowDuplicateKeys(false);
        options.setAllowRecursiveKeys(false);
        return options;
    }

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
