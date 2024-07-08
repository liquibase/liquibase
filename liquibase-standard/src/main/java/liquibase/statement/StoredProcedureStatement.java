package liquibase.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoredProcedureStatement extends AbstractSqlStatement implements CallableSqlStatement {

    private final String procedureName;
    private final List<String> parameters = new ArrayList<>();
    private final List<Integer> types = new ArrayList<>();


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

    public int getParameterType(String param) {
        for  (int i=0; i<parameters.size(); i++) {
            if (parameters.get(i).equals(param)) {
                return types.get(i);
            }
        }
        return -1;
    }

}
