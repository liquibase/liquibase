package liquibase.change;

import liquibase.database.OracleDatabase;
import liquibase.exception.SetupException;
import liquibase.resource.ClassLoaderFileOpener;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the SQL File with a simple text file. No real SQL is used with the
 * tests.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 *
 */
public abstract class SQLFileChangeTest extends AbstractChangeTest {
	
	SQLFileChange change;
	String fileName;
	
	@Before
    public void setUp() throws Exception {
	    //file contains contents "TESTDATA"
    	fileName="liquibase/change/SQLFileTestData.sql";
        change = new SQLFileChange();
        ClassLoaderFileOpener opener = new ClassLoaderFileOpener();
        change.setFileOpener(opener);
        change.setPath(fileName);
        change.init();
    }
	
	@Test
	public void setFileOpener() {
	    assertNotNull(change.getFileOpener());
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
    
    @Test
    public void generateStatementFileNotFound() throws Exception {
        try {
            change.setPath("doesnotexist.sql");
            change.init();
            change.generateStatements(new OracleDatabase());
            
            fail("The file does not exist so should not be found");
        } catch(SetupException fnfe) {
            //expected
        }
    }
    
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
		assertEquals("SQL From File", change.getChangeMetaData().getDescription());

	}

}
