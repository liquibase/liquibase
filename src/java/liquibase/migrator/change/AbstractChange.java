package liquibase.migrator.change;

import liquibase.database.*;
import liquibase.migrator.MD5Util;
import liquibase.migrator.Migrator;
import liquibase.migrator.UnsupportedChangeException;
import liquibase.migrator.RollbackImpossibleException;
import liquibase.util.StringUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * This is an abstract class, other concrete classes extend this class to provide
 * the implementation of the executeStatement according to their usage. This class
 * knows about which database the statements should be executed against.
 */
public abstract class AbstractChange {

    private String refactoringName;
    private String tagName;

    protected AbstractChange(String tagName, String refactoringName) {
        this.tagName = tagName;
        this.refactoringName = refactoringName;
    }

    public void executeStatements(AbstractDatabase database) throws SQLException, UnsupportedChangeException {
        String[] statements = generateStatements(database);

        for (String statement : statements) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).finest("Executing Statement: " + statement);
            try {
                Statement dbStatement = database.getConnection().createStatement();
                dbStatement.execute(statement);
                dbStatement.close();
            } catch (SQLException e) {
                throw new SQLException((e.getMessage() + " [" + statement + "]").replaceAll("\n", "").replaceAll("\r", ""));
            }
        }
    }

    public void executeRollbackStatements(AbstractDatabase database) throws SQLException, UnsupportedChangeException, RollbackImpossibleException {
        String[] statements = generateRollbackStatements(database);

        for (String statement : statements) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).finest("Executing Statement: " + statement);
            try {
                Statement dbStatement = database.getConnection().createStatement();
                dbStatement.execute(statement);
                dbStatement.close();
            } catch (SQLException e) {
                throw new SQLException((e.getMessage() + " [" + statement + "]").replaceAll("\n", "").replaceAll("\r", ""));
            }
        }
    }


    public final String[] generateStatements(AbstractDatabase database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return generateStatements(((MSSQLDatabase) database));
        } else if (database instanceof OracleDatabase) {
            return generateStatements(((OracleDatabase) database));
        } else if (database instanceof MySQLDatabase) {
            return generateStatements(((MySQLDatabase) database));
        } else if (database instanceof PostgresDatabase) {
            return generateStatements(((PostgresDatabase) database));
        } else {
            throw new RuntimeException("Unknown database type: " + database.getClass().getName());
        }
    }

    private String[] generateRollbackStatements(AbstractDatabase database) throws UnsupportedChangeException, RollbackImpossibleException {
        if (database instanceof MSSQLDatabase) {
            return generateRollbackStatements(((MSSQLDatabase) database));
        } else if (database instanceof OracleDatabase) {
            return generateRollbackStatements(((OracleDatabase) database));
        } else if (database instanceof MySQLDatabase) {
            return generateRollbackStatements(((MySQLDatabase) database));
        } else if (database instanceof PostgresDatabase) {
            return generateRollbackStatements(((PostgresDatabase) database));
        } else {
            throw new RuntimeException("Unknown database type: " + database.getClass().getName());
        }
    }

    public abstract String[] generateStatements(MSSQLDatabase database) throws UnsupportedChangeException;

    public abstract String[] generateStatements(OracleDatabase database) throws UnsupportedChangeException;

    public abstract String[] generateStatements(MySQLDatabase database) throws UnsupportedChangeException;

    public abstract String[] generateStatements(PostgresDatabase database) throws UnsupportedChangeException;

    public String[] generateRollbackStatements(MSSQLDatabase database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    public String[] generateRollbackStatements(OracleDatabase database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    public String[] generateRollbackStatements(MySQLDatabase database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    public String[] generateRollbackStatements(PostgresDatabase database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    protected String[] generateRollbackStatementsFromInverse(AbstractDatabase database) throws UnsupportedChangeException, RollbackImpossibleException {
        AbstractChange inverse = createInverse();
        if (inverse == null) {
            throw new RollbackImpossibleException("No inverse to "+getClass().getName()+" created");
        } else if (database instanceof MSSQLDatabase) {
            return inverse.generateStatements(((MSSQLDatabase) database));
        } else if (database instanceof OracleDatabase) {
            return inverse.generateStatements(((OracleDatabase) database));
        } else if (database instanceof MySQLDatabase) {
            return inverse.generateStatements(((MySQLDatabase) database));
        } else if (database instanceof PostgresDatabase) {
            return inverse.generateStatements(((PostgresDatabase) database));
        } else {
            throw new RuntimeException("Unknown database type: " + database.getClass().getName());
        }
    }

    public boolean canRollBack() {
        return createInverse() != null;
    }

    public abstract String getConfirmationMessage();

    protected AbstractChange createInverse() {
        return null;
    }

    public String getRefactoringName() {
        return refactoringName;
    }

    public String getTagName() {
        return tagName;
    }

    public abstract Element createNode(Document currentMigrationFileDOM);

    public void saveStatement(AbstractDatabase database, Writer writer) throws IOException, UnsupportedChangeException {
        String[] statements = generateStatements(database);
        for (String statement : statements) {
            writer.append(statement + ";\n\n");
        }
    }

    public void saveRollbackStatement(AbstractDatabase database, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException {
        String[] statements = generateRollbackStatements(database);
        for (String statement : statements) {
            writer.append(statement + ";\n\n");
        }
    }


    public String getMD5Sum() {
        try {
            StringBuffer buffer = new StringBuffer();
            nodeToStringBuffer(createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), buffer);
            return MD5Util.computeMD5(buffer.toString());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void nodeToStringBuffer(Element node, StringBuffer buffer) {
        buffer.append("<").append(node.getNodeName());
        SortedMap<String, String> attributeMap = new TreeMap<String, String>();
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            attributeMap.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        for (String key : attributeMap.keySet()) {
            buffer.append(" ").append(key).append("=\"").append(attributeMap.get(key)).append("\"");
        }
        buffer.append(">").append(StringUtils.trimToEmpty(node.getTextContent()));
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                nodeToStringBuffer(((Element) childNode), buffer);
            }
        }
        buffer.append("</").append(node.getNodeName()).append(">");
    }
}
