package liquibase.change.core

import liquibase.change.StandardChangeTest

public class AddNotNullConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        then:
        change.getConfirmationMessage() == "Null constraint has been added to TABLE_NAME.COL_HERE"
    }
}