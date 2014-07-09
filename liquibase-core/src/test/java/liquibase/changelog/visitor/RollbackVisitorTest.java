package liquibase.changelog.visitor;

import liquibase.ExecutionEnvironment;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import org.junit.Test;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;

public class RollbackVisitorTest {
    @Test
    public void visit() throws Exception {
        Database database = createMock(Database.class);

        ChangeSet changeSet = createMock(ChangeSet.class);
        changeSet.rollback(database);
        expectLastCall();


        database.removeRanStatus(changeSet);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);

        RollbackVisitor visitor = new RollbackVisitor(database);
        visitor.visit(changeSet, new DatabaseChangeLog(), new ExecutionEnvironment(database), null);

        verify(database);
        verify(changeSet);
    }
}
