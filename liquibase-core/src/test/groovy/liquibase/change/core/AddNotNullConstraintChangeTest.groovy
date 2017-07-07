package liquibase.change.core

import liquibase.change.StandardChangeTest

public class AddNotNullConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setConstraintName("COL_NN");

        then:
        change.getConfirmationMessage() == "NOT NULL constraint \"COL_NN\" has been added to TABLE_NAME.COL_HERE"
    }

    def getInverse() throws Exception {
        when:
        def change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setConstraintName("COL_NN");
        DropNotNullConstraintChange[] reverses = change.createInverses()

        then:
        reverses.length == 1
        reverses[0].getTableName() == "TABLE_NAME"
        reverses[0].getColumnName() == "COL_HERE"
    }
}