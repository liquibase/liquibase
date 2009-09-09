package liquibase.ext;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import static org.junit.Assert.*;

import org.junit.Test;

import java.util.HashMap;

public class ExtXMLChangeLogSAXParserTest {


    @Test
    public void extChangeLog() throws Exception {
        ServiceLocator.reset();
        ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        ServiceLocator.getInstance().setResourceAccessor(resourceAccessor);
        ChangeFactory.reset();

        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("changelogs/common/ext.changelog.xml", new HashMap<String, Object>(), resourceAccessor);

        assertEquals("changelogs/common/ext.changelog.xml", changeLog.getLogicalFilePath());

        assertEquals(4, changeLog.getChangeSets().size());

        ChangeSet changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        Change change = changeSet.getChanges().get(0);
        assertEquals("sampleChange", change.getChangeMetaData().getName());

        changeSet = changeLog.getChangeSets().get(2);
        change = changeSet.getChanges().get(0);
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("changeWithNestedTags", change.getChangeMetaData().getName());

        Object child1 = change.getClass().getMethod("getChild").invoke(change);
        assertNotNull(child1);
        assertEquals("standard", child1.getClass().getMethod("getName").invoke(child1));

        Object child2 = change.getClass().getMethod("getChild2").invoke(change);
        assertNotNull(child2);
        assertEquals("second", child1.getClass().getMethod("getName").invoke(child2));

        ServiceLocator.reset();
        ChangeFactory.reset();
    }
}
