package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.change.ColumnConfig;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class InsertDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new InsertDataChange();
        change.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("id");
        col1.setValueNumeric("123");

        ColumnConfig col2 = new ColumnConfig();
        col2.setName("name");
        col2.setValue("Andrew");

        ColumnConfig col3 = new ColumnConfig();
        col3.setName("age");
        col3.setValueNumeric("21");

        ColumnConfig col4 = new ColumnConfig();
        col4.setName("height");
        col4.setValueNumeric("1.78");

        change.addColumn(col1);
        change.addColumn(col2);
        change.addColumn(col3);
        change.addColumn(col4);

        then:
        "New row inserted into TABLE_NAME" == change.getConfirmationMessage()
    }

    @Override
    protected String getExpectedChangeName() {
        return "insert"
    }
}
