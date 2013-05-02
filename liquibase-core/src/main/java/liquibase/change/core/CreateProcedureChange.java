package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.DB2Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

@DatabaseChange(name = "createProcedure",
        description = "Defines the definition for a stored procedure. This command is better to use for creating procedures than the raw sql command because it will not attempt to strip comments or break up lines.\n\nOften times it is best to use the CREATE OR REPLACE syntax along with setting runOnChange='true' on the enclosing changeSet tag. That way if you need to make a change to your procedure you can simply change your existing code rather than creating a new REPLACE PROCEDURE call. The advantage to this approach is that it keeps your change log smaller and allows you to more easily see what has changed in your procedure code through your source control system's diff command.",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateProcedureChange extends AbstractChange {
    private String comments;
    private String procedureBody;

    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE,
    exampleValue = "CREATE OR REPLACE PROCEDURE testHello\n" +
            "    IS\n" +
            "    BEGIN\n" +
            "      DBMS_OUTPUT.PUT_LINE('Hello From The Database!');\n" +
            "    END;")
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
