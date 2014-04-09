package liquibase.change.core;

import java.math.BigInteger;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.UpdateStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddColumnChangeTest extends StandardChangeTest {


    def "add and remove column methods"() throws Exception {
        when:
    	def columnA = new AddColumnConfig();
    	columnA.setName("a");

        def columnB = new AddColumnConfig();
    	columnB.setName("b");

        def change = new AddColumnChange();

        then:
        change.getColumns().size() == 0

        change.removeColumn(columnA);
        change.getColumns().size() == 0

        change.addColumn(columnA);
        change.getColumns().size() == 1

        change.removeColumn(columnB);
        change.getColumns().size() == 1

        change.removeColumn(columnA);
        change.getColumns().size() == 0
    }

    def getConfirmationMessage() throws Exception {
        when:
        def refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.addColumn(column);

        then:
        refactoring.getConfirmationMessage() == "Columns NEWCOL(TYP) added to TAB"
    }

}
