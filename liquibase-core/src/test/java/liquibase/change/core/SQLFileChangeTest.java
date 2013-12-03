package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Map;

import liquibase.change.*;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.SetupException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.statement.SqlStatement;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the SQL File with a simple text file. No real SQL is used with the
 * tests.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 *
 */
public class SQLFileChangeTest extends StandardChangeTest {
	
	private SQLFileChange change;
	private String fileName;
	
	@Before
    public void setUp() throws Exception {
	    //file contains contents "TESTDATA"
    	fileName= "liquibase/change/core/SQLFileTestData.sql";
        change = new SQLFileChange();
        change.setSql("select * from tablename");
        ClassLoaderResourceAccessor opener = new ClassLoaderResourceAccessor();
        change.setResourceAccessor(opener);
        change.setPath(fileName);
        change.finishInitialization();
    }

    @Override
    public void generateStatement() throws Exception {

    }


    @Test
    public void multilineComment2() {
        String original = "--\r\n"+
                "-- This is a comment\r\n" +
                "UPDATE tablename SET column = 1;\r\n" +
                "GO";
        SQLFileChange change = new SQLFileChange();
        change.setSql(original);
        change.setSplitStatements(true);                 
        change.setStripComments(true);
        SqlStatement[] out = change.generateStatements(new MockDatabase());
        assertEquals(2, out.length);
        assertEquals("UPDATE tablename SET column = 1", out[0].toString());
    }


    @Test
	public void setFileOpener() {
	    assertNotNull(change.getResourceAccessor());
	}
    
//    @Test
//	public void generateStatement() throws Exception {
//		assertEquals(fileName,change.getPath());
//
//        OracleDatabase database = new OracleDatabase();
//        assertEquals("TESTDATA",change.generateStatements(database)[0].getSqlStatement(database));
//
//    	assertEquals(MD5Util.computeMD5(change.getSql()), change.generateCheckSum());
//	}
    
//    @Test
//    public void generateStatementFileNotFound() throws Exception {
//        try {
//            change.setPath("doesnotexist.sql");
//            change.finishInitialization();
//            change.generateStatements(new OracleDatabase());
//
//            fail("The file does not exist so should not be found");
//        } catch(SetupException fnfe) {
//            //expected
//        }
//    }
    
//    @Test
//    public void multiLineSQLFileSemiColon() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM customer;" + StreamUtil.getLineSeparator() +
//                "SELECT * from table;" + StreamUtil.getLineSeparator() +
//                "SELECT * from table2;" + StreamUtil.getLineSeparator());
//        OracleDatabase database = new OracleDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//
//        assertEquals(3,statements.length);
//        assertEquals("SELECT * FROM customer",statements[0].getSqlStatement(database));
//        assertEquals("SELECT * from table",statements[1].getSqlStatement(database));
//        assertEquals("SELECT * from table2",statements[2].getSqlStatement(database));
//    }
    
//    @Test
//    public void singleLineEndInSemiColon() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM customer;");
//        OracleDatabase database = new OracleDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//        assertEquals(1,statements.length);
//        assertEquals("SELECT * FROM customer",statements[0].getSqlStatement(database));
//    }
    
//    @Test
//    public void singleLineEndGo() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM customer\ngo");
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//        assertEquals(1,statements.length);
//        assertEquals("SELECT * FROM customer",statements[0].getSqlStatement(database));
//    }
    
//    @Test
//    public void singleLineBeginGo() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("goSELECT * FROM customer\ngo");
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//        assertEquals(1,statements.length);
//        assertEquals("goSELECT * FROM customer",statements[0].getSqlStatement(database));
//    }
    
//    @Test
//    public void multiLineSQLFileGoShouldFind() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM customer\ngo\n" +
//                "SELECT * from table\ngo");
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//        assertEquals(2,statements.length);
//        assertEquals("SELECT * FROM customer",statements[0].getSqlStatement(database));
//        assertEquals("SELECT * from table",statements[1].getSqlStatement(database));
//    }
    
//    @Test
//    public void multiLineSQLFileGoShouldNotFind() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM go\ngo\n" +
//                "SELECT * from gogo\ngo\n");
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//
//        assertEquals(2,statements.length);
//        assertEquals("SELECT * FROM go",statements[0].getSqlStatement(database));
//        assertEquals("SELECT * from gogo",statements[1].getSqlStatement(database));
//    }

    @Override
    @Test
	public void getConfirmationMessage() throws Exception {
    	change.setPath(fileName);
		assertEquals("SQL in file " + fileName + " executed", change.getConfirmationMessage());
	}

    @Override
    @Test
	public void getRefactoringName() throws Exception {
		assertEquals("sqlFile", ChangeFactory.getInstance().getChangeMetaData(change).getName());

	}

    @Test
    public void testStatementsWithSemicolons() {
        AbstractSQLChange change2 = new SQLFileChange();
        String insertWithSemicolon = "insert into table ( col ) values (' value with; semicolon ');";
        change2.setSql(insertWithSemicolon);
        Database database = new MockDatabase();
        SqlStatement[] statements = change2.generateStatements(database);
        assertEquals("Unexpected amount of statements returned",1, statements.length);
        String insertWithoutTrailingSemicolon = insertWithSemicolon.substring(0, insertWithSemicolon.length() - 1);
        assertEquals("unexpected SQL statement returned", insertWithoutTrailingSemicolon, statements[0].toString());
    }

    @Override
    protected void checkThatChecksumIsNew(Change change, Map<String, String> seenCheckSums, Field field) {
        //always ok
    }
    
    @Test
   public void replacementOfProperties() throws Exception
   {
      SQLFileChange change = new SQLFileChange();
      ChangeLogParameters changeLogParameters = new ChangeLogParameters();
      changeLogParameters.set("table.prefix", "prfx");
      changeLogParameters.set("some.other.prop", "nofx");
       ChangeSet changeSet = new ChangeSet("x", "y", true, true, null, null, null, null);
       changeSet.setChangeLogParameters(changeLogParameters);
       change.setChangeSet(changeSet);
      
      String fakeSql = "create ${table.prefix}_customer (${some.other.prop} INTEGER NOT NULL, PRIMARY KEY (${some.other.prop}));";
      
      change.setSql(fakeSql);
      
      String expected = "create prfx_customer (nofx INTEGER NOT NULL, PRIMARY KEY (nofx));";
      assertEquals(expected, change.getSql());
   }
}
