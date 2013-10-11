package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.sql.visitor.SqlVisitor;

import java.util.ArrayList;
import java.util.List;

public class DbmsChangeSetFilter implements ChangeSetFilter {

    private Database database;

    public DbmsChangeSetFilter(Database database) {
        this.database = database;
    }

    @Override
    public boolean accepts(ChangeSet changeSet) {
        if (database == null) {
            return true;
        }
         List<SqlVisitor> visitorsToRemove = new ArrayList<SqlVisitor>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if (!DatabaseList.definitionMatches(visitor.getApplicableDbms(), database, true)) {
                visitorsToRemove.add(visitor);
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        return DatabaseList.definitionMatches(changeSet.getDbmsSet(), database, true);
    }
}
