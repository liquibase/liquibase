package liquibase.migrator.change;

import liquibase.database.AbstractDatabase;
import liquibase.database.struture.DatabaseStructure;
import liquibase.migrator.MD5Util;
import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
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

    public void executeStatement(AbstractDatabase database) throws SQLException {
        // Do processing
        String statement = generateStatement(database);
        Logger.getLogger(Migrator.DEFAULT_LOG_NAME).finest("Executing Statement: " + statement);
        try {
            Statement dbStatement = database.getConnection().createStatement();
            dbStatement.execute(statement);
            dbStatement.close();
        } catch (SQLException e) {
            throw new SQLException((e.getMessage() + " [" + statement + "]").replaceAll("\n", "").replaceAll("\r", ""));
        }
    }

    public abstract String generateStatement(AbstractDatabase database);

    public abstract String getConfirmationMessage();

    public abstract boolean isApplicableTo(Set<DatabaseStructure> selectedDatabaseStructures);

    public String getRefactoringName() {
        return refactoringName;
    }

    public String getTagName() {
        return tagName;
    }

    public abstract Element createNode(Document currentMigrationFileDOM);

    public void saveStatement(AbstractDatabase database, Writer writer) throws IOException {
        writer.append(generateStatement(database) + ";\n\n");
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
        for (int i=0; i<attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            attributeMap.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        for (String key : attributeMap.keySet()) {
            buffer.append(" ").append(key).append("=\"").append(attributeMap.get(key)).append("\"");
        }
        buffer.append(">").append(StringUtils.trimToEmpty(node.getTextContent()));
        NodeList childNodes = node.getChildNodes();
        for (int i=0; i<childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                nodeToStringBuffer(((Element) childNode), buffer);
            }
        }
        buffer.append("</").append(node.getNodeName()).append(">");
    }
}
