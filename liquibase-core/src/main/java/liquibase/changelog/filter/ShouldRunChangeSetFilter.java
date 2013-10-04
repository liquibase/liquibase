package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.core.UpdateStatement;

import java.util.ArrayList;
import java.util.List;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    public List<RanChangeSet> ranChangeSets;
    private Database database;

    public ShouldRunChangeSetFilter(Database database) throws DatabaseException {
        this.database = database;
        this.ranChangeSets = database.getRanChangeSetList();
    }

    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public boolean accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equals(changeSet.getId())
                    && ranChangeSet.getAuthor().equals(changeSet.getAuthor())
                    && isPathEquals(changeSet, ranChangeSet)) {
                if (changeSet.shouldAlwaysRun()) {
                    return true;
                } else if (changeSet.shouldRunOnChange() && !changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPathEquals(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return ranChangeSet.getChangeLog().equalsIgnoreCase(changeSet.getFilePath());
        } else {
            return ranChangeSet.getChangeLog().equalsIgnoreCase(changeSet.getFilePath());
        }

    }
}
