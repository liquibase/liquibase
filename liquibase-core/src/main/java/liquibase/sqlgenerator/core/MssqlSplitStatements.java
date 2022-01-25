package liquibase.sqlgenerator.core;

import java.util.List;

public class MssqlSplitStatements {
    private List<String> setStatementsBefore;
    private String body;
    private List<String> setStatementsAfter;

    public List<String> getSetStatementsBefore() {
        return setStatementsBefore;
    }

    public void setSetStatementsBefore(List<String> setStatementsBefore) {
        this.setStatementsBefore = setStatementsBefore;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getSetStatementsAfter() {
        return setStatementsAfter;
    }

    public void setSetStatementsAfter(List<String> setStatementsAfter) {
        this.setStatementsAfter = setStatementsAfter;
    }
}
