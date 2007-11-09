package org.liquibase.intellij.plugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import liquibase.ChangeSet;
import liquibase.parser.MigratorSchemaResolver;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.liquibase.ide.common.ChangeLogWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class IntellijChangeLogWriter implements ChangeLogWriter {

    public void appendChangeSet(final ChangeSet changeSet) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                    documentBuilder.setEntityResolver(new MigratorSchemaResolver());

                    VirtualFile file = LiquibaseProjectComponent.getInstance().getChangeLogVirtualFile();
                    if (file == null) {
                        throw new RuntimeException("Could not find file");
                    }
                    Document doc;
                    if (!file.isValid() || file.getLength() == 0) {
                        doc = documentBuilder.newDocument();

                        Element changeLogElement = doc.createElement("databaseChangeLog");
                        changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/1.3");
                        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/1.3 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.3.xsd");

                        doc.appendChild(changeLogElement);
                    } else {
                        doc = documentBuilder.parse(file.getInputStream());
                    }

                    doc.getDocumentElement().appendChild(changeSet.createNode(doc));

                    OutputFormat format = new OutputFormat(doc);
                    format.setIndenting(true);
                    XMLSerializer serializer = new XMLSerializer(file.getOutputStream(null), format);
                    serializer.asDOMSerializer();
                    serializer.serialize(doc);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
