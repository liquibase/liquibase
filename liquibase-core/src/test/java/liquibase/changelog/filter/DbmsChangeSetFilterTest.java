package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.database.core.MySQLDatabase;
import static org.junit.Assert.*;
import org.junit.Test;

public class DbmsChangeSetFilterTest  {

//    @Test
//    public void emptyDbms() {
//        DbmsChangeSetFilter filter = new DbmsChangeSetFilter();
//
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "mysql")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "oracle")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "oracle, mysql")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null)));
//    }

    @Test
    public void singleDbms() {
        DbmsChangeSetFilter filter = new DbmsChangeSetFilter(new MySQLDatabase());

        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null, "mysql")));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null,null, "mysql, oracle")));
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null,null, "oracle")));
        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null)));
        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null,null, "h2,!mysql")));
    }

//    @Test
//    public void multiContexts() {
//        DbmsChangeSetFilter filter = new DbmsChangeSetFilter("mysql", "oracle");
//
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "mysql")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "oracle")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "oracle, mysql")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, "db2, oracle")));
//        assertFalse(filter.accepts(new ChangeSet(null, null, false, false, null, null, "db2")));
//        assertTrue(filter.accepts(new ChangeSet(null, null, false, false, null, null, null)));
//    }

}
