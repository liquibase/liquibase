package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.*;

public class UpdateStatement extends AbstractStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private SortedMap<String, Object> newColumnValues = new TreeMap<String, Object>();
    private String whereClause;

    private List<String> whereColumnNames = new ArrayList<String>();
    private List<Object> whereParameters = new ArrayList<Object>();
    private boolean needsPreparedStatement;


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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }

    public void setNeedsPreparedStatement(boolean needsPreparedStatement) {
        this.needsPreparedStatement = needsPreparedStatement;
    }

    public boolean getNeedsPreparedStatement() {
        return needsPreparedStatement;
    }
}
