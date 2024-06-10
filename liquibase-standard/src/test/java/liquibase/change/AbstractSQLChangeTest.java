package liquibase.change;

import liquibase.ChecksumVersion;
import liquibase.Scope;
import liquibase.database.core.MSSQLDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.util.StreamUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

public class AbstractSQLChangeTest {

    @Test
    public void constructor() {
        AbstractSQLChange change = new ExampleAbstractSQLChange();
        assertFalse(change.isStripComments());
        assertTrue(change.isSplitStatements());
        assertNull(change.getEndDelimiter());
    }

//    @Test
//    public void supports() {
//        assertTrue("AbstractSQLChange automatically supports all databases", new ExampleAbstractSQLChange().supports(mock(Database.class)));
//    }

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
    public void setEndDelimiter() {
        AbstractSQLChange change = new ExampleAbstractSQLChange();

        change.setEndDelimiter("GO");
        assertEquals("GO", change.getEndDelimiter());

        change.setEndDelimiter(";");
        assertEquals(";", change.getEndDelimiter());
    }

    @Test
    public void generateCheckSum_lineEndingIndependent() throws Exception {
        CheckSum sql = new ExampleAbstractSQLChange("LINE 1;\nLINE 2;\nLINE3;").generateCheckSum();
        CheckSum sqlCRLF = new ExampleAbstractSQLChange("LINE 1;\r\nLINE 2;\r\nLINE3;").generateCheckSum();
        CheckSum sqlLF = new ExampleAbstractSQLChange("LINE 1;\rLINE 2;\rLINE3;").generateCheckSum();
        CheckSum sqlDifferent = Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), ChecksumVersion.V8), () ->
                new ExampleAbstractSQLChange("Something Completely Different").generateCheckSum());

        assertEquals(sql.toString(), sqlCRLF.toString());
        assertEquals(sql.toString(), sqlLF.toString());
        assertNotEquals(sql.toString(), sqlDifferent.toString());
    }

    @Test
    public void generateCheckSum_nullSql() {
        assertNotNull(new ExampleAbstractSQLChange().generateCheckSum());
    }

    @Test
    public void generateCheckSum_changesBasedOnParams_latest() {
        CheckSum baseCheckSum = new ExampleAbstractSQLChange("SOME SQL").generateCheckSum();

        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("SOME SQL");
        change.setSplitStatements(false);
        assertEquals(baseCheckSum.toString(), change.generateCheckSum().toString());

        change = new ExampleAbstractSQLChange("SOME SQL");
        change.setEndDelimiter("X");
        assertEquals(baseCheckSum.toString(), change.generateCheckSum().toString());

        change = new ExampleAbstractSQLChange("SOME SQL");
        change.setStripComments(true);
        assertEquals(baseCheckSum.toString(), change.generateCheckSum().toString());
    }

    @Test
    public void generateCheckSum_changesBasedOnParams_v8() throws Exception {
        CheckSum baseCheckSum = new ExampleAbstractSQLChange("SOME SQL").generateCheckSum();

        final ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("SOME SQL");
        change.setSplitStatements(false);
        assertNotEquals(baseCheckSum.toString(), Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), ChecksumVersion.V8), () ->
                change.generateCheckSum().toString()));

        final ExampleAbstractSQLChange change2 = new ExampleAbstractSQLChange("SOME SQL");
        change2.setEndDelimiter("X");
        assertNotEquals(baseCheckSum.toString(),  Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), ChecksumVersion.V8), () ->
                change2.generateCheckSum().toString()));

        final ExampleAbstractSQLChange change3 = new ExampleAbstractSQLChange("SOME SQL");
        change3.setStripComments(true);
        assertNotEquals(baseCheckSum.toString(),  Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), ChecksumVersion.V8), () ->
                change3.generateCheckSum().toString()));
    }

