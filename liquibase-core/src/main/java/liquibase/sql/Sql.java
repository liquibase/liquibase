package liquibase.sql;

import liquibase.action.ExecuteAction;
import liquibase.action.QueryAction;
import liquibase.action.UpdateAction;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface Sql extends UpdateAction, QueryAction, ExecuteAction {
    public String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
