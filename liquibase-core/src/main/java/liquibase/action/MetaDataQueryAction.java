package liquibase.action;

import liquibase.AbstractExtensibleObject;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public abstract class MetaDataQueryAction extends AbstractExtensibleObject implements QueryAction {

    @Override
    public QueryResult query(ExecutionOptions options) throws DatabaseException {
        QueryResult queryResult = executeQuery(options);

        List<Map<String, Object>> finalResult = new ArrayList<Map<String, Object>>();
        for (Map<String, ?> row : queryResult.toList()) {
            DatabaseObject object = createObject(row);
            Map tableMap = new HashMap();
            tableMap.put("object", object);
            finalResult.add(tableMap);
        }
        return new QueryResult(finalResult);
    }

    protected abstract DatabaseObject createObject(Map<String, ?> row);

    protected abstract QueryResult executeQuery(ExecutionOptions options) throws DatabaseException;

    @Override
    public String toString(ExecutionOptions options) {
        return toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"("+ StringUtils.join(getAttributeMap(), ", ", new StringUtils.ToStringFormatter())+")";
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    public QueryAction merge(QueryAction action) {
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
                return this;
            } else {
                if (allSame) {
                    return this;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }

        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
