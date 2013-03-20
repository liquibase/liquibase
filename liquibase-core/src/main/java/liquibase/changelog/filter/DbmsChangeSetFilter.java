package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.sql.visitor.SqlVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DbmsChangeSetFilter implements ChangeSetFilter {

    private String databaseString;

    public DbmsChangeSetFilter(Database database) {
        this.databaseString = database.getShortName();
    }

    public boolean accepts(ChangeSet changeSet) {
         List<SqlVisitor> visitorsToRemove = new ArrayList<SqlVisitor>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if (databaseString == null) {
                continue;
            }
            if (visitor.getApplicableDbms() != null && visitor.getApplicableDbms().size() > 0) {
                if (!visitor.getApplicableDbms().contains(databaseString)) {
                    visitorsToRemove.add(visitor);
                }
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        if (databaseString == null) {
            return true;
        }
        Set<String> dbmsSet = changeSet.getDbmsSet();

        if (dbmsSet == null || dbmsSet.isEmpty()) {
            return true;
        }

        // !h2 would mean that the h2 database should be excluded
        if (dbmsSet.contains("!" + databaseString)) {
            return false;
        }

        Set<String> dbmsSupported = new HashSet<String>();
        // add all dbms that do not start with ! to a list
        for (String dbms: dbmsSet) {
            if (!dbms.startsWith("!")) {
                dbmsSupported.add(dbms);
            }
        }


        if (dbmsSupported.isEmpty() || dbmsSupported.contains(databaseString)) {
            return true;
        }

        return false;
    }
}
