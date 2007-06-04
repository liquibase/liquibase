package liquibase.migrator.parser;

import liquibase.migrator.change.*;
import liquibase.migrator.preconditions.*;
import liquibase.migrator.*;
import liquibase.util.StringUtils;
import liquibase.util.StreamUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base SAX Handler for all modes of reading change logs.  This class is subclassed depending on
 * how the change log should be read (for migration, for rollback, etc).
 */
public abstract class BaseChangeLogHandler extends DefaultHandler {

    protected Migrator migrator;
    protected Logger log;

    private DatabaseChangeLog changeLog;
    private AbstractChange change;
    private StringBuffer text;
    private PreconditionSet precondition;
    private ChangeSet changeSet;
    private OrPrecondition orprecondition;
    private NotPrecondition notprecondition;
    private RunningAsPrecondition runningAs;
    private String physicalChangeLogLocation;


    protected BaseChangeLogHandler(Migrator migrator, String physicalChangeLogLocation) {
        this.migrator = migrator;
        this.physicalChangeLogLocation = physicalChangeLogLocation;
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);

    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if ("comment".equals(qName)) {
                text = new StringBuffer();
            } else if ("databaseChangeLog".equals(qName)) {
                changeLog = new DatabaseChangeLog(migrator, physicalChangeLogLocation);
                changeLog.setLogicalFilePath(atts.getValue("logicalFilePath"));
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
                changeSet = new ChangeSet(atts.getValue("id"), atts.getValue("author"), alwaysRun, runOnChange, changeLog, atts.getValue("context"));
            } else if (changeSet != null && "rollback".equals(qName)) {
                text = new StringBuffer();
            } else if (changeSet != null && change == null) {
                change = migrator.getChangeFactory().create(qName);
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
                precondition = new PreconditionSet(migrator);
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
                precondition.checkConditions();
            } else if (precondition != null && "or".equals(qName) && notprecondition == null) {
                precondition.setOrPreCondition(orprecondition);
            } else if (precondition != null && "not".equals(qName)) {
                precondition.setNotPreCondition(notprecondition);
            } else if (precondition != null && "runningAs".equals(qName)) {
                precondition.setRunningAs(runningAs);
            } else if (changeSet != null && "rollback".equals(qName)) {
                changeSet.setRollBackSQL(textString);
            } else if (changeSet != null && "comment".equals(qName)) {
                changeSet.setComments(textString);
            } else if (changeSet != null && "changeSet".equals(qName)) {
                handleChangeSet(changeSet);
                changeSet = null;
            } else if (change != null && qName.equals(change.getTagName())) {
                if (textString != null) {
                    if (change instanceof RawSQLChange) {
                        ((RawSQLChange) change).setSql(textString);
                    } else if (change instanceof CreateViewChange) {
                        ((CreateViewChange) change).setSelectQuery(textString);
                    } else {
                        throw new RuntimeException("Unexpected text in " + change.getTagName());
                    }
                }
                text = null;
                changeSet.addRefactoring(change);
                change = null;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error thrown as a SAXException: " + e.getMessage(), e);
            throw new SAXException(e);
        }
    }

    protected abstract void handleChangeSet(ChangeSet changeSet) throws SQLException, DatabaseHistoryException, MigrationFailedException, PreconditionFailedException, IOException;

    public void characters(char ch[], int start, int length) throws SAXException {
        if (text != null) {
            text.append(new String(ch, start, length));
        }
    }

    protected String escapeStringForDatabase(String string) {
        return string.replaceAll("'", "''");
    }

    protected void removeRanStatus(ChangeSet changeSet) throws SQLException, IOException {
        Migrator migrator = changeSet.getDatabaseChangeLog().getMigrator();
        String sql = "DELETE FROM DATABASECHANGELOG WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getDatabaseChangeLog().getFilePath()));

        Writer sqlOutputWriter = migrator.getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = migrator.getDatabase().getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
        } else {
            sqlOutputWriter.write(sql + ";" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
        }
    }
}