//    @Test
//    public void generateStatements_nullSqlMakesNoStatements() {
//        assertEquals(0, new ExampleAbstractSQLChange(null).generateStatements(mock(Database.class)).length);
//    }
//
//    @Test
//    public void generateStatements() {
//        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");
//
//        change.setSplitStatements(true);
//        change.setStripComments(true);
//        SqlStatement[] statements = change.generateStatements(mock(Database.class));
//        assertEquals(3, statements.length);
//        assertEquals("LINE 1", ((RawParameterizedSqlStatement) statements[0]).getSql());
//        assertEquals("LINE 2", ((RawParameterizedSqlStatement) statements[1]).getSql());
//        assertEquals("LINE 3", ((RawParameterizedSqlStatement) statements[2]).getSql());
//    }
//
//    @Test
//    public void generateStatements_crlfEndingStandardizes() {
//        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\r\n--a comment\r\nLINE 2;\r\nLINE 3;");
//
//        change.setSplitStatements(true);
//        change.setStripComments(true);
//        SqlStatement[] statements = change.generateStatements(mock(Database.class));
//        assertEquals(3, statements.length);
//        assertEquals("LINE 1", ((RawParameterizedSqlStatement) statements[0]).getSql());
//        assertEquals("LINE 2", ((RawParameterizedSqlStatement) statements[1]).getSql());
//        assertEquals("LINE 3", ((RawParameterizedSqlStatement) statements[2]).getSql());
//    }

    @Test
    public void generateStatements_convertsEndingsOnSqlServer() {
        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");

        change.setSplitStatements(false);
        change.setStripComments(true);
        SqlStatement[] statements = change.generateStatements(new MSSQLDatabase());
        assertEquals(1, statements.length);
        assertEquals("LINE 1;\r\n\r\nLINE 2;\r\nLINE 3;", ((RawParameterizedSqlStatement) statements[0]).getSql());
    }

