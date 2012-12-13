package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Test;

public class RollbackVisitorTest {
    @Test
    public void visit() throws Exception {
        Database database = createMock(Database.class);

        ChangeSet changeSet = createMock(ChangeSet.class);
        expect(changeSet.toString(false)).andReturn("/my/db/changelog.xml::addtables-24::devgal71");
        changeSet.rollback(database);
        expectLastCall();


        database.removeRanStatus(changeSet);
        expectLastCall();

        database.commit();
        expectLastCall();


        replay(changeSet);
        replay(database);

        RollbackVisitor visitor = new RollbackVisitor(database);
        visitor.visit(changeSet, new DatabaseChangeLog(), database);

        verify(database);
        verify(changeSet);
    }
}
