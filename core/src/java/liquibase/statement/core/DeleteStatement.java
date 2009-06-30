package liquibase.statement.core;

import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class DeleteStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String whereClause;
    private List<Object> whereParameters = new ArrayList<Object>();


    public DeleteStatement(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public DeleteStatement setWhereClause(String whereClause) {
        this.whereClause = whereClause;

        return this;
    }

    public DeleteStatement addWhereParameter(Object value) {
        this.whereParameters.add(value);

        return this;
    }

    public DeleteStatement addWhereParameters(Object... value) {
        this.whereParameters.addAll(Arrays.asList(value));

        return this;
    }

    public List<Object> getWhereParameters() {
        return whereParameters;
    }
}