//    @Test
//    public void generateStatements_keepComments() {
//        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");
//
//        change.setSplitStatements(true);
//        change.setStripComments(false);
//        SqlStatement[] statements = change.generateStatements(mock(Database.class));
//        assertEquals(3, statements.length);
//        assertEquals("LINE 1", ((RawParameterizedSqlStatement) statements[0]).getSql());
//        assertEquals("--a comment\nLINE 2", ((RawParameterizedSqlStatement) statements[1]).getSql());
//        assertEquals("LINE 3", ((RawParameterizedSqlStatement) statements[2]).getSql());
//    }
//
//    @Test
//    public void generateStatements_noSplit() {
//        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");
//
//        change.setSplitStatements(false);
//        change.setStripComments(true);
//        SqlStatement[] statements = change.generateStatements(mock(Database.class));
//        assertEquals(1, statements.length);
//        assertEquals("LINE 1;\n\nLINE 2;\nLINE 3;", ((RawParameterizedSqlStatement) statements[0]).getSql());
//    }
//
//    @Test
//    public void generateStatements_noSplitKeepComments() {
//        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("LINE 1;\n--a comment\nLINE 2;\nLINE 3;");
//
//        change.setSplitStatements(false);
//        change.setStripComments(false);
//        SqlStatement[] statements = change.generateStatements(mock(Database.class));
//        assertEquals(1, statements.length);
//        assertEquals("LINE 1;\n--a comment\nLINE 2;\nLINE 3;", ((RawParameterizedSqlStatement) statements[0]).getSql());
//    }

    @Test
    public void normalizeSql_latest() throws IOException {
        assertNormalizingStreamCorrectLatest("singlelineString", "single line String");
        assertNormalizingStreamCorrectLatest("singlelinestringwithwhitespace", "single line string with      whitespace");
        assertNormalizingStreamCorrectLatest("multiplelinestring", "\r\nmultiple\r\nline\r\nstring\r\n");
        assertNormalizingStreamCorrectLatest("multiplelinestring", "\rmultiple\rline\rstring\r");
        assertNormalizingStreamCorrectLatest("multiplelinestring", "\nmultiple\nline\nstring\n");
        assertNormalizingStreamCorrectLatest("alinewithdoublenewlines", "\n\na\nline \n with \r\n \r\n double \n \n \n \n newlines");
//        assertNormalizingStreamCorrectLatest("", null);
        assertNormalizingStreamCorrectLatest("", "    ");
        assertNormalizingStreamCorrectLatest("", " \n \n \n   \n  ");

        //test quickBuffer -> resizingBuffer handoff
        String longSpaceString = "a line with a lot of: wait for it....                                                                                                                                                                                                                                                                                         spaces";
        assertNormalizingStreamCorrectLatest("alinewithalotof:waitforit....spaces", longSpaceString);

        String versionNormalized = "INSERTINTOrecommendation_list(instanceId,name,publicId)SELECTDISTINCTinstanceId,\"default\"asname,\"default\"aspublicIdFROMrecommendation;";

        String version1 = "INSERT INTO recommendation_list(instanceId, name, publicId)\n" +
                "SELECT DISTINCT instanceId, \"default\" as name, \"default\" as publicId\n" +
                "FROM recommendation;";
        assertNormalizingStreamCorrectLatest(versionNormalized, version1);

        String version2 = "INSERT INTO \n" +
                "    recommendation_list(instanceId, name, publicId)\n" +
                "SELECT \n" +
                "    DISTINCT \n" +
                "        instanceId, \n" +
                "          \"default\" as name, \n" +
                "          \"default\" as publicId\n" +
                "   FROM \n" +
                "       recommendation;";
        assertNormalizingStreamCorrectLatest(versionNormalized, version2);
    }

    @Test
    public void normalizeSql_V8() throws IOException {
        assertNormalizingStreamCorrectV8("single line String", "single line String");
        assertNormalizingStreamCorrectV8("single line string with whitespace", "single line string with      whitespace");
        assertNormalizingStreamCorrectV8("multiple line string", "\r\nmultiple\r\nline\r\nstring\r\n");
        assertNormalizingStreamCorrectV8("multiple line string", "\rmultiple\rline\rstring\r");
        assertNormalizingStreamCorrectV8("multiple line string", "\nmultiple\nline\nstring\n");
        assertNormalizingStreamCorrectV8("a line with double newlines", "\n\na\nline \n with \r\n \r\n double \n \n \n \n newlines");
//        assertNormalizingStreamCorrectV8("", null);
        assertNormalizingStreamCorrectV8("", "    ");
        assertNormalizingStreamCorrectV8("", " \n \n \n   \n  ");

        //test quickBuffer -> resizingBuffer handoff
        String longSpaceString = "a line with a lot of: wait for it....                                                                                                                                                                                                                                                                                         spaces";
        assertNormalizingStreamCorrectV8("a line with a lot of: wait for it.... spaces", longSpaceString);

        String versionNormalized = "INSERT INTO recommendation_list(instanceId, name, publicId) SELECT DISTINCT instanceId, \"default\" as name, \"default\" as publicId FROM recommendation;";

        String version1 = "INSERT INTO recommendation_list(instanceId, name, publicId)\n" +
                "SELECT DISTINCT instanceId, \"default\" as name, \"default\" as publicId\n" +
                "FROM recommendation;";
        assertNormalizingStreamCorrectV8(versionNormalized, version1);

        String version2 = "INSERT INTO \n" +
                "    recommendation_list(instanceId, name, publicId)\n" +
                "SELECT \n" +
                "    DISTINCT \n" +
                "        instanceId, \n" +
                "          \"default\" as name, \n" +
                "          \"default\" as publicId\n" +
                "   FROM \n" +
                "       recommendation;";
        assertNormalizingStreamCorrectV8(versionNormalized, version2);
    }

    private void assertNormalizingStreamCorrectLatest(String expected, String toCorrect) throws IOException {
        AbstractSQLChange.NormalizingStream normalizingStream = new AbstractSQLChange.NormalizingStream(new ByteArrayInputStream(toCorrect.getBytes()));
        assertEquals(expected, StreamUtil.readStreamAsString(normalizingStream));
    }

    private void assertNormalizingStreamCorrectV8(String expected, String toCorrect) throws IOException {
        NormalizingStreamV8 normalizingStream = new NormalizingStreamV8("x", true, false, new ByteArrayInputStream(toCorrect.getBytes()));
        assertEquals("x:true:false:"+expected, StreamUtil.readStreamAsString(normalizingStream));
    }

//    @Test
//    public void generateStatements_willCallNativeSqlIfPossible() throws DatabaseException {
//        ExampleAbstractSQLChange change = new ExampleAbstractSQLChange("SOME SQL");
//
//        Database database = mock(Database.class);
//        DatabaseConnection connection = mock(DatabaseConnection.class);
//        when(database.getConnection()).thenReturn(connection);
//        when(connection.nativeSQL("SOME SQL")).thenReturn("SOME NATIVE SQL");
//
//        SqlStatement[] statements = change.generateStatements(database);
//        assertEquals(1, statements.length);
//        assertEquals("SOME NATIVE SQL", ((RawParameterizedSqlStatement) statements[0]).getSql());
//
//        //If there is an error, it falls back to passed SQL
//        when(connection.nativeSQL("SOME SQL")).thenThrow(new DatabaseException("Testing exception"));
//        statements = change.generateStatements(database);
//        assertEquals(1, statements.length);
//        assertEquals("SOME SQL", ((RawParameterizedSqlStatement) statements[0]).getSql());
//    }

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
