package liquibase.change;

import liquibase.util.XMLUtil;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class AbstractSQLChangeTest extends AbstractChangeTest {

    private RawSQLChange refactoring;
    
    @Before
    public void setUp() throws Exception {
        refactoring = new RawSQLChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Custom SQL", new RawSQLChange().getDescription());
    }

    public void generateStatement() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //    @Test
//    public void generateStatement() throws Exception {
//        refactoring.setSql("SQL STATEMENT HERE");
//        OracleDatabase database = new OracleDatabase();
//        assertEquals("SQL STATEMENT HERE", refactoring.generateStatements(database)[0].getSqlStatement(database));
//    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Custom SQL executed", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        refactoring.setSql("SOME SQL HERE");

        Element element = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("sql", element.getTagName());

        assertEquals("SOME SQL HERE", XMLUtil.getTextContent(element));
    }
    
//    @Test
//    public void multiLineSQLFileSemiColon() throws Exception {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM customer;\n" +
//                "SELECT * from table;");
//        OracleDatabase database = new OracleDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//
//        assertEquals(2,statements.length);
//        assertEquals("SELECT * FROM customer",statements[0].getSqlStatement(database));
//        assertEquals("SELECT * from table",statements[1].getSqlStatement(database));
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
    
//    @Test
//    public void stripComments() throws UnsupportedChangeException, StatementNotSupportedOnDatabaseException {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM x\n -- A comment");
//        change2.setStripComments(true);
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//
//        assertEquals(1,statements.length);
//        assertEquals("SELECT * FROM x\n",statements[0].getSqlStatement(database));
//    }
    
//    @Test
//    public void turnOffSplitting() throws UnsupportedChangeException, StatementNotSupportedOnDatabaseException {
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM x;SELECT * FROM x;");
//        change2.setSplitStatements(false);
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//
//        assertEquals(1,statements.length);
//        assertEquals("SELECT * FROM x;SELECT * FROM x;",statements[0].getSqlStatement(database));
//    }
    
//    @Test
//    public void defaultSplittingAndNoStripping() throws Exception{
//        SQLFileChange change2 = new SQLFileChange();
//        change2.setSql("SELECT * FROM x;\n/*A Comment*/SELECT * FROM x;");
//
//        MSSQLDatabase database = new MSSQLDatabase();
//        SqlStatement[] statements = change2.generateStatements(database);
//
//        assertEquals(2,statements.length);
//        assertEquals("SELECT * FROM x",statements[0].getSqlStatement(database));
//        assertEquals("/*A Comment*/SELECT * FROM x",statements[1].getSqlStatement(database));
//    }
}
