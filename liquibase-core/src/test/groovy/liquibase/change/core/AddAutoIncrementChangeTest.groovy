package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.change.ChangeMetaData;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.SetNullableStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddAutoIncrementChangeTest extends StandardChangeTest {

    def getAppliesTo() {
        expect:
        def change = new AddAutoIncrementChange();
        ChangeFactory.getInstance().getChangeMetaData(change).getAppliesTo().iterator().next() == "column"
    }


    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddAutoIncrementChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COLUMN_NAME");
        change.setColumnDataType("DATATYPE(255)");

        then:
        change.getConfirmationMessage() == "Auto-increment added to TABLE_NAME.COLUMN_NAME"
    }

    def "check change metadata"() {
        expect:
        def change = new AddAutoIncrementChange();
        def metaData = ChangeFactory.getInstance().getChangeMetaData(change);
        metaData.getName() == "addAutoIncrement"

    }

}
