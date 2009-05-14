package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.UpdateStatement;
import liquibase.executor.ExecutorService;

import java.util.ArrayList;
import java.util.List;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    public List<RanChangeSet> ranChangeSets;
    private Database database;

    public ShouldRunChangeSetFilter(Database database) throws JDBCException {
        this.database = database;
        this.ranChangeSets = database.getRanChangeSetList();
    }

    @SuppressWarnings({"RedundantIfStatement"})
    public boolean accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equals(changeSet.getId())
                    && ranChangeSet.getAuthor().equals(changeSet.getAuthor())
                    && isPathEquals(changeSet, ranChangeSet)) {

                if (!changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum())) {
                    UpdateStatement md5sumUpdateStatement = new UpdateStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName());
                    md5sumUpdateStatement.addNewColumnValue("MD5SUM", changeSet.generateCheckSum());
                    md5sumUpdateStatement.setWhereClause("ID = ? AND AUTHOR = ? AND FILENAME = ?");
                    md5sumUpdateStatement.addWhereParameter(changeSet.getId());
                    md5sumUpdateStatement.addWhereParameter(changeSet.getAuthor());
                    md5sumUpdateStatement.addWhereParameter(changeSet.getFilePath());

                    try {
                        ExecutorService.getInstance().getWriteExecutor(database).update(md5sumUpdateStatement, new ArrayList<SqlVisitor>());
                    } catch (JDBCException e) {
                        throw new RuntimeException(e);
                    }
                }

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
            return ranChangeSet.getChangeLog().equals(changeSet.getFilePath());
        }

    }
}
