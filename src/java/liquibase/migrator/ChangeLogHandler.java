package liquibase.migrator;

import liquibase.migrator.change.*;
import liquibase.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

public class ChangeLogHandler implements ContentHandler {
    private Migrator migrator;
    private DatabaseChangeLog changeLog;
    private AbstractChange change;
    private StringBuffer text;

    private ChangeSet changeSet;

    public ChangeLogHandler(Migrator migrator) {
        this.migrator = migrator;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if ("databaseChangeLog".equals(qName)) {
                changeLog = new DatabaseChangeLog(migrator);
            } else if ("include".equals(qName)) {
                new IncludeMigrator(atts.getValue("file"), migrator).migrate();
            } else if (changeSet == null && "changeSet".equals(qName)) {
                boolean alwaysRun = false;
                boolean runOnChange = false;
                if ("true".equalsIgnoreCase(atts.getValue("runAlways"))) {
                    alwaysRun = true;
                }
                if ("true".equalsIgnoreCase(atts.getValue("runOnChange"))) {
                    runOnChange = true;
                }
                changeSet = new ChangeSet(atts.getValue("id"), atts.getValue("author"), alwaysRun, runOnChange, changeLog);
            } else if (changeSet != null && change == null) {
                change = ChangeFactory.getInstance().create(qName);
                text = new StringBuffer();
                if (change == null) {
                    throw new MigrationFailedException("Unknown refactoring: " + qName);
                }
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(change, attributeName, attributeValue);
                }
            } else if (change != null && "column".equals(qName)) {
                ColumnConfig column = new ColumnConfig();
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(column, attributeName, attributeValue);
                }
                if (change instanceof AddColumnChange) {
                    ((AddColumnChange) change).setColumn(column);
                } else if (change instanceof CreateTableChange) {
                    ((CreateTableChange) change).addColumn(column);
                } else if (change instanceof InsertDataChange) {
                    ((InsertDataChange) change).addColumn(column);
                } else {
                    throw new RuntimeException("Unexpected column tag for "+change.getClass().getName());
                }
            } else if (change != null && "constraints".equals(qName)) {
                ConstraintsConfig constraints = new ConstraintsConfig();
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(constraints, attributeName, attributeValue);
                }
                ColumnConfig lastColumn;
                if (change instanceof AddColumnChange) {
                    lastColumn = ((AddColumnChange) change).getColumn();
                } else if (change instanceof CreateTableChange) {
                    lastColumn = ((CreateTableChange) change).getColumns().get(((CreateTableChange) change).getColumns().size() - 1);
                } else {
                    throw new RuntimeException("Unexpected change: "+change.getClass().getName());
                }
                lastColumn.setConstraints(constraints);
            } else {
                throw new MigrationFailedException("Unexpected tag: " + qName);
            }
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    private void setProperty(Object object, String attributeName, String attributeValue) throws IllegalAccessException, InvocationTargetException {
        String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
        Method[] methods = object.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(methodName)) {
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Boolean.class)) {
                    method.invoke(object, new Object[]{Boolean.valueOf(attributeValue)});
                    return;
                } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(String.class)) {
                    method.invoke(object, new Object[]{attributeValue.toString()});
                    return;
                }
            }
        }
        throw new RuntimeException("Property not found: "+attributeName);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (changeSet != null && "changeSet".equals(qName)) {
                changeSet.execute();
                changeSet = null;
            } else if (change != null && qName.equals(change.getTagName())) {
                if (text.length() > 0) {
                    if (change instanceof RawSQLChange) {
                        ((RawSQLChange) change).setSql(text.toString());
                    } else {
                        throw new RuntimeException("Unexpected text in "+change.getTagName());
                    }
                }
                text = null;
                changeSet.addRefactoring(change);
                change = null;
            }
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (text != null) {
            text.append(StringUtils.trimToEmpty(new String(ch, start, length)));
        }
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
}
