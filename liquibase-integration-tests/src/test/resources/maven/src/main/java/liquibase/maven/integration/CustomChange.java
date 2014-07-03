package liquibase.maven.integration;

import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomSqlRollback;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.DeleteDataStatement;
import liquibase.statement.core.InsertDataStatement;

/**
 *
 * @author lujop
 */
public class CustomChange implements CustomSqlChange,CustomSqlRollback{

    @Override
    public Statement[] generateStatements(Database database) throws CustomChangeException {
        Statement st[]=new Statement[1];
        InsertDataStatement is=new InsertDataStatement(null, null,"persons");
        is.addColumnValue("id",new Integer(1));
        is.addColumnValue("firstname", "joan");
        is.addColumnValue("lastname", "pujol");
        st[0]=is;
        return st;
    }

    @Override
    public String getConfirmationMessage() {
       return "executed";
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {

    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public Statement[] generateRollbackStatements(Database database) throws CustomChangeException, RollbackImpossibleException {
        Statement st[]=new Statement[1];
        DeleteDataStatement ds=new DeleteDataStatement(null, null,"persons");
        ds.setWhereClause("id=1");
        st[0]=ds;
        return st;
    }



}
