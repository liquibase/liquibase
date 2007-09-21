package liquibase.database.sql;

import liquibase.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoredProcedureStatement implements SqlStatement {

    private String procedureName;
    private List<String> parameters = new ArrayList<String>();


    public String getProcedureName() {
        return procedureName;
    }


    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addParameter(String param) {
        parameters.add(param);
    }



    public String getSqlStatement(Database database) {
        StringBuffer string = new StringBuffer();
        string.append("exec (").append(procedureName);
        for (String param : getParameters()) {
            string.append(" ").append(param).append(",");
        }
        return string.toString().replaceFirst(",$",")");
    }
}
