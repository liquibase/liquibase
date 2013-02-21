package liquibase.util;

import static org.junit.Assert.*;
import org.junit.Test;


public class StringUtilsTest {

    @Test
     public void windowsDelimiter() {
         String sql = "/*\n" +
                 "This is a test comment of MS-SQL script\n" +
                 "*/\n" +
                 "\n" +
                 "Select * from Test;\n" +
                 "Update Test set field = 1";
         String[] result = StringUtils.processMutliLineSQL(sql,true, true, ";");
         assertEquals(2,result.length);
         assertEquals("Select * from Test",result[0]);
         assertEquals("Update Test set field = 1",result[1]);
     }

    @Test
    public void multipleComments() {
        String raw = "/**\n" +
                "Some comments go here\n" +
                "**/\n" +
                "create table sqlfilerollback (id int);\n" +
                "\n" +
                "/**\n" +
                "Some morecomments go here\n" +
                "**/\n" +
                "create table sqlfilerollback2 (id int);";
        String[] strings = StringUtils.processMutliLineSQL(raw, true, true, null);
        assertEquals(2, strings.length);
        assertEquals("create table sqlfilerollback (id int)", strings[0]);
        assertEquals("create table sqlfilerollback2 (id int)", strings[1]);
    }

    @Test
    public void noComments() {
        String noComments=" Some text but no comments";
        String result = StringUtils.stripComments(noComments);
        assertEquals(noComments.trim(),result);
    }
    
    @Test
    public void singleLineNoNewLine() {
        String sql = "Some text" ;
        String comment = " -- with comment";
        String totalLine=sql + comment ;
        String result = StringUtils.stripComments(totalLine);
        
        assertEquals(sql,result);
    }
    
    @Test
    public void singleLineNoFollowOnLine() {
        String sql = "Some text" ;
        String comment = " -- with comment\n";
        String totalLine=sql + comment ;
        String result = StringUtils.stripComments(totalLine);
        
        assertEquals(sql.trim(),result);
    }
    
    @Test
    public void singleLongLineNoFollowOnLine() {
        StringBuilder sqlBuilder = new StringBuilder();
        for (int i = 0; i < 10000; ++i)
            sqlBuilder.append(" A");
        String sql = sqlBuilder.toString();
        String comment = " -- with comment\n";
        String totalLine=sql + comment ;
        long start = System.currentTimeMillis();
        String result = StringUtils.stripComments(totalLine);
        long end = System.currentTimeMillis();
        
        assertEquals(sql.trim(),result);
        assertTrue("Did not complete within 1 second", end - start <= 1000);
    }
    
    @Test
    public void singleLineMultipleComments() {
        String sql = "Some text" ;
        String comment = " -- with comment";
        String totalLine=sql + comment + "\n"+ sql + comment ;
        String result = StringUtils.stripComments(totalLine);
        
        assertEquals(sql+"\n"+sql,result);
    }
    
    @Test
    public void singleLineWithFollowupLine() {
        String sql = "Some text" ;
        String comment = " -- with comment";
        String totalLine=sql + comment + "\n" + sql ;
        String result = StringUtils.stripComments(totalLine);
        
        assertEquals(sql + "\n" + sql,result);
    }
    
    @Test
    public void multiLineOnOwnLine() {
        String sql = "/*Some text\nmore text*/" ;
        
        String result = StringUtils.stripComments(sql);
        assertEquals("",result);
    }
    
    @Test
    public void multiLineAfterSQL() {
        String sql = "some sql";
        String comment = "/*Some text\nmore text*/" ;
        String total = sql + comment;
        String result = StringUtils.stripComments(total);
        assertEquals(sql.trim(),result);
    }
    
    @Test
    public void multiLineFinishesWithTextOnLine() {
        String sql = "some sql";
        String comment = "/*Some text\nmore text*/" ;
        String total = comment + sql;
        String result = StringUtils.stripComments(total);
        assertEquals(sql.trim(),result);
    }
    
    @Test
    public void multiLineStartAndFinishWithSQL() {
        String sql = "some sql";
        String comment = "/*Some text\nmore text*/" ;
        String total = sql + comment + sql;
        String result = StringUtils.stripComments(total);
        assertEquals(sql.trim() + sql,result);
    }
    
