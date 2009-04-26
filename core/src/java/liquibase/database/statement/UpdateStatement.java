package liquibase.database.statement;

import liquibase.database.Database;

import java.util.*;

public class UpdateStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private Map<String, Object> newColumnValues = new HashMap<String, Object>();
    private String whereClause;
    private List<Object> whereParameters = new ArrayList<Object>();


    public UpdateStatement(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public UpdateStatement addNewColumnValue(String columnName, Object newValue) {
        newColumnValues.put(columnName, newValue);

        return this;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public UpdateStatement setWhereClause(String whereClause) {
        this.whereClause = whereClause;

        return this;
    }

    public void addWhereParameter(Object value) {
        this.whereParameters.add(value);
    }

    public Map<String, Object> getNewColumnValues() {
        return newColumnValues;
    }

    public List<Object> getWhereParameters() {
        return whereParameters;
    }
}
