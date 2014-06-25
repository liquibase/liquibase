package liquibase.action;

import liquibase.exception.DatabaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.QueryResult;
import liquibase.executor.Row;
import liquibase.structure.DatabaseObject;

import java.util.Map;

public class MockMetaDataAction extends MetaDataQueryAction {

    public MockMetaDataAction(Map<String, Object> attributes) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            setAttribute(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected DatabaseObject rawMetaDataToObject(Row row, ExecutionEnvironment env) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    protected QueryResult getRawMetaData(ExecutionEnvironment env) throws DatabaseException {
        throw new RuntimeException("Not implemented");
    }
}
