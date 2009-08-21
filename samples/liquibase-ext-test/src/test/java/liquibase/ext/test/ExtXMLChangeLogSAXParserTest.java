package liquibase.ext.test;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RawSQLChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.change.custom.ExampleCustomSqlChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.test.JUnitResourceAccessor;
import liquibase.test.ExtensionResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.logging.Logger;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

public class ExtXMLChangeLogSAXParserTest {


    @Test
    public void extChangeLog() throws Exception {
        ServiceLocator.reset();
        ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        ServiceLocator.getInstance().setResourceAccessor(resourceAccessor);
        ChangeFactory.reset();

        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse("liquibase/ext/test/ext.changelog.xml", new HashMap<String, Object>(), resourceAccessor);

        assertEquals("liquibase/ext/test/ext.changelog.xml", changeLog.getLogicalFilePath());

        assertEquals(4, changeLog.getChangeSets().size());

        ChangeSet changeSet = changeLog.getChangeSets().get(1);
        assertEquals("nvoxland", changeSet.getAuthor());
        assertEquals("2", changeSet.getId());
        assertEquals(1, changeSet.getChanges().size());
        Change change = changeSet.getChanges().get(0);
        assertEquals("sample2", change.getChangeMetaData().getName());

        changeSet = changeLog.getChangeSets().get(2);
        change = changeSet.getChanges().get(0);
        assertEquals(1, changeSet.getChanges().size());
        assertEquals("sample3", change.getChangeMetaData().getName());

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
