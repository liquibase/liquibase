package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    private final Database database;
    private final boolean ignoreClasspathPrefix;

    public ShouldRunChangeSetFilter(Database database, boolean ignoreClasspathPrefix) throws DatabaseException {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
        this.database = database;
    }

    public ShouldRunChangeSetFilter(Database database) throws DatabaseException {
        this(database, true);
    }

    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        RanChangeSet latestRanChangeSet = null;

        try {
            for (RanChangeSet ranChangeSet : this.database.getRanChangeSetList()) {
                if (ranChangeSet.isSameAs(changeSet)) {
                    if (latestRanChangeSet == null ||
                            ranChangeSet.getDateExecuted().after(latestRanChangeSet.getDateExecuted())) {
                        latestRanChangeSet = ranChangeSet;
                    }
                }
            }

            if (latestRanChangeSet != null) {
                if (changeSet.shouldAlwaysRun()) {
                    return new ChangeSetFilterResult(true, "Change set always runs", this.getClass());
                }
                if (changeSet.shouldRunOnChange() && checksumChanged(changeSet, latestRanChangeSet)) {
                    return new ChangeSetFilterResult(true, "Change set checksum changed", this.getClass());
                }
                return new ChangeSetFilterResult(false, "Change set already ran", this.getClass());
            }
            return new ChangeSetFilterResult(true, "Change set has not ran yet", this.getClass());
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected boolean checksumChanged(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return !changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum());
    }
}
