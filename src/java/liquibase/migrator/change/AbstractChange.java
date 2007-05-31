package liquibase.migrator.change;

import liquibase.util.StreamUtil;
import liquibase.database.*;
import liquibase.migrator.MD5Util;
import liquibase.migrator.Migrator;
import liquibase.migrator.RollbackImpossibleException;
import liquibase.migrator.UnsupportedChangeException;
import liquibase.util.StringUtils;
import liquibase.util.XMLUtil;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;

/**
 * Base class all changes (refactorings) implement.
 * <p>
 * <b>How changes are constructed and run when reading changelogs:</b>
 * <ol>
 *      <li>As the changelog handler gets to each element inside a changeSet, it passes the tag name to liquibase.migrator.change.ChangeFactory
 *      which looks through all the registered changes until it finds one with matching specified tag name</li>
 *      <li>The ChangeFactory then constructs a new instance of the change</li>
 *      <li>For each attribute in the XML node, reflection is used to call a corresponding set* method on the change class</li>
 *      <li>The correct generateStatements(*) method is called for the current database</li>
 * </ol>
 * <p>
 * <b>To implement a new change:</b>
 * <ol>
 *      <li>Create a new class that extends AbstractChange</li>
 *      <li>Implement the abstract generateStatements(*) methods which return the correct SQL calls for each database</li>
 *      <li>Implement the createMessage() method to create a descriptive message for logs and dialogs
 *      <li>Implement the createNode() method to generate an XML element based on the values in this change</li>
 *      <li>Add the new class to the liquibase.migrator.change.ChangeFactory</li>
 * </ol>
 * <p><b>Implementing automatic rollback support</b><br><br>
 * The easiest way to allow automatic rollback support is by overriding the createInverses() method.
 * If there are no corresponding inverse changes, you can override the generateRollbackStatements(*) and canRollBack() methods.
 * <p>
 * <b>Notes for generated SQL:</b><br>
 * Because migration and rollback scripts can be generated for execution at a different time, or against a different database,
 * changes you implement cannot directly reference data in the database.  For example, you cannot implement a change that selects
 * all rows from a database and modifies them based on the primary keys you find because when the SQL is actually run, those rows may not longer
 * exist and/or new rows may have been added.
 * <p>
 * We chose the name "change" over "refactoring" because changes will sometimes change functionality whereas true refactoring will not.
 *
 * @see liquibase.migrator.change.ChangeFactory
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
        AbstractChange[] inverses = createInverses();
        if (inverses == null) {
            throw new RollbackImpossibleException("No inverse to " + getClass().getName() + " created");
        }

        List<String> statements = new ArrayList<String>();

        for (AbstractChange inverse : inverses) {
            if (database instanceof MSSQLDatabase) {
                statements.addAll(Arrays.asList(inverse.generateStatements(((MSSQLDatabase) database))));
            } else if (database instanceof OracleDatabase) {
                statements.addAll(Arrays.asList(inverse.generateStatements(((OracleDatabase) database))));
            } else if (database instanceof MySQLDatabase) {
                statements.addAll(Arrays.asList(inverse.generateStatements(((MySQLDatabase) database))));
            } else if (database instanceof PostgresDatabase) {
                statements.addAll(Arrays.asList(inverse.generateStatements(((PostgresDatabase) database))));
            } else {
                throw new RuntimeException("Unknown database type: " + database.getClass().getName());
            }
        }

        return statements.toArray(new String[statements.size()]);
    }

    public boolean canRollBack() {
        return createInverses() != null;
    }

    public abstract String getConfirmationMessage();

    protected AbstractChange[] createInverses() {
        return null;
    }

    public String getRefactoringName() {
        return refactoringName;
    }

    public String getTagName() {
        return tagName;
    }

    public abstract Element createNode(Document currentChangeLogDOM);

    public void saveStatement(AbstractDatabase database, Writer writer) throws IOException, UnsupportedChangeException {
        String[] statements = generateStatements(database);
        for (String statement : statements) {
            writer.append(statement + ";" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
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
        for (Map.Entry entry : attributeMap.entrySet()) {
            buffer.append(" ").append(entry.getKey()).append("=\"").append(attributeMap.get(entry.getValue())).append("\"");
        }
        buffer.append(">").append(StringUtils.trimToEmpty(XMLUtil.getTextContent(node)));
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
