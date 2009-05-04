package liquibase.changelog.visitor;

import liquibase.ChangeSet;
import liquibase.changelog.visitor.RollbackVisitor;
import liquibase.database.Database;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Test;

public class RollbackVisitorTest {
    @Test
    public void visit() throws Exception {
        Database database = createMock(Database.class);

        ChangeSet changeSet = createMock(ChangeSet.class);
        changeSet.rolback(database);
        expectLastCall();


        database.removeRanStatus(changeSet);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);

        RollbackVisitor visitor = new RollbackVisitor(database);
        visitor.visit(changeSet, database);

        verify(database);
        verify(changeSet);
    }
}
