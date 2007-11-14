package org.liquibase.intellij.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import liquibase.ChangeSet;
import liquibase.parser.MigratorSchemaResolver;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.liquibase.ide.common.ChangeLogWriter;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class IntellijChangeLogWriter implements ChangeLogWriter {

    public void appendChangeSet(final ChangeSet changeSet) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    DocumentBuilder documentBuilder = createDocumentBuilder();

                    VirtualFile file = LiquibaseProjectComponent.getInstance().getChangeLogVirtualFile();
                    if (file == null) {
                        throw new RuntimeException("Could not find file");
                    }
                    Document doc;
                    if (!file.isValid() || file.getLength() == 0) {
                        createEmptyChangeLog(new File(file.getUrl()));
                    }

                    doc = documentBuilder.parse(file.getInputStream());

                    doc.getDocumentElement().appendChild(changeSet.createNode(doc));

                    OutputStream outputStream = file.getOutputStream(null);

                    serializeXML(doc, outputStream);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void serializeXML(Document doc, OutputStream outputStream) throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(outputStream, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);
    }

    private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new MigratorSchemaResolver());
        return documentBuilder;
    }


    public void createEmptyChangeLog(File file) throws ParserConfigurationException, IOException {
        Document doc = createDocumentBuilder().newDocument();

        Element changeLogElement = doc.createElement("databaseChangeLog");
        changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/1.3");
        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/1.3 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.3.xsd");

        changeLogElement.appendChild(doc.createComment("Add change tags here"));
        doc.appendChild(changeLogElement);

        FileOutputStream outputStream = new FileOutputStream(file);
        serializeXML(doc, outputStream);
        outputStream.close();

        VirtualFileManager.getInstance().refresh(true);
    }
}
