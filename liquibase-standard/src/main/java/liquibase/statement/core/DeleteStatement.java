package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String where;
    private List<Object> whereParameters = new ArrayList<>();
    private List<String> whereColumnNames = new ArrayList<>();

    public DeleteStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName  = catalogName;
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

    public String getWhere() {
        return where;
    }

    public DeleteStatement setWhere(String where) {
        this.where = where;
        return this;
    }

    /**
     * @deprecated use {@link #getWhere()}
     */
    public String getWhereClause() {
        return getWhere();
    }

    /**
     * @deprecated use {@link #setWhere(String)}
     */
    public DeleteStatement setWhereClause(String whereClause) {
        return setWhere(whereClause);
    }

    public DeleteStatement addWhereParameter(Object value) {
        this.whereParameters.add(value);
        return this;
    }

    public DeleteStatement addWhereParameters(Object... value) {
        this.whereParameters.addAll(Arrays.asList(value));
        return this;
    }

    public DeleteStatement addWhereColumnName(String value) {
        this.whereColumnNames.add(value);
        return this;
    }

    public List<Object> getWhereParameters() {
        return whereParameters;
    }

    public List<String> getWhereColumnNames() {
        return whereColumnNames;
    }
}
