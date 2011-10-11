package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.DB2Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

@ChangeClass(name = "createProcedure", description = "Create Procedure", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateProcedureChange extends AbstractChange {
    private String comments;
    private String procedureBody;

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
        } else if (database instanceof DB2Database) {
            endDelimiter = "";
        }

        return new SqlStatement[]{
                new RawSqlStatement(getProcedureBody(), endDelimiter),
        };
    }

    public String getConfirmationMessage() {
        return "Stored procedure created";
    }
}
