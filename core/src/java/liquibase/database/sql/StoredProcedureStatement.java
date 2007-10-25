package liquibase.database.sql;

import liquibase.database.Database;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoredProcedureStatement implements SqlStatement, CallableSqlStatement {

    private String procedureName;
    private List<String> parameters = new ArrayList<String>();
    private List<Integer> types = new ArrayList<Integer>();


    public StoredProcedureStatement(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getProcedureName() {
        return procedureName;
    }


    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addParameter(String param, int type) {
        parameters.add(param);
        types.add(type);
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }

    public String getSqlStatement(Database database) {
        StringBuffer string = new StringBuffer();
        string.append("exec (").append(procedureName);
        for (String param : getParameters()) {
            string.append(" ").append(param).append(",");
        }
        return string.toString().replaceFirst(",$", ")");
    }


    public CallableStatement createCallableStatement(Database database) throws SQLException {
        StringBuffer sql = new StringBuffer("{call " + getProcedureName());

        if (parameters.size() > 0) {
            sql.append("(");
            //noinspection UnusedDeclaration
            for (Object param : parameters) {
                sql.append("?,");
            }
            sql.deleteCharAt(sql.lastIndexOf(","));
            sql.append(")");
        }

        sql.append("}");

        CallableStatement pstmt = database.getConnection().prepareCall(sql.toString());

        for (int i = 0; i < parameters.size(); i++) {
            Object param = parameters.get(i);
            int type = database.getDatabaseType(types.get(i));

            if (param == null) {
                pstmt.setNull(i + 1, type);
            } else {
                try {
                    pstmt.setObject(i + 1, param, type);
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
