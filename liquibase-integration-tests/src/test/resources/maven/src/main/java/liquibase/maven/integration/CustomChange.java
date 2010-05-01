package liquibase.maven.integration;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;

/**
 *
 * @author lujop
 */
public class CustomChange implements CustomSqlChange{

    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        SqlStatement st[]=new SqlStatement[1];
        InsertStatement is=new InsertStatement(null,"persons");
        is.addColumnValue("id",new Integer(1));
        is.addColumnValue("firstname", "joan");
        is.addColumnValue("lastname", "pujol");
        st[0]=is;
        return st;
    }

    public String getConfirmationMessage() {
       return "executed";
    }

    public void setUp() throws SetupException {

    }

    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

}
