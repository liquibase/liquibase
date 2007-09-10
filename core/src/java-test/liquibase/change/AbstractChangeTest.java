package liquibase.change;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.structure.DatabaseObject;
import liquibase.util.StreamUtil;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;

/**
 * Base test class for changes
 */
public abstract class AbstractChangeTest {

    public abstract void getRefactoringName() throws Exception;

    public abstract void generateStatement() throws Exception;

    public abstract void getConfirmationMessage() throws Exception;

    public abstract void createNode() throws Exception;

    @Test
    public void saveStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactoring") {
            public String[] generateStatements(Database database) {
                return new String[]{"GENERATED STATEMENT"};
            }

            public String getConfirmationMessage() {
                return null;
            }

            public Element createNode(Document changeLogFileDOM) {
                return null;
            }


            public Set<DatabaseObject> getAffectedDatabaseObjects() {
                return null;
            }
        };

        StringWriter stringWriter = new StringWriter();

        OracleDatabase database = new OracleDatabase();
        change.saveStatements(database, stringWriter);

        assertEquals("GENERATED STATEMENT;" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator(), stringWriter.getBuffer().toString());
    }

    @Test
    public void executeStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactorign") {
            public String[] generateStatements(Database database) {
                return new String[]{"GENERATED STATEMENT;"};
            }

            public String getConfirmationMessage() {
                return null;
            }

            public Element createNode(Document changeLogFileDOM) {
                return null;
            }


            public Set<DatabaseObject> getAffectedDatabaseObjects() {
                return null;
            }
        };

        Connection conn = createMock(Connection.class);
        Statement statement = createMock(Statement.class);
        conn.setAutoCommit(false);
        expect(conn.createStatement()).andReturn(statement);
        
        expect(statement.execute("GENERATED STATEMENT;")).andStubReturn(true);
        statement.close();
        expectLastCall();
        replay(conn);
        replay(statement);

        OracleDatabase database = new OracleDatabase();
        database.setConnection(conn);

        change.executeStatements(database);
        
        verify(conn);
        verify(statement);
    }
}