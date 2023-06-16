package liquibase.changelog.filter;

import liquibase.change.Change;
import liquibase.change.DbmsTargetedChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DbmsChangeSetFilter implements ChangeSetFilter {

    private final Database database;
    public static final String MDC_NAME = "dbmsMismatch";
    public static final String DISPLAY_NAME = "DBMS mismatch";

    public DbmsChangeSetFilter(Database database) {
        this.database = database;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (database == null) {
            return new ChangeSetFilterResult(true, "No database connection, cannot evaluate dbms attribute", this.getClass(), getMdcName(), getDisplayName());
        }
        List<SqlVisitor> visitorsToRemove = new ArrayList<>();
        List<Change> changesToRemove = new ArrayList<>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if (!DatabaseList.definitionMatches(visitor.getApplicableDbms(), database, true)) {
                visitorsToRemove.add(visitor);
            }
        }
        for(Change change : changeSet.getChanges()){
            if (((change instanceof DbmsTargetedChange)) && !DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), database, true)){
                changesToRemove.add(change);
            }
        }

        changeSet.getSqlVisitors().removeAll(visitorsToRemove);
        changeSet.removeAllChanges(changesToRemove);

        String dbmsList;
        if ((changeSet.getDbmsSet() == null) || changeSet.getDbmsSet().isEmpty()) {
            dbmsList = "all databases";
        } else {
            dbmsList = "'"+ StringUtil.join(changeSet.getDbmsSet(), ", ") + "'";
        }

        if (DatabaseList.definitionMatches(changeSet.getDbmsSet(), database, true)) {
            return new ChangeSetFilterResult(true, "Database '" + database.getShortName() + "' matches " + dbmsList, this.getClass(), getMdcName(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(false, "Database '"+database.getShortName()+"' does not match "+dbmsList, this.getClass(), getMdcName(), getDisplayName());
        }
    }

    @Override
    public String getMdcName() {
        return MDC_NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
