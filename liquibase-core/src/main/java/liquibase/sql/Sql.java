package liquibase.sql;

import liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface Sql {
    public String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
