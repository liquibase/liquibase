package liquibase.database.statement.syntax;

import liquibase.database.structure.DatabaseObject;
import liquibase.database.Database;

import java.util.Collection;

public interface Sql {
    public String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
