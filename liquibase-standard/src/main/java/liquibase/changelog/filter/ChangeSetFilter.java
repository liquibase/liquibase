package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public interface ChangeSetFilter {

    ChangeSetFilterResult accepts(ChangeSet changeSet);

    /**
     * @return a descriptive name for the filter, which will be used in the MDC entries for this filter
     */
    default String getMdcName() {
        return getClass().getSimpleName();
    }
}
