package liquibase.parser.filter;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.sql.UpdateStatement;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.exception.JDBCException;

import java.util.List;
import java.util.ArrayList;

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

                if (!changeSet.getMd5sum().equals(ranChangeSet.getMd5sum())) {
                    UpdateStatement md5sumUpdateStatement = new UpdateStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName());
                    md5sumUpdateStatement.addNewColumnValue("MD5SUM", changeSet.getMd5sum());
                    md5sumUpdateStatement.setWhereClause("ID = ? AND AUTHOR = ? AND FILENAME = ?");
                    md5sumUpdateStatement.addWhereParameter(changeSet.getId());
                    md5sumUpdateStatement.addWhereParameter(changeSet.getAuthor());
                    md5sumUpdateStatement.addWhereParameter(changeSet.getFilePath());

                    try {
                        database.getJdbcTemplate().update(md5sumUpdateStatement, new ArrayList<SqlVisitor>());
                    } catch (JDBCException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (changeSet.shouldAlwaysRun()) {
                    return true;
                } else if (changeSet.shouldRunOnChange() && !changeSet.getMd5sum().equals(ranChangeSet.getMd5sum())) {
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
