package liquibase.util;

import liquibase.Scope;
import org.yaml.snakeyaml.LoaderOptions;

public class SnakeYamlUtil {
    private SnakeYamlUtil() {

    }

    /**
     * Safely set the code point limit when configuring a new SnakeYaml instance. This method is necessary because
     * older versions of SnakeYaml do not have the ability to set a code point limit. Thus, if someone is using an older
     * version of SnakeYaml, we do not want to interrupt Liquibase execution with an exception.
     */
    public static void setCodePointLimitSafely(LoaderOptions loaderOptions, int codePointLimit) {
        try {
            loaderOptions.setCodePointLimit(codePointLimit);
        } catch (NoSuchMethodError e) {
            Scope.getCurrentScope().getLog(SnakeYamlUtil.class).warning(
                    "Failed to set code point limit for SnakeYaml, because the version of SnakeYaml being used is too old. " +
                            "Consider upgrading to a SnakeYaml version equal to or newer than 1.32, by downloading and " +
                            "installing a newer version of Liquibase (which includes a newer version of SnakeYaml). " +
                            "Loading particularly large JSON and YAML documents (like snapshots) in Liquibase may fail if SnakeYaml is not upgraded.", e);
        }
    }
}
