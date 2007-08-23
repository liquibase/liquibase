package liquibase.migrator.change;

import liquibase.database.MSSQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.migrator.ClassLoaderFileOpener;
import liquibase.migrator.exception.SetupException;
import liquibase.util.MD5Util;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests the SQL File with a simple text file. No real SQL is used with the
 * tests.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 *
 */
public class SQLFileChangeTest extends AbstractChangeTest {
	
	SQLFileChange change;
	String fileName;
	
	@Before
    public void setUp() throws Exception {
	    //file contains contents "TESTDATA"
    	fileName="liquibase/migrator/change/SQLFileTestData.sql";
        change = new SQLFileChange();
        ClassLoaderFileOpener opener = new ClassLoaderFileOpener();
        change.setFileOpener(opener);
        change.setPath(fileName);
        change.setUp();
    }
	
	@Test
	public void setFileOpener() {
	    assertNotNull(change.getFileOpener());
	}
    
    @Test
	public void createNode() throws Exception {
 
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("sqlFile", element.getTagName());
        
        assertEquals(fileName, element.getAttribute("path"));
	}

    @Test
	public void generateStatement() throws Exception {
		assertEquals(fileName,change.getPath());
		
    	assertEquals("TESTDATA",change.generateStatements(new OracleDatabase())[0]);
    	
    	assertEquals(MD5Util.computeMD5(change.getSql()), change.getMD5Sum());
	}
    
    @Test
    public void generateStatementFileNotFound() throws Exception {
        try {
            change.setPath("doesnotexist.sql");
            change.setUp();
            change.generateStatements(new OracleDatabase());
            
            fail("The file does not exist so should not be found");
        } catch(SetupException fnfe) {
            //expected
        }
    }
    
    @Test
    public void multiLineSQLFileSemiColon() throws Exception {
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql("SELECT * FROM customer;" +
                "SELECT * from table;");
        String[] statements = change2.generateStatements(new OracleDatabase());
        
        assertEquals(2,statements.length);
        assertEquals("SELECT * FROM customer",statements[0]);
        assertEquals("SELECT * from table",statements[1]);
    }
    
    @Test
    public void singleLineEndInSemiColon() throws Exception {
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql("SELECT * FROM customer;");
        String[] statements = change2.generateStatements(new OracleDatabase());
        assertEquals(1,statements.length);
        assertEquals("SELECT * FROM customer",statements[0]);
    }
    
    @Test
    public void singleLineEndGo() throws Exception {
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql("SELECT * FROM customer\ngo");
        String[] statements = change2.generateStatements(new MSSQLDatabase());
        assertEquals(1,statements.length);
        assertEquals("SELECT * FROM customer",statements[0]);
    }
    
    @Test
    public void singleLineBeginGo() throws Exception {
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql("goSELECT * FROM customer\ngo");
        String[] statements = change2.generateStatements(new MSSQLDatabase());
        assertEquals(1,statements.length);
        assertEquals("goSELECT * FROM customer",statements[0]);
    }
    
    @Test
    public void multiLineSQLFileGoShouldFind() throws Exception {
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql("SELECT * FROM customer\ngo\n" +
                "SELECT * from table\ngo");
        String[] statements = change2.generateStatements(new MSSQLDatabase());
        assertEquals(2,statements.length);
        assertEquals("SELECT * FROM customer",statements[0]);
        assertEquals("SELECT * from table",statements[1]);
    }
    
    @Test
    public void multiLineSQLFileGoShouldNotFind() throws Exception {
        SQLFileChange change2 = new SQLFileChange();
        change2.setSql("SELECT * FROM go\ngo\n" +
                "SELECT * from gogo\ngo\n");
        String[] statements = change2.generateStatements(new MSSQLDatabase());
        
        assertEquals(2,statements.length);
        assertEquals("SELECT * FROM go",statements[0]);
        assertEquals("SELECT * from gogo",statements[1]);
    }

    @Test
	public void getConfirmationMessage() throws Exception {
    	change.setPath(fileName);
		assertEquals("SQL File " + fileName + " has been executed", change.getConfirmationMessage());
	}

    @Test
	public void getRefactoringName() throws Exception {
		assertEquals("SQL From File", change.getChangeName());

	}

}
