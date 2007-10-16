package liquibase;

import liquibase.migrator.Migrator;
import liquibase.parser.MigratorSchemaResolver;
import liquibase.preconditions.AndPrecondition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog implements Comparable<DatabaseChangeLog> {
    private Migrator migrator;
    private AndPrecondition preconditions;
    private String physicalFilePath;
    private String logicalFilePath;

    public DatabaseChangeLog(Migrator migrator, String physicalFilePath) {
        this.migrator = migrator;
        this.physicalFilePath = physicalFilePath;
    }

    public Migrator getMigrator() {
        return migrator;
    }

    public AndPrecondition getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(AndPrecondition precond) {
        preconditions = precond;
    }

    public String getPhysicalFilePath() {
        return physicalFilePath;
    }

    public void setPhysicalFilePath(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public String getLogicalFilePath() {
        return logicalFilePath;
    }

    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return logicalFilePath;
        }
    }

    public String toString() {
        return getFilePath();
    }

    public int compareTo(DatabaseChangeLog o) {
        return getFilePath().compareTo(o.getFilePath());
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseChangeLog that = (DatabaseChangeLog) o;

        return getFilePath().equals(that.getFilePath());

    }

    public int hashCode() {
        return getFilePath().hashCode();
    }

    public Document toDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new MigratorSchemaResolver());

        Document doc = documentBuilder.newDocument();

        Element changeLogElement = doc.createElement("databaseChangeLog");
        changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/1.2");
        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/1.2 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.2.xsd");

        doc.appendChild(changeLogElement);

        return doc;
    }
}
