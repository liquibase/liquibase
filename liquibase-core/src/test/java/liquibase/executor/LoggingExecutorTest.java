package liquibase.executor;

import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import liquibase.change.core.RawSQLChange;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.util.StreamUtil;

public class LoggingExecutorTest {

    @Test
    public void outputDelimiterTrue() throws DatabaseException {
        StringWriter output = new StringWriter();
        MySQLDatabase database = new MySQLDatabase();
        LoggingExecutor executor = new LoggingExecutor(null, output, database);

        RawSQLChange change = new RawSQLChange("CREATE PROCEDURE x (a int)\n" +
                "        BEGIN\n" +
                "          DECLARE b int;\n" +
                "        END//");
        change.setEndDelimiter("//");
        change.setOutputDelimiter(true);

        executor.execute(change);

        Assert.assertEquals("delimiter //" + StreamUtil.getLineSeparator() +
                "CREATE PROCEDURE x (a int)" + StreamUtil.getLineSeparator() +
                "        BEGIN" + StreamUtil.getLineSeparator() +
                "          DECLARE b int;" + StreamUtil.getLineSeparator() +
                "        END//" + StreamUtil.getLineSeparator() +
                "delimiter ;" + StreamUtil.getLineSeparator()
                + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator(), output.toString());
    }

    @Test
    public void outputDelimiterFalse() throws DatabaseException {
        StringWriter output = new StringWriter();
        MySQLDatabase database = new MySQLDatabase();
        LoggingExecutor executor = new LoggingExecutor(null, output, database);

        RawSQLChange change = new RawSQLChange("CREATE PROCEDURE x (a int)\n" +
                "        BEGIN\n" +
                "          DECLARE b int;\n" +
                "        END//");
        change.setEndDelimiter("//");
        change.setOutputDelimiter(false);

        executor.execute(change);

        Assert.assertEquals("CREATE PROCEDURE x (a int)" + StreamUtil.getLineSeparator() +
                "        BEGIN" + StreamUtil.getLineSeparator() +
                "          DECLARE b int;" + StreamUtil.getLineSeparator() +
                "        END//" + StreamUtil.getLineSeparator() +
                StreamUtil.getLineSeparator(), output.toString());
    }
}
