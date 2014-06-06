package liquibase.sql;

import liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface Sql extends ExecutableUpdate, ExecutableQuery, ExecutableExecute {
    public String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
