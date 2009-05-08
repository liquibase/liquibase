package liquibase.sql;

import liquibase.database.structure.DatabaseObject;

import java.util.Collection;

public interface Sql {
    public String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
