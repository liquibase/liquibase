package liquibase.change.core

import liquibase.change.StandardChangeTest
import liquibase.database.core.MySQLDatabase
import liquibase.statement.core.SetNullableStatement
import liquibase.statement.core.UpdateStatement

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

    def should_generateStatements_add_update_statement_before_not_null_constraint() {
        given:
        def change = new AddNotNullConstraintChange()
        change.setTableName("table_name")
        change.setColumnName("column_name")
        change.setColumnDataType("varchar(20)")
        change.setDefaultNullValue("Hello World!")

        def database = new MySQLDatabase()

        when:
        def output = change.generateStatements(database)

        then:
        output.length == 2
        output[0] instanceof UpdateStatement
        def update = (UpdateStatement) output[0]
        update.getTableName() == "table_name"
        update.getNewColumnValues().size() == 1
        update.getNewColumnValues().get("column_name") == "Hello World!"
        update.getWhereClause() == "column_name IS NULL"

        output[1] instanceof SetNullableStatement
    }

    def should_generateStatements_update_statement_handle_boolean_type() {
        given:
        def change = new AddNotNullConstraintChange()
        change.setTableName("FOO")
        change.setColumnName("BAR")
        change.setColumnDataType("BOOLEAN")
        change.setDefaultNullValue("false")

        def database = new MySQLDatabase()

        when:
        def output = change.generateStatements(database)

        then:
        output.length == 2
        output[0] instanceof UpdateStatement
        def update = (UpdateStatement) output[0]
        update.getTableName() == "FOO"
        update.getNewColumnValues().size() == 1
        update.getNewColumnValues().get("BAR") == 0
        update.getWhereClause() == "BAR IS NULL"

        output[1] instanceof SetNullableStatement
    }

    def should_generateStatements_update_statement_handle_bit_1_type() {
        given:
        def change = new AddNotNullConstraintChange()
        change.setTableName("xxx")
        change.setColumnName("col_name")
        change.setColumnDataType("BIT(1)")
        change.setDefaultNullValue("1")

        def database = new MySQLDatabase()

        when:
        def output = change.generateStatements(database)

        then:
        output.length == 2
        output[0] instanceof UpdateStatement
        def update = (UpdateStatement) output[0]
        update.getTableName() == "xxx"
        update.getNewColumnValues().size() == 1
        update.getNewColumnValues().get("col_name") == 1
        update.getWhereClause() == "col_name IS NULL"

        output[1] instanceof SetNullableStatement
    }

}
