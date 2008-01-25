package liquibase.database.sql;

import liquibase.database.Database;

import java.util.*;

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

    public void addWhereParameter(Object value) {
        this.whereParameters.add(value);
    }


    public boolean supportsDatabase(Database database) {
        return true;
    }

    public String getSqlStatement(Database database) {
        StringBuffer sql = new StringBuffer("DELETE FROM "+database.escapeTableName(getSchemaName(), getTableName()));

        if (whereClause != null) {
            String fixedWhereClause = " WHERE "+whereClause;
            for (Object param : whereParameters) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?", convertToString(param, database));
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return sql.toString();
    }

    private String convertToString(Object newValue, Database database) {
        String sqlString;
        if (newValue == null) {
            sqlString = "NULL";
        } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
            sqlString = "'"+newValue+"'";
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
