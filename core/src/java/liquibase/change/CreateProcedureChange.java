package liquibase.change;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

public class CreateProcedureChange extends AbstractChange {
    private String comments;
    private String procedureBody;

    public CreateProcedureChange() {
        super("createProcedure", "Create Procedure");
    }

    public String getProcedureBody() {
        return procedureBody;
    }

    public void setProcedureBody(String procedureBody) {
        this.procedureBody = procedureBody;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public SqlStatement[] generateStatements(Database database) {
        String endDelimiter = ";";
        if (database instanceof OracleDatabase) {
            endDelimiter = "\n/";
        }
        return new SqlStatement[] {
                new RawSqlStatement(getProcedureBody(), endDelimiter),
        };
    }

    public String getConfirmationMessage() {
        return "Stored procedure created";
    }
}
