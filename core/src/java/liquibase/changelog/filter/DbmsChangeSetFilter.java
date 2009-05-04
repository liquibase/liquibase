package liquibase.changelog.filter;

import liquibase.ChangeSet;
import liquibase.database.Database;

public class DbmsChangeSetFilter implements ChangeSetFilter {

    private String databaseString;

    public DbmsChangeSetFilter(Database database) {
        this.databaseString = database.getTypeName();
    }

    public boolean accepts(ChangeSet changeSet) {
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
