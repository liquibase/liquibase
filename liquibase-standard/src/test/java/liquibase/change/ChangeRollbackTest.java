package liquibase.change;

import org.junit.Assert;
import org.junit.Test;

import liquibase.change.AddColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.OutputChange;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MockDatabase;
import liquibase.exception.RollbackImpossibleException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;

public class ChangeRollbackTest {

    @Test
    public void generateRollbackStatements_for_empty_change_must_succeed() throws RollbackImpossibleException {
        EmptyChange change = new EmptyChange();
        SqlStatement[] statements = change.generateRollbackStatements(new MockDatabase());
        Assert.assertEquals(0, statements.length);
    }

    @Test
    public void generateRollbackStatements_for_output_change_must_succeed() throws RollbackImpossibleException {
        OutputChange change = new OutputChange();
        change.setMessage("Irrelevant for the test");
        SqlStatement[] statements = change.generateRollbackStatements(new MockDatabase());
        Assert.assertEquals(0, statements.length);
    }

}
