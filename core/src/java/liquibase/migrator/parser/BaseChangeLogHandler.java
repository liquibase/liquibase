package liquibase.migrator.parser;

import liquibase.migrator.*;
import liquibase.migrator.change.*;
import liquibase.migrator.exception.DatabaseHistoryException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.LiquibaseException;
import liquibase.migrator.preconditions.*;
import liquibase.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base SAX Handler for all modes of reading change logs.  This class is subclassed depending on
 * how the change log should be read (for migration, for rollback, etc).
 */
@SuppressWarnings({"AbstractClassExtendsConcreteClass"})
public abstract class BaseChangeLogHandler extends DefaultHandler {

    protected Migrator migrator;
    protected Logger log;

    private DatabaseChangeLog changeLog;
    private Change change;
    private StringBuffer text;
    private PreconditionSet precondition;
    private ChangeSet changeSet;
    private OrPrecondition orprecondition;
    private NotPrecondition notprecondition;
    private RunningAsPrecondition runningAs;
    private String physicalChangeLogLocation;
    private FileOpener fileOpener;


    protected BaseChangeLogHandler(Migrator migrator, String physicalChangeLogLocation,FileOpener fileOpener) {
        this.migrator = migrator;
        this.physicalChangeLogLocation = physicalChangeLogLocation;
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
        this.fileOpener = fileOpener;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if ("comment".equals(qName)) {
                text = new StringBuffer();
            } else if ("databaseChangeLog".equals(qName)) {
                changeLog = new DatabaseChangeLog(migrator, physicalChangeLogLocation);
                changeLog.setLogicalFilePath(atts.getValue("logicalFilePath"));
            } else if ("include".equals(qName)) {
                String fileName = atts.getValue("file");
                handleIncludedChangeLog(fileName);
            } else if (changeSet == null && "changeSet".equals(qName)) {
                boolean alwaysRun = false;
                boolean runOnChange = false;
                if ("true".equalsIgnoreCase(atts.getValue("runAlways"))) {
                    alwaysRun = true;
                }
                if ("true".equalsIgnoreCase(atts.getValue("runOnChange"))) {
                    runOnChange = true;
                }
                changeSet = new ChangeSet(atts.getValue("id"), atts.getValue("author"), alwaysRun, runOnChange, changeLog, atts.getValue("context"), atts.getValue("dbms"));
            } else if (changeSet != null && "rollback".equals(qName)) {
                text = new StringBuffer();
            } else if (changeSet != null && change == null) {
                change = migrator.getChangeFactory().create(qName);
                text = new StringBuffer();
                if (change == null) {
                    throw new MigrationFailedException(changeSet, "Unknown change: " + qName);
                }
                change.setFileOpener(fileOpener);
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(change, attributeName, attributeValue);
                }
                change.setUp();
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
                } else if (change instanceof CreateIndexChange) {
                    ((CreateIndexChange) change).addColumn(column);
                } else if (change instanceof ModifyColumnChange) {
                    ((ModifyColumnChange) change).setColumn(column);
                } else {
                    throw new RuntimeException("Unexpected column tag for " + change.getClass().getName());
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
                    throw new RuntimeException("Unexpected change: " + change.getClass().getName());
                }
                lastColumn.setConstraints(constraints);
            } else if ("preConditions".equals(qName)) {
//                System.out.println(migrator);
                precondition = new PreconditionSet(migrator, changeLog);
                //System.out.println("pre condition is true");

            } else if ("dbms".equals(qName)) {
                if (precondition != null) {
                    DBMSPrecondition dbmsPrecondition = new DBMSPrecondition();
//                    System.out.println("dbms is true");
                    for (int i = 0; i < atts.getLength(); i++) {
                        String attributeName = atts.getQName(i);
                        String attributeValue = atts.getValue(i);
                        setProperty(dbmsPrecondition, attributeName, attributeValue);
                    }
//                    System.out.println("attributes added");
                    if (orprecondition != null) {
//                        System.out.println("orrprecondition");
                        orprecondition.addDbms(dbmsPrecondition);
                    } else if (notprecondition != null) {
                        notprecondition.addDbms(dbmsPrecondition);
                    } else {
                        precondition.addDbms(dbmsPrecondition);
                    }
                } else {
                    throw new RuntimeException("Unexpected Dbms tag");
                }
            } else if ("or".equals(qName)) {
                if (precondition != null) {
                    orprecondition = new OrPrecondition();
                    if (notprecondition != null) {
                        notprecondition.setOrprecondition(orprecondition);
                    }
                } else {
                    throw new RuntimeException("Unexpected Or tag");
                }

            } else if ("not".equals(qName)) {
                if (precondition != null) {
                    notprecondition = new NotPrecondition();
                } else {
                    throw new RuntimeException("Unexpected Or tag");
                }

            } else if ("runningAs".equals(qName)) {
                if (precondition != null) {
                    runningAs = new RunningAsPrecondition();
                    for (int i = 0; i < atts.getLength(); i++) {
                        String attributeName = atts.getQName(i);
                        String attributeValue = atts.getValue(i);
                        setProperty(runningAs, attributeName, attributeValue);
                    }
                } else {
                    throw new RuntimeException("Unexpected Or tag");
                }

            } else {
                throw new MigrationFailedException(changeSet, "Unexpected tag: " + qName);
            }
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    protected void handleIncludedChangeLog(String fileName) throws LiquibaseException, IOException {
        new IncludeMigrator(fileName, migrator).migrate();
    }

    private void setProperty(Object object, String attributeName, String attributeValue) throws IllegalAccessException, InvocationTargetException {
        String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Boolean.class)) {
                    method.invoke(object, Boolean.valueOf(attributeValue));
                    return;
                } else
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(String.class)) {
                    method.invoke(object, attributeValue);
                    return;
                } else
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Integer.class)) {
                    method.invoke(object, Integer.valueOf(attributeValue));
                    return;
                }
            }
        }
        throw new RuntimeException("Property not found: " + attributeName);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String textString = null;
        if (text != null && text.length() > 0) {
            textString = StringUtils.trimToNull(text.toString());
        }

        try {
            if (precondition != null && "preConditions".equals(qName)) {
                changeLog.setPreconditions(precondition);
                handlePreCondition(precondition);
            } else if (precondition != null && "or".equals(qName) && notprecondition == null) {
                precondition.setOrPreCondition(orprecondition);
            } else if (precondition != null && "not".equals(qName)) {
                precondition.setNotPreCondition(notprecondition);
            } else if (precondition != null && "runningAs".equals(qName)) {
                precondition.setRunningAs(runningAs);
            } else if (changeSet != null && "rollback".equals(qName)) {
                changeSet.setRollBackSQL(textString);
            } else if (change != null && change instanceof RawSQLChange && "comment".equals(qName)) {
                ((RawSQLChange) change).setComments(textString);
                text = new StringBuffer();
            } else if (changeSet != null && "comment".equals(qName)) {
                changeSet.setComments(textString);
                text = new StringBuffer();
            } else if (changeSet != null && "changeSet".equals(qName)) {
                handleChangeSet(changeSet);
                changeSet = null;
            } else if (change != null && qName.equals(change.getTagName())) {
                if (textString != null) {
                    if (change instanceof RawSQLChange) {
                        ((RawSQLChange) change).setSql(textString);
                    } else if (change instanceof CreateViewChange) {
                        ((CreateViewChange) change).setSelectQuery(textString);
                    } else if (change instanceof InsertDataChange) {
                        List<ColumnConfig> columns = ((InsertDataChange) change).getColumns();
                        columns.get(columns.size()-1).setValue(textString);                        
                    } else {
                        throw new RuntimeException("Unexpected text in " + change.getTagName());
                    }
                }
                text = null;
                changeSet.addChange(change);
                change = null;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error thrown as a SAXException: " + e.getMessage(), e);
            throw new SAXException(changeLog.getPhysicalFilePath()+": "+e.getMessage(), e);
        }
    }

    /**
     * By defaultd does nothing.  Overridden in ValidatChangeLogHandler and anywhere else that is interested in them.
     */
    protected void handlePreCondition(@SuppressWarnings("unused") PreconditionSet preconditions) {        
    }

    protected abstract void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException;

    public void characters(char ch[], int start, int length) throws SAXException {
        if (text != null) {
            text.append(new String(ch, start, length));
        }
    }
}
