package liquibase.change.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.core.string.StringChangeLogSerializer;

import liquibase.test.JUnitResourceAccessor;
import org.junit.Assert;
import org.junit.Test;

public class UpdateDataChangeTest {

    @Test
    public void testChecksumCalculation() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "\n" + 
                "<databaseChangeLog\n" + 
                "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" + 
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
                "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd\">\n" +
                "\n" + 
                "    <changeSet id=\"1\" author=\"a\" logicalFilePath=\"a\">\n" + 
                "        <update tableName=\"test\">\n" + 
                "            <column name=\"name\" value=\"changed\"/>\n" +
                "            <where>id = 1</where>\n" +
                "        </update>\n" +
                "    </changeSet>\n" +
                "</databaseChangeLog>\n";

        ResourceAccessor resourceAccessor = new CompositeResourceAccessor(getResource(xml), new JUnitResourceAccessor());
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("xml", resourceAccessor);
        DatabaseChangeLog changelog = parser.parse(null, new ChangeLogParameters(), resourceAccessor);
        ChangeSet changeSet = changelog.getChangeSet("a", "a", "1");

        CheckSum checksum = changeSet.generateCheckSum();
        Assert.assertEquals("7:f5d90faf3e96175decc8395de001a5bb", checksum.toString()); // version 3.1.x

        String changelogString = new StringChangeLogSerializer().serialize(changeSet.getChanges().get(0), false);
        Assert.assertTrue(changelogString.contains("where=")); // version 3.1.x
    }

    private ResourceAccessor getResource(final String xml) {
        return new ResourceAccessor() {
            @Override
            public ClassLoader toClassLoader() {
                return UpdateDataChange.class.getClassLoader();
            }
            @Override
            public Enumeration<URL> getResources(String packageName) throws IOException {
                return null;
            }
            @Override
            public InputStream getResourceAsStream(String file) throws IOException {
                if (file == null) {
                    byte[] buf = xml.getBytes("UTF-8");
                    return new ByteArrayInputStream(buf);
                } else {
                    return null;
                }
            }
        };
    }
}
