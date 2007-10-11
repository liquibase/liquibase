package liquibase.database.sql;

import liquibase.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateStatement implements SqlStatement, PreparedSqlStatement {
    private String tableName;
    private Map<String, Object> newColumnValues = new HashMap<String, Object>();
    private Map<String, Integer> newColumnTypes = new HashMap<String, Integer>();
    private String whereClause;
    private List<Object> whereParameters = new ArrayList<Object>();
    private List<Integer> whereParameterTypes = new ArrayList<Integer>();


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addNewColumnValue(String columnName, Object newValue, int type) {
        newColumnValues.put(columnName, newValue);
        newColumnTypes.put(columnName, type);

    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public void addWhereParameter(Object value, int type) {
        this.whereParameters.add(value);
        this.whereParameterTypes.add(type);
    }


    public String getSqlStatement(Database database) {
        StringBuffer sql = new StringBuffer("UPDATE "+tableName+" SET");
        for (String column : newColumnValues.keySet()) {
            sql.append(" ").append(column).append(" = ");
            Object newValue = newColumnValues.get(column);
            if (newValue == null) {
                sql.append("NULL");
            } else if (newValue instanceof String && database.shouldQuoteValue(((String) newValue))) {
                sql.append("'").append(newValue).append("'");
            } else {
                sql.append(newValue);
            }
            sql.append(",");
        }

        sql.deleteCharAt(sql.lastIndexOf(","));
        if (whereClause != null) {
            String fixedWhereClause = "WHERE "+whereClause;
            for (Object param : whereParameters) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?", "'"+param+"'");
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return sql.toString();
    }


    public PreparedStatement createPreparedStatement(Database database) throws SQLException {
        StringBuffer sql = new StringBuffer("UPDATE "+tableName+" SET");
        List<Object> params = new ArrayList<Object>();
        List<Integer> types = new ArrayList<Integer>();
        for (String column : newColumnValues.keySet()) {
            sql.append(" ").append(column).append(" = ?,");
            params.add(newColumnValues.get(column));
            types.add(newColumnTypes.get(column));
        }

        sql.deleteCharAt(sql.lastIndexOf(","));

        if (whereClause != null) {
            sql.append(" where ").append(whereClause);
        }

        PreparedStatement pstmt = database.getConnection().prepareStatement(sql.toString());

        params.addAll(whereParameters);
        types.addAll(whereParameterTypes);

        for (int i=0; i<params.size(); i++) {
            Object param = params.get(i);
            int type = database.getDatabaseType(types.get(i));

            if (param == null) {
                pstmt.setNull(i+1, type);
            } else {
                try {
                    pstmt.setObject(i+1, param, type);
                } catch (SQLException e) {
                    throw e;
                }
            }
        }

        return pstmt;
    }


    public String getEndDelimiter(Database database) {
        return ";";
    }
}
