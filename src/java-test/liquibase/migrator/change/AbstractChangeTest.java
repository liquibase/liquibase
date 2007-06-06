package liquibase.migrator.change;

import junit.framework.TestCase;
import liquibase.util.StreamUtil;
import liquibase.database.*;
import liquibase.migrator.UnsupportedChangeException;
import static org.easymock.classextension.EasyMock.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Statement;

public abstract class AbstractChangeTest extends TestCase {

    public abstract void testGetRefactoringName() throws Exception;

    public abstract void testGenerateStatement() throws Exception;

    public abstract void testGetConfirmationMessage() throws Exception;

    public abstract void testCreateNode() throws Exception;

    public void testSaveStatement() throws Exception {
        AbstractChange change = new AbstractChange("test", "Test Refactoring") {
            public String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
                return new String[]{"GENERATED STATEMENT"};
            }

            public String getConfirmationMessage() {
                return null;
            }

            public Element createNode(Document changeLogFileDOM) {
                return null;
            }
        };

        StringWriter stringWriter = new StringWriter();

        OracleDatabase database = new OracleDatabase();
        change.saveStatements(database, stringWriter);

        assertEquals("GENERATED STATEMENT;" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator(), stringWriter.getBuffer().toString());
    }

    public void testExecuteStatement() throws Exception {
        AbstractChange change = new AbstractChange("test", "Test Refactorign") {
            public String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
                return new String[]{"GENERATED STATEMENT;"};
            }

            public String getConfirmationMessage() {
                return null;
            }

            public Element createNode(Document changeLogFileDOM) {
                return null;
            }
        };

        Connection conn = createMock(Connection.class);
        Statement statement = createMock(Statement.class);
        expect(conn.createStatement()).andReturn(statement);
        expect(statement.execute("GENERATED STATEMENT;")).andStubReturn(true);
        statement.close();
        expectLastCall();
        replay(conn);
        replay(statement);

        OracleDatabase database = new OracleDatabase();
        database.setConnection(conn);

        change.executeStatements(database);
    }
}