package liquibase.database.sql;

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


    public boolean supportsDatabase(Database database) {
        return true;
    }

    public Map<String, Object> getNewColumnValues() {
        return newColumnValues;
    }

    public String getSqlStatement(Database database) {
        StringBuffer sql = new StringBuffer("UPDATE "+database.escapeTableName(getSchemaName(), getTableName())+" SET");
        for (String column : newColumnValues.keySet()) {
            sql.append(" ").append(database.escapeColumnName(getSchemaName(), getTableName(), column)).append(" = ");
            sql.append(convertToString(newColumnValues.get(column), database));
            sql.append(",");
        }

        sql.deleteCharAt(sql.lastIndexOf(","));
        if (whereClause != null) {
            String fixedWhereClause = "WHERE "+whereClause;
            for (Object param : whereParameters) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?", convertToString(param, database));
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return sql.toString();
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
            sqlString = "NULL";
        } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
            sqlString = "'" + newValue + "'";
        } else if (newValue instanceof Date) {
            sqlString = database.getDateLiteral(((Date) newValue));
        } else if (newValue instanceof Boolean) {
            if (((Boolean) newValue)) {
                sqlString = database.getTrueBooleanValue();
            } else {
                sqlString = database.getFalseBooleanValue();
            }
        } else {
            sqlString = newValue.toString();
        }
        return sqlString;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }
}
