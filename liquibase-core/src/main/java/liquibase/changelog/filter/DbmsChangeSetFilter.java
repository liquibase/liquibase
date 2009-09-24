package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.sql.visitor.SqlVisitor;

import java.util.List;
import java.util.ArrayList;

public class DbmsChangeSetFilter implements ChangeSetFilter {

    private String databaseString;

    public DbmsChangeSetFilter(Database database) {
        this.databaseString = database.getTypeName();
    }

    public boolean accepts(ChangeSet changeSet) {
         List<SqlVisitor> visitorsToRemove = new ArrayList<SqlVisitor>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if (databaseString != null && visitor.getApplicableDbms() != null && visitor.getApplicableDbms().size() > 0) {
                boolean shouldRemove = true;
                    if (visitor.getApplicableDbms().contains(databaseString)) {
                        shouldRemove = false;
                    }
                if (shouldRemove) {
                    visitorsToRemove.add(visitor);
                }
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        if (databaseString == null) {
            return true;
        }

        if (changeSet.getDbmsSet() == null) {
            return true;
        }

        for (String dbms : changeSet.getDbmsSet()) {
            if (databaseString.equals(dbms)) {
                return true;
            }
        }

        return false;
    }
}
