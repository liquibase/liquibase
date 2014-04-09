package liquibase.change.core

import liquibase.change.ChangeFactory;
import liquibase.change.CheckSum
import liquibase.change.StandardChangeTest;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class UpdateDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new UpdateDataChange()
        change.setTableName("TABLE_NAME");

        then:
        change.getConfirmationMessage() == "Data updated in TABLE_NAME"
    }


    @Override
    protected String getExpectedChangeName() {
        return "update"
    }


}
