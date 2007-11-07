package liquibase.change;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
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

    public Element createNode(Document currentChangeLogFileDOM) {
        Element sqlElement = currentChangeLogFileDOM.createElement(getTagName());
        sqlElement.appendChild(currentChangeLogFileDOM.createTextNode(getProcedureBody()));

        return sqlElement;
    }


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
