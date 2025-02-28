package liquibase.util;

import liquibase.Scope;
import liquibase.logging.mdc.MdcKey;

public class LogUtil {


    /**
     * In {@link LiquibaseCommandLine#addEmptyMdcValues()}, baseline values are added to the MDC with empty strings.
     * It is desired that {@link MdcKey#CHANGESET_ID}, {@link MdcKey#CHANGESET_AUTHOR} and {@link MdcKey#LIQUIBASE_SYSTEM_USER}
     * are not ever cleared from the scope, except when the command finishes executing (or they are replaced with a new
     * value). When the command finishes executing, these keys must be set back to an empty string so that incorrect
     * values do not persist outside of command execution (like would occur during flow). {@link MdcKey#LIQUIBASE_SYSTEM_USER}
     * is purposefully excluded from this method because it does not need to be reset when command execution finishes
     * because it should be constant throughout the entire Liquibase execution.
     */
    public static void setPersistedMdcKeysToEmptyString() {
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_ID, "", false);
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_AUTHOR, "", false);
    }
}
