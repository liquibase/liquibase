package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import static org.junit.Assert.*;

import org.junit.Test;

public class DropForeignKeyConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setConstraintName("FK_NAME");

        then:
        "Foreign key FK_NAME dropped" == change.getConfirmationMessage()
    }
}
