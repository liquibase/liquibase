package liquibase.parser.core.yaml;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.resource.ResourceAccessor;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class YamlChangeLogParserTest {

    private String changeLogText = "databaseChangeLog:\n" +
            "  - changeSet:\n" +
            "      id: test1\n" +
            "      author: nvoxland\n" +
            "      runOnChange: true\n" +
            "      changes:\n" +
            "      - createTable:\n" +
            "          tableName: testTable\n" +
            "          columns:\n" +
            "          - column:\n" +
            "              name: id\n" +
            "              type: int\n" +
            "          - column:\n" +
            "              name: name\n" +
            "              type: varchar(255)\n";

    @Test
    public void parse() throws ChangeLogParseException {
        DatabaseChangeLog changeLog = new YamlChangeLogParser().parse("test.yaml", new ChangeLogParameters(), new ResourceAccessor() {
            @Override
            public InputStream getResourceAsStream(String file) throws IOException {
                return new ByteArrayInputStream(changeLogText.getBytes());
            }

            @Override
            public Enumeration<URL> getResources(String packageName) throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ClassLoader toClassLoader() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        System.out.println(changeLog);


    }
}
