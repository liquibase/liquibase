package liquibase.parser;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.FileOpener;
import liquibase.change.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.exception.*;
import liquibase.migrator.IncludeMigrator;
import liquibase.migrator.Migrator;
import liquibase.preconditions.AndPrecondition;
import liquibase.preconditions.Precondition;
import liquibase.preconditions.PreconditionLogic;
import liquibase.preconditions.SqlPrecondition;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Stack;
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
    private AndPrecondition rootPrecondition;
    private Stack<PreconditionLogic> preconditionLogicStack = new Stack<PreconditionLogic>();
    private ChangeSet changeSet;
    protected String physicalChangeLogLocation;
    private FileOpener fileOpener;
    private Precondition currentPrecondition;


    protected BaseChangeLogHandler(Migrator migrator, String physicalChangeLogLocation, FileOpener fileOpener) {
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
                change.setChangeSet(changeSet);
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
                rootPrecondition = new AndPrecondition();
                preconditionLogicStack.push(rootPrecondition);
                //System.out.println("pre condition is true");

            } else if (rootPrecondition != null) {
                currentPrecondition = migrator.getPreconditionFactory().create(qName);

                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(currentPrecondition, attributeName, attributeValue);
                }
                preconditionLogicStack.peek().addNestedPrecondition(currentPrecondition);

                if (currentPrecondition instanceof PreconditionLogic) {
                    preconditionLogicStack.push(((PreconditionLogic) currentPrecondition));
                }

                if ("sqlCheck".equals(qName)) {
                    text = new StringBuffer();
                }
            } else if ("param".equals(qName)) {
                if (change instanceof CustomChangeWrapper) {
                    ((CustomChangeWrapper) change).setParam(atts.getValue("name"), atts.getValue("value"));
                } else {
                    throw new MigrationFailedException(changeSet, "'param' unexpected in " + qName);
                }
            } else if (change instanceof ExecuteShellCommandChange && "arg".equals(qName)) {
                ((ExecuteShellCommandChange) change).addArg(atts.getValue("value"));
            } else {
                throw new MigrationFailedException(changeSet, "Unexpected tag: " + qName);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error thrown as a SAXException: " + e.getMessage(), e);
            throw new SAXException(e);
        }
    }

    protected void handleIncludedChangeLog(String fileName) throws LiquibaseException, IOException {
        new IncludeMigrator(fileName, migrator).migrate();
    }

    private void setProperty(Object object, String attributeName, String attributeValue) throws IllegalAccessException, InvocationTargetException, CustomChangeException {
        if (object instanceof CustomChangeWrapper) {
            if (attributeName.equals("class")) {
                ((CustomChangeWrapper) object).setClass(attributeValue);
            } else {
                ((CustomChangeWrapper) object).setParam(attributeName, attributeValue);
            }
        } else {
            ObjectUtil.setProperty(object, attributeName, attributeValue);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String textString = null;
        if (text != null && text.length() > 0) {
            textString = StringUtils.trimToNull(text.toString());
        }

        try {
            if (rootPrecondition != null) {
                if ("preConditions".equals(qName)) {
                    changeLog.setPreconditions(rootPrecondition);
                    handlePreCondition(rootPrecondition);
                    rootPrecondition = null;
                } else if ("and".equals(qName)) {
                    preconditionLogicStack.pop();
                    currentPrecondition = null;
                } else if ("or".equals(qName)) {
                    preconditionLogicStack.pop();
                    currentPrecondition = null;
                } else if ("not".equals(qName)) {
                    preconditionLogicStack.pop();
                    currentPrecondition = null;
                } else if (qName.equals("sqlCheck")) {
                    ((SqlPrecondition) currentPrecondition).setSql(textString);
                    currentPrecondition = null;
                }

            } else if (changeSet != null && "rollback".equals(qName)) {
                changeSet.setRollBackSQL(textString);
            } else if (change != null && change instanceof RawSQLChange && "comment".equals(qName)) {
                ((RawSQLChange) change).setComments(textString);
                text = new StringBuffer();
            } else if (change != null && change instanceof CreateProcedureChange && "comment".equals(qName)) {
                ((CreateProcedureChange) change).setComments(textString);
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
                    } else if (change instanceof CreateProcedureChange) {
                        ((CreateProcedureChange) change).setProcedureBody(textString);
                    } else if (change instanceof CreateViewChange) {
                        ((CreateViewChange) change).setSelectQuery(textString);
                    } else if (change instanceof InsertDataChange) {
                        List<ColumnConfig> columns = ((InsertDataChange) change).getColumns();
                        columns.get(columns.size() - 1).setValue(textString);
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
            throw new SAXException(changeLog.getPhysicalFilePath() + ": " + e.getMessage(), e);
        }
    }

    /**
     * By defaultd does nothing.  Overridden in ValidatChangeLogHandler and anywhere else that is interested in them.
     */
    protected void handlePreCondition(@SuppressWarnings("unused")Precondition precondition) {
    }

    protected abstract void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException;

    public void characters(char ch[], int start, int length) throws SAXException {
        if (text != null) {
            text.append(new String(ch, start, length));
        }
    }
}
