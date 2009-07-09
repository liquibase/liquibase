package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.executor.ExecutorService;
import liquibase.executor.WriteExecutor;
import liquibase.exception.DatabaseException;
import liquibase.statement.core.UpdateStatement;
import liquibase.change.CheckSum;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShouldRunChangeSetFilterTest  {

    @Test
    public void accepts_noneRun() throws DatabaseException {
        Database database = createMock(Database.class);
        expect(database.getRanChangeSetList()).andReturn(new ArrayList<RanChangeSet>());
        replay(database);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database);

        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",null,  null, null)));
    }

    @Test
    public void accepts() throws DatabaseException {
        ArrayList<RanChangeSet> ranChanges = new ArrayList<RanChangeSet>();
        ranChanges.add(new RanChangeSet("path/changelog", "1", "testAuthor", CheckSum.parse("12345"), new Date(), null));
        ranChanges.add(new RanChangeSet("path/changelog", "2", "testAuthor", CheckSum.parse("12345"), new Date(), null));

        Database database = createMock(Database.class);
        expect(database.getRanChangeSetList()).andReturn(ranChanges);
        expect(database.getDatabaseChangeLogTableName()).andReturn("DATABASECHANGELOG").anyTimes();
        expect(database.getDefaultSchemaName()).andReturn(null).anyTimes();

        WriteExecutor template = createMock(WriteExecutor.class);
        expect(template.update(isA(UpdateStatement.class), isA(List.class))).andReturn(1).anyTimes();
//        template.comment("Lock Database");
//        expectLastCall();

        replay(database);
        replay(template);
        ExecutorService.getInstance().setWriteExecutor(database, template);

        ShouldRunChangeSetFilter filter = new ShouldRunChangeSetFilter(database);

        //everything same
        assertFalse(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "path/changelog",null,  null, null)));

        //alwaysRun
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", true, false, "path/changelog", null, null, null)));

        //run on change
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, true, "path/changelog", null, null, null)));

        //different id
        assertTrue(filter.accepts(new ChangeSet("3", "testAuthor", false, false, "path/changelog",null,  null, null)));

        //different author
        assertTrue(filter.accepts(new ChangeSet("1", "otherAuthor", false, false, "path/changelog", null, null, null)));

        //different path
        assertTrue(filter.accepts(new ChangeSet("1", "testAuthor", false, false, "other/changelog", null, null, null)));
    }
}
