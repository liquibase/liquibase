package liquibase.changelog.visitor;

import liquibase.ChangeSet;
import liquibase.database.Database;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Test;

public class UpdateVisitorTest {

    @Test
    public void visit_unrun() throws Exception {
        Database database = createMock(Database.class);

        ChangeSet changeSet = createMock(ChangeSet.class);
        expect(changeSet.execute(database)).andReturn(true);


        expect(database.getRunStatus(changeSet)).andReturn(ChangeSet.RunStatus.NOT_RAN);


        database.markChangeSetAsRan(changeSet);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);

        UpdateVisitor visitor = new UpdateVisitor(database);
        visitor.visit(changeSet, database);

        verify(database);
        verify(changeSet);
    }

    @Test
    public void visit_rerun() throws Exception {
        ChangeSet changeSet = createMock(ChangeSet.class);
        Database database = createMock(Database.class);

        expect(changeSet.execute(database)).andReturn(true);

        expect(database.getRunStatus(changeSet)).andReturn(ChangeSet.RunStatus.ALREADY_RAN);

        database.markChangeSetAsReRan(changeSet);
        expectLastCall();

        database.commit();
        expectLastCall();
        
        replay(changeSet);
        replay(database);

        UpdateVisitor visitor = new UpdateVisitor(database);
        visitor.visit(changeSet, database);

        verify(database);
        verify(changeSet);
    }
}