    @Test
    public void shouldStripComments() {
        String sql = "some sql";
        String comment = "/*Some text\nmore text*/" ;
        String total = sql + comment + sql;
        String[] result = StringUtils.processMutliLineSQL(total,true, false, null);
        assertEquals(1,result.length);
        assertEquals(sql+sql,result[0]);
    }

    @Test
    public void stripComments2() {
        String sql = "insert into test_table values(1, 'hello');\n" +
                "insert into test_table values(2, 'hello');\n" +
                "--insert into test_table values(3, 'hello');\n" +
                "insert into test_table values(4, 'hello');";

        String[] result = StringUtils.processMutliLineSQL(sql, true, true, ";");
        assertEquals(3, result.length);
    }

    
    @Test
    public void shouldNotStripComments() {
        String sql = "some sql";
        String comment = "/*Some text\nmore text*/" ;
        String total = sql + comment + sql;
        String[] result = StringUtils.processMutliLineSQL(total,false, false, null);
        assertEquals(1,result.length);
        assertEquals(total,result[0]);
    }
    
    @Test
    public void splitOngo() {
        String sql = "some sql\ngo\nmore sql";
        String[] result = StringUtils.splitSQL(sql, null);
        assertEquals(2,result.length);
        assertEquals("some sql",result[0]);
        assertEquals("more sql",result[1]);
    }
    
    @Test
    public void splitOnGO() {
        String sql = "some sql\nGO\nmore sql";
        String[] result = StringUtils.splitSQL(sql, null);
        assertEquals(2,result.length);
        assertEquals("some sql",result[0]);
        assertEquals("more sql",result[1]);
    }

    @Test
    public void multilineComment() {
        String sql = "/*\n" +
                "This is a test comment of SQL script\n" +
                "*/\n" +
                "\n" +
                "Select * from Test;\n" +
                "Update Test set field = 1";
        String[] result = StringUtils.processMutliLineSQL(sql,true, true, null);
        assertEquals(2,result.length);
        assertEquals("Select * from Test",result[0]);
        assertEquals("Update Test set field = 1",result[1]);

    }

     @Test
    public void testSplitWithSemicolon() {
        StringBuilder sb = new StringBuilder();
        sb.append("select * from simple_select_statement;\n");
        sb.append("insert into table ( col ) values (' value with; semicolon ');");
        String[] result = StringUtils.processMutliLineSQL(sb.toString(), true, true, null);
        assertEquals("Unexpected amount of statements returned",2, result.length);
    }

    @Test
    public void splitWithGo() {
        String testString = "SELECT *\n" +
                "                                FROM sys.objects\n" +
                "                                WHERE object_id = OBJECT_ID(N'[test].[currval]')\n" +
                "                                AND type in (N'FN', N'IF', N'TF', N'FS', N'FT')\n" +
                "                        )\n" +
                "                        DROP FUNCTION [test].[currval]\n" +
                "go\n" +
                "                        IF EXISTS\n" +
                "                        (\n" +
                "                        SELECT *\n" +
                "                        FROM sys.objects\n" +
                "                        WHERE object_id = OBJECT_ID(N'[test].[nextval]')\n" +
                "                        AND type in (N'P', N'PC')\n" +
                "                        )\n" +
                "                        DROP PROCEDURE [test].[nextval]:";

        String[] strings = StringUtils.splitSQL(testString, null);
        assertEquals(2, strings.length);
    }

     @Test
    public void splitWithX() {
        String testString =  "insert into datatable (col) values ('a value with a ;') X\n"+
            "insert into datatable (col) values ('another value with a ;') X";

        String[] strings = StringUtils.splitSQL(testString, "X");
        assertEquals(2, strings.length);
         assertEquals("insert into datatable (col) values ('a value with a ;')", strings[0]);
         assertEquals("insert into datatable (col) values ('another value with a ;')", strings[1]);
    }

    @Test
    public void commentRemoval() {
        String testString = "--\n" +
                "-- Create the blog table.\n" +
                "--\n" +
                "CREATE TABLE blog\n" +
                "(\n" +
                "    ID                         NUMBER(15)    NOT NULL\n" +
                ")";

        String[] strings = StringUtils.processMutliLineSQL(testString, true, false, null);
        assertEquals(1, strings.length);
        assertTrue(strings[0].startsWith("CREATE TABLE blog"));
    }
}
