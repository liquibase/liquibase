package liquibase.action;

import liquibase.AbstractExtensibleObject;
import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.QueryResult;
import liquibase.executor.Row;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * A base class for {@link liquibase.action.Action}s that fetch metadata about a database.
 * For performance reasons, implementations should be able to handle both requests for a specific record and requests for all objects that match a particular pattern.
 * Standard expectation is that if a search field in a subclass is null, it means "match anything" and if it is set it means "return only objects whose field matches the set value".
 * Before executing a large number of MetaDataQueryActions, the will all be ran through the {@link #merge(QueryAction)} method in an attempt to limit the queries made.
 * Subclasses should use the {@link liquibase.ExtensibleObject} methods to store fields so that the default {@link #merge(QueryAction)} and {@link #describe()} methods work as expected.
 */
public abstract class MetaDataQueryAction extends AbstractExtensibleObject implements QueryAction {

    /**
     * Return a QueryResult with a single column in each row with the key of "object" and value of a {@link liquibase.structure.DatabaseObject} implementation.
     * Subclasses will normally not override this method, but instead override {@link #getRawMetaData(liquibase.ExecutionEnvironment)} and {@link #rawMetaDataToObject(liquibase.executor.Row, liquibase.ExecutionEnvironment)}
     */
    @Override
    public QueryResult query(ExecutionEnvironment env) throws DatabaseException {
        QueryResult queryResult = getRawMetaData(env);

        List<Map<String, Object>> finalResult = new ArrayList<Map<String, Object>>();
        for (Row row : queryResult.toList()) {
            DatabaseObject object = rawMetaDataToObject(row, env);
            Map tableMap = new HashMap();
            tableMap.put("object", object);
            finalResult.add(tableMap);
        }
        return new QueryResult(finalResult);
    }

    /**
     * Used by {@link #query(liquibase.ExecutionEnvironment)} read the metadata stored in the database. Returns a QueryResult that can be consumed by {@link #rawMetaDataToObject(liquibase.executor.Row, liquibase.ExecutionEnvironment)} into the value returned on the final QueryResult.
     */
    protected abstract QueryResult getRawMetaData(ExecutionEnvironment env) throws DatabaseException;

    /**
     * Used by {@link #query( liquibase.ExecutionEnvironment)} to convert each row returned by {@link #getRawMetaData(liquibase.ExecutionEnvironment)} into the value returned on the final QueryResult.
     */
    protected abstract DatabaseObject rawMetaDataToObject(Row row, ExecutionEnvironment env);


    @Override
    public String describe() {
        return getClass().getSimpleName()+"("+ StringUtils.join(this, ", ", new StringUtils.ToStringFormatter())+")";
    }

    @Override
    public String toString() {
        return describe();
    }

    /**
     * Attempt to merge the given QueryAction into this Action. Returns true if the merge was successful, false if not.
     * The merge operation may modify this object, such as changing a "tableName" from a set value to null.
     * For a merge to be successful, updated version of this MetaDataQueryAction must return a QueryResults that contains all objects that would have been found by both original QueryActions.
     * The merged action may return more objects than would have been found in the original QueryActions.
     * When creating a merge function, make sure to consider performance--the merge should not pass if the new QueryAction returns far too much.
     * For example, merging two table metadata queries should probably not succeed if the final QueryAction will query all tables across all schemas.
     * But, it should succeed if the final QueryAction will query all tables within a single schema.
     */
    public boolean merge(QueryAction action) {
        if (action.getClass().equals(this.getClass())) {
            Set<String> attributes = new HashSet<String>(this.getAttributes());
            attributes.addAll(((MetaDataQueryAction) action).getAttributes());

            boolean merged = false;
            boolean allSame = true;
            for (String attribute : attributes) {
                Object thisValue = this.getAttribute(attribute, Object.class);
                Object otherValue = ((MetaDataQueryAction) action).getAttribute(attribute, Object.class);

                Object finalValue;
                if (thisValue == null || otherValue == null) {
                    finalValue = null;
                } else if (thisValue.equals(otherValue)) {
                    finalValue = thisValue;
                } else {
                    finalValue = null;
                }

                if (allSame && finalValue != null && !finalValue.equals(thisValue)) {
                    allSame = false;
                }

                if (!merged) {
                    if (finalValue == null && (thisValue != null || otherValue != null)) {
                        merged = true;
                    }
                }
                if (merged) {
                    this.setAttribute(attribute, finalValue);
                }
            }
            if (merged) {
                return true;
            } else {
                if (allSame) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
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
