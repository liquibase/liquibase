package org.liquibase.intellij.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import liquibase.parser.LiquibaseSchemaResolver;
import liquibase.parser.ChangeLogSerializer;
import liquibase.parser.xml.XMLChangeLogParser;
import liquibase.ChangeSet;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.liquibase.ide.common.ChangeLogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;

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
                        createEmptyChangeLog(file.getOutputStream(null));
                    }

                    doc = documentBuilder.parse(file.getInputStream());

                    doc.getDocumentElement().appendChild(new ChangeLogSerializer(doc).createNode(changeSet));

                    OutputStream outputStream = file.getOutputStream(null);

                    serializeXML(doc, outputStream);

                    outputStream.flush();
                    outputStream.close();

                    file.refresh(false, false);

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
        documentBuilder.setEntityResolver(new LiquibaseSchemaResolver());
        return documentBuilder;
    }

    public void createEmptyChangeLog(final String changeLogFile) throws ParserConfigurationException, IOException {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    if (!new File(changeLogFile).createNewFile()) {
                        throw new RuntimeException("Could not create file");
                    }
                    VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
                    VirtualFile file = virtualFileManager.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, changeLogFile));
                    if (file == null) {
                        throw new RuntimeException("Could not find file");
                    }

                    createEmptyChangeLog(file.getOutputStream(null));

                    file.refresh(false, false);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public void createEmptyChangeLog(OutputStream outputStream) throws ParserConfigurationException, IOException {
        Document doc = createDocumentBuilder().newDocument();

        Element changeLogElement = doc.createElement("databaseChangeLog");
        changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/"+ XMLChangeLogParser.getSchemaVersion());
        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/"+XMLChangeLogParser.getSchemaVersion()+" http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-"+XMLChangeLogParser.getSchemaVersion()+".xsd");

        changeLogElement.appendChild(doc.createComment("Add change tags here"));
        doc.appendChild(changeLogElement);

        serializeXML(doc, outputStream);
        outputStream.flush();
        outputStream.close();

        VirtualFileManager.getInstance().refresh(true);
    }
}
