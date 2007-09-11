package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.ChangeSet;
import liquibase.FileOpener;
import liquibase.migrator.Migrator;
import liquibase.exception.JDBCException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.MD5Util;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;

/**
 * Standard superclass for Changes to implement. This is a <i>skeletal implementation</i>,
 * as defined in Effective Java#16.
 *
 * @see Change
 */
public abstract class AbstractChange implements Change {

    /*
     * The name and the tag name of the change.
     * Defined as private members, so they can
     * only be accessed through accessor methods
     * by its subclasses
     */
    private final String changeName;
    private final String tagName;
    private FileOpener fileOpener;

    private ChangeSet changeSet;

    /**
     * Constructor with tag name and name
     *
     * @param tagName the tag name for this change
     * @param changeName the name for this change
     */
    protected AbstractChange(String tagName, String changeName) {
        this.tagName = tagName;
        this.changeName = changeName;
    }

    //~ ------------------------------------------------------------------------------- public interface

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    /**
     * @see liquibase.change.Change#getChangeName()
     */
    public String getChangeName() {
        return changeName;
    }

    /**
     * @see liquibase.change.Change#getTagName()
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @see liquibase.change.Change#executeStatements(liquibase.database.Database)
     */
    public void executeStatements(Database database) throws JDBCException, UnsupportedChangeException {
        SqlStatement[] statements = generateStatements(database);

        execute(statements, database);
    }

    /**
     * @see liquibase.change.Change#saveStatements(liquibase.database.Database, java.io.Writer)
     */
    public void saveStatements(Database database, Writer writer) throws IOException, UnsupportedChangeException {
        SqlStatement[] statements = generateStatements(database);
        for (SqlStatement statement : statements) {
            writer.append(statement.getSqlStatement(database)).append(";").append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());
        }
    }

    /**
     * @see liquibase.change.Change#executeRollbackStatements(liquibase.database.Database)
     */
    public void executeRollbackStatements(Database database) throws JDBCException, UnsupportedChangeException, RollbackImpossibleException {
        SqlStatement[] statements = generateRollbackStatements(database);
        execute(statements, database);
    }

    /**
     * @see liquibase.change.Change#saveRollbackStatement(liquibase.database.Database, java.io.Writer)
     */
    public void saveRollbackStatement(Database database, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException {
        SqlStatement[] statements = generateRollbackStatements(database);
        for (SqlStatement statement : statements) {
            writer.append(statement.getSqlStatement(database)).append(";\n\n");
        }
    }

    /*
     * Skipped by this skeletal implementation
     *
     * @see liquibase.change.Change#generateStatements(liquibase.database.Database)
     */

    /**
     * @see liquibase.change.Change#generateRollbackStatements(liquibase.database.Database)
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        return generateRollbackStatementsFromInverse(database);
    }

    /**
     * @see liquibase.change.Change#canRollBack()
     */
    public boolean canRollBack() {
        return createInverses() != null;
    }

    /*
     * Skipped by this skeletal implementation
     *
     * @see liquibase.change.Change#getConfirmationMessage()
     */

    /*
     * Skipped by this skeletal implementation
     *
     * @see liquibase.change.Change#createNode(org.w3c.dom.Document)
     */

    /**
     * @see liquibase.change.Change#getMD5Sum()
     */
    public String getMD5Sum() {
        try {
            StringBuffer buffer = new StringBuffer();
            nodeToStringBuffer(createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()), buffer);
            return MD5Util.computeMD5(buffer.toString());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    //~ ------------------------------------------------------------------------------- private methods
    /*
     * Generates rollback statements from the inverse changes returned by createInverses()
     *
     * @param database the target {@link Database} associated to this change's rollback statements
     * @return an array of {@link String}s containing the rollback statements from the inverse changes
     * @throws UnsupportedChangeException if this change is not supported by the {@link Database} passed as argument
     * @throws RollbackImpossibleException if rollback is not supported for this change
     */
    private SqlStatement[] generateRollbackStatementsFromInverse(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        Change[] inverses = createInverses();
        if (inverses == null) {
            throw new RollbackImpossibleException("No inverse to " + getClass().getName() + " created");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        for (Change inverse : inverses) {
            statements.addAll(Arrays.asList(inverse.generateStatements(database)));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    /*
     * Create inverse changes that can roll back this change. This method is intended
     * to be overriden by the subclasses that can create inverses.
     *
     * @return an array of {@link Change}s containing the inverse
     *         changes that can roll back this change
     */
    protected Change[] createInverses() {
        return null;
    }

    /*
     * Creates a {@link String} using the XML element representation of this
     * change
     *
     * @param node the {@link Element} associated to this change
     * @param buffer a {@link StringBuffer} object used to hold the {@link String}
     *               representation of the change
     */
    private void nodeToStringBuffer(Element node, StringBuffer buffer) {
        buffer.append("<").append(node.getNodeName());
        SortedMap<String, String> attributeMap = new TreeMap<String, String>();
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            attributeMap.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        for (Map.Entry entry : attributeMap.entrySet()) {
            String value = (String) entry.getValue();
            if (value != null) {
                buffer.append(" ").append(entry.getKey()).append("=\"").append(attributeMap.get(value)).append("\"");
            }
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

    /*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws JDBCException if there were problems issuing the statements
     */
    private void execute(SqlStatement[] statements, Database database) throws JDBCException {
        for (SqlStatement statement : statements) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).finest("Executing Statement: " + statement);
            try {
                Statement dbStatement = database.getConnection().createStatement();
                dbStatement.execute(statement.getSqlStatement(database));
                dbStatement.close();
            } catch (SQLException e) {
                throw new JDBCException((e.getMessage() + " [" + statement.getSqlStatement(database) + "]").replaceAll("\n", "").replaceAll("\r", ""));
            }
        }
    }
    
    /**
     * Default implementation that stores the file opener provided when the
     * Change was created.
     */
    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }
    
    /**
     * Returns the FileOpen as provided by the creating ChangeLog.
     * 
     * @return The file opener
     */
    public FileOpener getFileOpener() {
        return fileOpener;
    }
    
    /**
     * Most Changes don't need to do any setup.
     * This implements a no-op
     */
    public void setUp() throws SetupException {
        
    }
}
