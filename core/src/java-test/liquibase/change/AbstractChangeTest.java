package liquibase.change;

import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.visitor.SqlVisitor;
import liquibase.database.structure.DatabaseObject;
import liquibase.util.StreamUtil;
import liquibase.exception.InvalidChangeDefinitionException;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Set;
import java.util.ArrayList;

/**
 * Base test class for changes
 */
public abstract class AbstractChangeTest {

    @Test
    public abstract void getRefactoringName() throws Exception;

    @Test
    public abstract void generateStatement() throws Exception;

    @Test
    public abstract void getConfirmationMessage() throws Exception;

    @Test
    public void saveStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactoring") {
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{new RawSqlStatement("GENERATED STATEMENT")};
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

            public void validate(Database database) throws InvalidChangeDefinitionException {

            }
        };

        StringWriter stringWriter = new StringWriter();

        OracleDatabase database = new OracleDatabase();
        database.saveStatements(change, new ArrayList<SqlVisitor>(), stringWriter);

        assertEquals("GENERATED STATEMENT;" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator(), stringWriter.getBuffer().toString());
    }

    @Test
    public void executeStatement() throws Exception {
        Change change = new AbstractChange("test", "Test Refactorign") {
            public SqlStatement[] generateStatements(Database database) {
                return new SqlStatement[]{new RawSqlStatement("GENERATED STATEMENT;")};
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

            public void validate(Database database) throws InvalidChangeDefinitionException {

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

        database.executeStatements(change, new ArrayList<SqlVisitor>());
        
        verify(conn);
        verify(statement);
    }
}