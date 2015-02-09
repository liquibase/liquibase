package liquibase.actionlogic;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.ObjectUtil;

import java.sql.ResultSet;
import java.util.*;

public class ObjectBasedQueryResult extends QueryResult {

    private List resultSet;

    public ObjectBasedQueryResult(Object result) {
        this(result, null);
    }

    public ObjectBasedQueryResult(Object result, String message) {
        super(message);
        if (result == null) {
            this.resultSet = Collections.unmodifiableList(new ArrayList());
            return;
        }

        if (result instanceof ResultSet) {
            throw new UnexpectedLiquibaseException("Cannot pass ResultSet directly into ObjectBasedQueryResult. Use JdbcUtils.extract() to create a disconnected collection to pass in.");
        }

        if (!(result instanceof Collection)) {
            result = Arrays.asList(result);
        }

        this.resultSet = Collections.unmodifiableList((List) result);
    }

    @Override
    public <T> T asObject(Class<T> requiredType) throws IllegalArgumentException {
        return ObjectUtil.convert(getSingleEntry(), requiredType);
    }

    @Override
    public <T> T asObject(T defaultValue) throws IllegalArgumentException {
        T obj = (T) asObject(defaultValue.getClass());
        if (obj == null) {
            return defaultValue;
        }
        return obj;
    }

    @Override
    public <T> List<T> asList(Class<T> elementType) throws IllegalArgumentException {
        List returnList = new ArrayList();
        for (Object obj : resultSet) {
            returnList.add(ObjectUtil.convert(obj, elementType));
        }
        return Collections.unmodifiableList(returnList);

    }

    protected Object getSingleEntry() throws IllegalArgumentException {
        if (resultSet.size() == 0) {
            return null;
        }
        if (resultSet.size() > 1) {
            throw new IllegalArgumentException("Results contained " + resultSet.size() + " rows");
        }
        return resultSet.get(0);
    }

    @Override
    public int size() {
        return resultSet.size();
    }
}
