package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import static org.easymock.classextension.EasyMock.*;

import liquibase.database.ObjectQuotingStrategy;
import org.junit.Test;

public class UpdateVisitorTest {

    @Test
    public void visit_unrun() throws Exception {
        Database database = createMock(Database.class);
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);

        ChangeSet changeSet = createMock(ChangeSet.class);
        expect(changeSet.execute(new DatabaseChangeLog("test.xml"), database)).andReturn(ChangeSet.ExecType.EXECUTED);


        expect(database.getRunStatus(changeSet)).andReturn(ChangeSet.RunStatus.NOT_RAN);


        database.markChangeSetExecStatus(changeSet, ChangeSet.ExecType.EXECUTED);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);

        UpdateVisitor visitor = new UpdateVisitor(database);
        visitor.visit(changeSet, new DatabaseChangeLog("test.xml"), database);

        verify(database);
        verify(changeSet);
    }

}
