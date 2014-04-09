package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.TagDatabaseStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class TagDatabaseChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new TagDatabaseChange()
        change.setTag("TAG_NAME");

        then:
        "Tag 'TAG_NAME' applied to database" == change.getConfirmationMessage()
    }   
}