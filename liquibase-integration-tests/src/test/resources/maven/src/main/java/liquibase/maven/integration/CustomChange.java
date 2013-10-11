package liquibase.maven.integration;

import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomSqlRollback;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InsertStatement;

/**
 *
 * @author lujop
 */
public class CustomChange implements CustomSqlChange,CustomSqlRollback{

    @Override
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        SqlStatement st[]=new SqlStatement[1];
        InsertStatement is=new InsertStatement(null, null,"persons");
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
    public SqlStatement[] generateRollbackStatements(Database database) throws CustomChangeException, RollbackImpossibleException {
        SqlStatement st[]=new SqlStatement[1];
        DeleteStatement ds=new DeleteStatement(null, null,"persons");
        ds.setWhereClause("id=1");
        st[0]=ds;
        return st;
    }



}
