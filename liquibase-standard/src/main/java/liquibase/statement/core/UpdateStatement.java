package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.*;

public class UpdateStatement extends AbstractSqlStatement {
    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final SortedMap<String, Object> newColumnValues = new TreeMap<>();
    private String whereClause;

    private final List<String> whereColumnNames = new ArrayList<>();
    private final List<Object> whereParameters = new ArrayList<>();


    public UpdateStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getCatalogName() {
        return catalogName;
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

    public UpdateStatement addWhereParameter(Object value) {
        this.whereParameters.add(value);

        return this;
    }

    public UpdateStatement addWhereParameters(Object... value) {
        this.whereParameters.addAll(Arrays.asList(value));

        return this;
    }

    public UpdateStatement addWhereColumnName(String value) {
        this.whereColumnNames.add(value);

        return this;
    }


    public Map<String, Object> getNewColumnValues() {
        return newColumnValues;
    }

    public List<Object> getWhereParameters() {
        return whereParameters;
    }

    public List<String> getWhereColumnNames() {
        return whereColumnNames;
    }
}
