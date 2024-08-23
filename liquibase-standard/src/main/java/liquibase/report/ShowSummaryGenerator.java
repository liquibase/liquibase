package liquibase.report;

import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSetStatus;
import liquibase.plugin.Plugin;

import java.util.List;

public interface ShowSummaryGenerator extends Plugin {

    /**
     *
     * This method returns a priority value for an implementation. Liquibase uses this to
     * determine which LicenseService is currently in use. There can only be a single
     * LicenseService used at a time, and the highest priority implementation wins.
     *
     * @return  int
     *
     */
    int getPriority();

    /**
     * Get all additional change set statuses that should be reported in the show summary verbose output.
     */
    List<ChangeSetStatus> getAllAdditionalChangeSetStatus(ChangeLogIterator runChangeLogIterator);

    /**
     * Append any additional summary messages that shuold be reported in the show summary output.
     */
    void appendAdditionalSummaryMessages(StringBuilder builder, ChangeLogIterator runChangeLogIterator);
}
