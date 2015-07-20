package liquibase.change;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StreamUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractSQLChangeTest {

    @Test
    public void constructor() {
        AbstractSQLChange change = new ExampleAbstractSQLChange();
        assertFalse(change.isStripComments());
        assertTrue(change.isSplitStatements());
        assertNull(change.getEndDelimiter());
    }

    @Test
    public void supports() {
        assertTrue("AbstractSQLChange automatically supports all databases", new ExampleAbstractSQLChange().supports(mock(Database.class)));
    }

    @Test
    public void setStrippingComments() {
        AbstractSQLChange change = new ExampleAbstractSQLChange();
        change.setStripComments(true);
        assertTrue(change.isStripComments());

        change.setStripComments(false);
        assertFalse(change.isStripComments());

        change.setStripComments(null);
        assertFalse(change.isStripComments());
    }

    @Test
    public void setSplittingStatements() {
        AbstractSQLChange change = new ExampleAbstractSQLChange();
        change.setSplitStatements(true);
        assertTrue(change.isSplitStatements());

        change.setSplitStatements(false);
        assertFalse(change.isSplitStatements());

        change.setSplitStatements(null);
        assertTrue(change.isSplitStatements());
    }

    @Test
    public void setSql() {
        AbstractSQLChange sql = new ExampleAbstractSQLChange();
        sql.setSql("SOME SQL");
        assertEquals("SOME SQL", sql.getSql());

        sql.setSql("   SOME SQL   ");
        assertEquals("setSql should trim", "SOME SQL", sql.getSql());

        sql.setSql("   ");
        assertNull("setSql should set empty strings to null", sql.getSql());
    }

    @Test
    public void setEndDelmiter() {
        AbstractSQLChange change = new ExampleAbstractSQLChange();

        change.setEndDelimiter("GO");
        assertEquals("GO", change.getEndDelimiter());

        change.setEndDelimiter(";");
        assertEquals(";", change.getEndDelimiter());
    }

    @Test
    public void generateCheckSum_lineEndingIndependent() {
        CheckSum sql = new ExampleAbstractSQLChange("LINE 1;\nLINE 2;\nLINE3;").generateCheckSum();
        CheckSum sqlCRLF = new ExampleAbstractSQLChange("LINE 1;\r\nLINE 2;\r\nLINE3;").generateCheckSum();
        CheckSum sqlLF = new ExampleAbstractSQLChange("LINE 1;\rLINE 2;\rLINE3;").generateCheckSum();
        CheckSum sqlDifferent = new ExampleAbstractSQLChange("Something Completely Different").generateCheckSum();

        assertEquals(sql.toString(), sqlCRLF.toString());
        assertEquals(sql.toString(), sqlLF.toString());
        assertFalse(sql.toString().equals(sqlDifferent.toString()));
    }

    @Test
    public void generateCheckSum_nullSql() {
        assertNotNull(new ExampleAbstractSQLChange().generateCheckSum());
    }

    @Test
    public void generateCheckSum_changesBasedOnParams() {
        CheckSum baseCheckSum = new ExampleAbstractSQLChange("SOME SQL").generateCheckSum();

        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("SOME SQL");
        change.setSplitStatements(false);
        assertFalse(baseCheckSum.toString().equals(change.generateCheckSum().toString()));

        change = new ExampleAbstractSQLChange("SOME SQL");
        change.setEndDelimiter("X");
        assertFalse(baseCheckSum.toString().equals(change.generateCheckSum().toString()));

        change = new ExampleAbstractSQLChange("SOME SQL");
        change.setStripComments(true);
        assertFalse(baseCheckSum.toString().equals(change.generateCheckSum().toString()));
    }

    @Test
    public void generateStatements_nullSqlMakesNoStatements() {
        assertEquals(0, new ExampleAbstractSQLChange(null).generateStatements(mock(Database.class)).length);
    }

    @Test
    public void generateStatements() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");

        change.setSplitStatements(true);
        change.setStripComments(true);
        SqlStatement[] statements = change.generateStatements(mock(Database.class));
        assertEquals(3, statements.length);
        assertEquals("LINE 1", ((RawSqlStatement) statements[0]).getSql());
        assertEquals("LINE 2", ((RawSqlStatement) statements[1]).getSql());
        assertEquals("LINE 3", ((RawSqlStatement) statements[2]).getSql());
    }

    @Test
    public void generateStatements_crlfEndingStandardizes() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\r\n--a comment\r\nLINE 2;\r\nLINE 3;");

        change.setSplitStatements(true);
        change.setStripComments(true);
        SqlStatement[] statements = change.generateStatements(mock(Database.class));
        assertEquals(3, statements.length);
        assertEquals("LINE 1", ((RawSqlStatement) statements[0]).getSql());
        assertEquals("LINE 2", ((RawSqlStatement) statements[1]).getSql());
        assertEquals("LINE 3", ((RawSqlStatement) statements[2]).getSql());
    }

    @Test
    public void generateStatements_convertsEndingsOnSqlServer() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");

        change.setSplitStatements(false);
        change.setStripComments(true);
        SqlStatement[] statements = change.generateStatements(new MSSQLDatabase());
        assertEquals(1, statements.length);
        assertEquals("LINE 1;\r\n\r\nLINE 2;\r\nLINE 3;", ((RawSqlStatement) statements[0]).getSql());
    }

    @Test
    public void generateStatements_keepComments() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");

        change.setSplitStatements(true);
        change.setStripComments(false);
        SqlStatement[] statements = change.generateStatements(mock(Database.class));
        assertEquals(3, statements.length);
        assertEquals("LINE 1", ((RawSqlStatement) statements[0]).getSql());
        assertEquals("--a comment\nLINE 2", ((RawSqlStatement) statements[1]).getSql());
        assertEquals("LINE 3", ((RawSqlStatement) statements[2]).getSql());
    }

    @Test
    public void generateStatements_noSplit() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");

        change.setSplitStatements(false);
        change.setStripComments(true);
        SqlStatement[] statements = change.generateStatements(mock(Database.class));
        assertEquals(1, statements.length);
        assertEquals("LINE 1;\n\nLINE 2;\nLINE 3;", ((RawSqlStatement) statements[0]).getSql());
    }

    @Test
    public void generateStatements_noSplitKeepComments() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");

        change.setSplitStatements(false);
        change.setStripComments(false);
        SqlStatement[] statements = change.generateStatements(mock(Database.class));
        assertEquals(1, statements.length);
        assertEquals("LINE 1;\n--a comment\nLINE 2;\nLINE 3;", ((RawSqlStatement) statements[0]).getSql());
    }

    @Test
    public void normalizeSql() throws IOException {
        assertNormalizingStreamCorrect("single line String", "single line String");
        assertNormalizingStreamCorrect("single line string with whitespace", "single line string with      whitespace");
        assertNormalizingStreamCorrect("multiple line string", "\r\nmultiple\r\nline\r\nstring\r\n");
        assertNormalizingStreamCorrect("multiple line string", "\rmultiple\rline\rstring\r");
        assertNormalizingStreamCorrect("multiple line string", "\nmultiple\nline\nstring\n");
        assertNormalizingStreamCorrect("a line with double newlines", "\n\na\nline \n with \r\n \r\n double \n \n \n \n newlines");
//        assertNormalizingStreamCorrect("", null);
        assertNormalizingStreamCorrect("", "    ");
        assertNormalizingStreamCorrect("", " \n \n \n   \n  ");

        //test quickBuffer -> resizingBuffer handoff
        String longSpaceString = "a line with a lot of: wait for it....                                                                                                                                                                                                                                                                                         spaces";
        assertNormalizingStreamCorrect("a line with a lot of: wait for it.... spaces", longSpaceString);

        String versionNormalized = "INSERT INTO recommendation_list(instanceId, name, publicId) SELECT DISTINCT instanceId, \"default\" as name, \"default\" as publicId FROM recommendation;";

        String version1 = "INSERT INTO recommendation_list(instanceId, name, publicId)\n" +
                "SELECT DISTINCT instanceId, \"default\" as name, \"default\" as publicId\n" +
                "FROM recommendation;";
        assertNormalizingStreamCorrect(versionNormalized, version1);

        String version2 = "INSERT INTO \n" +
                "    recommendation_list(instanceId, name, publicId)\n" +
                "SELECT \n" +
                "    DISTINCT \n" +
                "        instanceId, \n" +
                "          \"default\" as name, \n" +
                "          \"default\" as publicId\n" +
                "   FROM \n" +
                "       recommendation;";
        assertNormalizingStreamCorrect(versionNormalized, version2);
    }

    private void assertNormalizingStreamCorrect(String expected, String toCorrect) throws IOException {
        AbstractSQLChange.NormalizingStream normalizingStream = new AbstractSQLChange.NormalizingStream("x", true, false, new ByteArrayInputStream(toCorrect.getBytes()));
        assertEquals("x:true:false:"+expected, StreamUtil.getStreamContents(normalizingStream));
    }

    @Test
    public void generateStatements_willCallNativeSqlIfPossible() throws DatabaseException {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("SOME SQL");

        Database database = mock(Database.class);
        DatabaseConnection connection = mock(DatabaseConnection.class);
        when(database.getConnection()).thenReturn(connection);
        when(connection.nativeSQL("SOME SQL")).thenReturn("SOME NATIVE SQL");

        SqlStatement[] statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        assertEquals("SOME NATIVE SQL", ((RawSqlStatement) statements[0]).getSql());

        //If there is an error, it falls back to passed SQL
        when(connection.nativeSQL("SOME SQL")).thenThrow(new DatabaseException("Testing exception"));
        statements = change.generateStatements(database);
        assertEquals(1, statements.length);
        assertEquals("SOME SQL", ((RawSqlStatement) statements[0]).getSql());
    }

    @DatabaseChange(name = "exampleAbstractSQLChange", description = "Used for the AbstractSQLChangeTest unit test", priority = 1)
    private static class ExampleAbstractSQLChange extends AbstractSQLChange {

        private ExampleAbstractSQLChange() {
        }

        private ExampleAbstractSQLChange(String sql) {
            setSql(sql);
        }


        @Override
        public String getConfirmationMessage() {
            return "Example SQL Change Message";
        }

        @Override
        public String getSerializedObjectNamespace() {
            return STANDARD_CHANGELOG_NAMESPACE;
        }

    }
}
