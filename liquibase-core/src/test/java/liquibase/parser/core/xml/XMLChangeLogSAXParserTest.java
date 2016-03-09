package liquibase.parser.core.xml;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.test.JUnitResourceAccessor;

public class XMLChangeLogSAXParserTest {

    @Test
    public void testIgnoreDuplicateChangeSets() throws ChangeLogParseException, Exception {
        XMLChangeLogSAXParser xmlParser = new XMLChangeLogSAXParser();
        DatabaseChangeLog changeLog = xmlParser.parse("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/master.changelog.xml", 
            new ChangeLogParameters(), new JUnitResourceAccessor());

        List<ChangeSet> changeSets = changeLog.getChangeSets();
        Assert.assertEquals(8, changeSets.size());
        
        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser", 
            changeSets.get(0).toString());
        
        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser", 
            changeSets.get(1).toString());
        Assert.assertEquals(1, changeSets.get(1).getContexts().getContexts().size());

        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser", 
            changeSets.get(2).toString());
        Assert.assertEquals(1, changeSets.get(2).getLabels().getLabels().size());

        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser", 
            changeSets.get(3).toString());
        Assert.assertEquals(2, changeSets.get(3).getLabels().getLabels().size());

        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser", 
            changeSets.get(4).toString());
        Assert.assertEquals(1, changeSets.get(4).getDbmsSet().size());

        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog1.xml::1::testuser", 
            changeSets.get(5).toString());
        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog3.xml::1::testuser", 
            changeSets.get(6).toString());
        Assert.assertEquals("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog2.xml::1::testuser", 
            changeSets.get(7).toString());
    }

}