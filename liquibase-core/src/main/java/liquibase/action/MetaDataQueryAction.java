package liquibase.action;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * A base class for {@link liquibase.action.Action}s that fetch metadata about a database.
 * For performance reasons, implementations should be able to handle both requests for a specific record and requests for all objects that match a particular pattern.
 * Standard expectation is that if a search field in a subclass is null, it means "match anything" and if it is set it means "return only objects whose field matches the set value".
 * Subclasses should use the {@link liquibase.ExtensibleObject} methods to store fields so that the default {@link #describe()} method and others work as expected.
 */
public abstract class MetaDataQueryAction extends AbstractExtensibleObject implements QueryAction {

//    /**
//     * Return a QueryResult with a single column in each row with the key of "object" and value of a {@link liquibase.structure.DatabaseObject} implementation.
//     * Subclasses will normally not override this method, but instead override {@link #getRawMetaData(liquibase.Scope)} and {@link #rawMetaDataToObject(liquibase.action.Row, liquibase.Scope)}
//     */
//    @Override
//    public QueryAction.Result query(Scope scope) throws DatabaseException {
//        QueryAction.Result queryResult = getRawMetaData(scope);
//
//        List<Map<String, Object>> finalResult = new ArrayList<Map<String, Object>>();
//        for (Row row : queryResult.toList()) {
//            DatabaseObject object = rawMetaDataToObject(row, scope);
//            Map tableMap = new HashMap();
//            tableMap.put("object", object);
//            finalResult.add(tableMap);
//        }
//        return new QueryAction.Result(finalResult);
//    }

    /**
     * Used by {@link #query(liquibase.Scope)} read the metadata stored in the database. Returns a QueryResult that can be consumed by {@link #rawMetaDataToObject(liquibase.action.Row, liquibase.Scope)} into the value returned on the final QueryResult.
     */
    protected abstract QueryAction.Result getRawMetaData(Scope env) throws DatabaseException;

    /**
     * Used by {@link #query( liquibase.Scope)} to convert each row returned by {@link #getRawMetaData(liquibase.Scope)} into the value returned on the final QueryResult.
     */
    protected abstract DatabaseObject rawMetaDataToObject(Row row, Scope env);


    @Override
    public String describe() {
        return getClass().getSimpleName()+"("+ StringUtils.join(this, ", ", new StringUtils.ToStringFormatter())+")";
    }

    @Override
    public String toString() {
        return describe();
    }

    /**
     * Equals method compares the output of {@link #describe()}
     */
    @Override
    public boolean equals(Object obj) {
        return this.getClass().equals(obj.getClass()) && this.describe().equals(obj.toString());

    }

    @Override
    public int hashCode() {
        return this.describe().hashCode();
    }
}
