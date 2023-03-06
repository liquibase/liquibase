package liquibase.util;

import liquibase.Scope;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * This class provides methods that are necessary because older (< 1.32) versions of SnakeYaml do not have
 * those methods. Thus, if someone is using an older version of SnakeYaml, we do not want to interrupt Liquibase
 * execution with an exception anymore.
 */
public class SnakeYamlUtil {

    private static boolean showErrorMessage = true;

    private SnakeYamlUtil() {

    }

    /**
     * Safely set the code point limit when configuring a new SnakeYaml instance.
     */
    public static void setCodePointLimitSafely(LoaderOptions loaderOptions, int codePointLimit) {
        safelyCallNewSnakeYamlMethod(() -> loaderOptions.setCodePointLimit(codePointLimit));
    }

    /**
     * Safely set configuration to process comments when configuring a new SnakeYaml instance. This method
     * had the return type changed.
     */
    public static void setProcessCommentsSafely(LoaderOptions loaderOptions, boolean enable) {
        safelyCallNewSnakeYamlMethod(() -> loaderOptions.setProcessComments(enable));
    }

    /**
     * Helper method to make sure that we display the error message only once.
     */
    private static void safelyCallNewSnakeYamlMethod(Runnable code) {
        try {
            code.run();
        } catch (NoSuchMethodError | BootstrapMethodError e) {
            if (showErrorMessage) {
                showErrorMessage = false;
                Scope.getCurrentScope().getLog(SnakeYamlUtil.class).warning(
                        "Failed to set code point limit for SnakeYaml, because the version of SnakeYaml being used is too old. " +
                                "Consider upgrading to a SnakeYaml version equal to or newer than 1.32, by downloading and " +
                                "installing a newer version of Liquibase (which includes a newer version of SnakeYaml). " +
                                "Loading particularly large JSON and YAML documents (like snapshots) in Liquibase may fail if SnakeYaml is not upgraded.", e);
            }
        }
    }
}
