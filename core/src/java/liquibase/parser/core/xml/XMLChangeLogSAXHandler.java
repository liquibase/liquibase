package liquibase.parser.core.xml;

import liquibase.change.*;
import liquibase.change.core.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ExpressionExpander;
import liquibase.exception.CustomChangeException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionFactory;
import liquibase.precondition.PreconditionLogic;
import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;
import liquibase.util.log.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class XMLChangeLogSAXHandler extends DefaultHandler {

    private static final char LIQUIBASE_FILE_SEPARATOR = '/';

    protected Logger log;

    private DatabaseChangeLog databaseChangeLog;
    private Change change;
    private StringBuffer text;
    private PreconditionContainer rootPrecondition;
    private Stack<PreconditionLogic> preconditionLogicStack = new Stack<PreconditionLogic>();
    private ChangeSet changeSet;
    private String paramName;
    private ResourceAccessor resourceAccessor;
    private Precondition currentPrecondition;

    private Map<String, Object> changeLogParameters = new HashMap<String, Object>();
    private boolean inRollback = false;

    private boolean inModifySql = false;
    private Collection modifySqlDbmsList;


    protected XMLChangeLogSAXHandler(String physicalChangeLogLocation, ResourceAccessor resourceAccessor, Map<String, Object> properties) {
        log = LogFactory.getLogger();
        this.resourceAccessor = resourceAccessor;

        databaseChangeLog = new DatabaseChangeLog(physicalChangeLogLocation);
        databaseChangeLog.setPhysicalFilePath(physicalChangeLogLocation);

        for (Map.Entry entry : System.getProperties().entrySet()) {
            changeLogParameters.put(entry.getKey().toString(), entry.getValue());
        }

        for (Map.Entry entry : properties.entrySet()) {
            changeLogParameters.put(entry.getKey().toString(), entry.getValue());
        }
    }

    public DatabaseChangeLog getDatabaseChangeLog() {
        return databaseChangeLog;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes baseAttributes) throws SAXException {
        Attributes atts = new ExpandingAttributes(baseAttributes);
        try {
            if ("comment".equals(qName)) {
                text = new StringBuffer();
            } else if ("validCheckSum".equals(qName)) {
                text = new StringBuffer();
            } else if ("databaseChangeLog".equals(qName)) {
                String version = uri.substring(uri.lastIndexOf("/") + 1);
                if (!version.equals(XMLChangeLogSAXParser.getSchemaVersion())) {
                    log.warning(databaseChangeLog.getPhysicalFilePath() + " is using schema version " + version + " rather than version " + XMLChangeLogSAXParser.getSchemaVersion());
                }
                databaseChangeLog.setLogicalFilePath(atts.getValue("logicalFilePath"));
            } else if ("include".equals(qName)) {
                String fileName = atts.getValue("file");
                boolean isRelativeToChangelogFile = Boolean.parseBoolean(atts.getValue("relativeToChangelogFile"));
                handleIncludedChangeLog(fileName, isRelativeToChangelogFile, databaseChangeLog.getPhysicalFilePath());
            } else if ("includeAll".equals(qName)) {
                String pathName = atts.getValue("path");
                if (!(pathName.endsWith("/") || pathName.endsWith("\\"))) {
                    pathName = pathName+"/";
                }
                log.finest("includeAll for "+pathName);
                log.finest("Using file opener for includeAll: "+ resourceAccessor.getClass().getName());
                Enumeration<URL> resources = resourceAccessor.getResources(pathName);

                boolean foundResource = false;

                while (resources.hasMoreElements()) {
                    URL fileUrl = resources.nextElement();
                    if (!fileUrl.toExternalForm().startsWith("file:")) {
                        log.finest(fileUrl.toExternalForm()+" is not a file path");
                        continue;
                    }
                    File file = new File(fileUrl.toURI());
                    log.finest("includeAll using path "+file.getCanonicalPath());
                    if (!file.exists()) {
                        throw new SAXException("includeAll path " + pathName + " could not be found.  Tried in " + file.toString());
                    }
                    if (file.isDirectory()) {
                        log.finest(file.getCanonicalPath()+" is a directory");
                        for (File childFile : file.listFiles()) {
                            if (handleIncludedChangeLog(pathName + childFile.getName(), false, databaseChangeLog.getPhysicalFilePath())) {
                                foundResource = true;
                            }
                        }
                    } else {
                        if (handleIncludedChangeLog(pathName + file.getName(), false, databaseChangeLog.getPhysicalFilePath())) {
                            foundResource = true;
                        }
                    }
                }

                if (!foundResource) {
                    throw new SAXException("Could not find directory "+pathName);
                }
            } else if (changeSet == null && "changeSet".equals(qName)) {
                boolean alwaysRun = false;
                boolean runOnChange = false;
                if ("true".equalsIgnoreCase(atts.getValue("runAlways"))) {
                    alwaysRun = true;
                }
                if ("true".equalsIgnoreCase(atts.getValue("runOnChange"))) {
                    runOnChange = true;
                }
                changeSet = new ChangeSet(atts.getValue("id"),
                        atts.getValue("author"),
                        alwaysRun,
                        runOnChange,
                        databaseChangeLog.getFilePath(),
                        databaseChangeLog.getPhysicalFilePath(),
                        atts.getValue("context"),
                        atts.getValue("dbms"),
                        Boolean.valueOf(atts.getValue("runInTransaction")));
                if (StringUtils.trimToNull(atts.getValue("failOnError")) != null) {
                    changeSet.setFailOnError(Boolean.parseBoolean(atts.getValue("failOnError")));
                }
            } else if (changeSet != null && "rollback".equals(qName)) {
                text = new StringBuffer();
                String id = atts.getValue("changeSetId");
                if (id != null) {
                    String path = atts.getValue("changeSetPath");
                    if (path == null) {
                        path = databaseChangeLog.getFilePath();
                    }
                    String author = atts.getValue("changeSetAuthor");
                    ChangeSet changeSet = databaseChangeLog.getChangeSet(path, author, id);
                    if (changeSet == null) {
                        throw new SAXException("Could not find changeSet to use for rollback: " + path + ":" + author + ":" + id);
                    } else {
                        for (Change change : changeSet.getChanges()) {
                            this.changeSet.addRollbackChange(change);
                        }
                    }
                }
                inRollback = true;
            } else if ("preConditions".equals(qName)) {
                rootPrecondition = new PreconditionContainer();
                rootPrecondition.setOnFail(StringUtils.trimToNull(atts.getValue("onFail")));
                rootPrecondition.setOnError(StringUtils.trimToNull(atts.getValue("onError")));
                preconditionLogicStack.push(rootPrecondition);
            } else if (currentPrecondition != null && currentPrecondition instanceof CustomPreconditionWrapper && qName.equals("param")) {
                ((CustomPreconditionWrapper) currentPrecondition).setParam(atts.getValue("name"), atts.getValue("value"));
            } else if (rootPrecondition != null) {
                currentPrecondition = PreconditionFactory.getInstance().create(qName);

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
            } else if ("modifySql".equals(qName)) {
                inModifySql = true;
                if (StringUtils.trimToNull(atts.getValue("dbms")) != null) {
                    modifySqlDbmsList = StringUtils.splitAndTrim(atts.getValue("dbms"), ",");
                }
            } else if (inModifySql) {
                SqlVisitor sqlVisitor = SqlVisitorFactory.getInstance().create(qName);
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(sqlVisitor, attributeName, attributeValue);
                }
                sqlVisitor.setApplicableDbms(modifySqlDbmsList);

                changeSet.addSqlVisitor(sqlVisitor);
            } else if (changeSet != null && change == null) {
                change = ChangeFactory.getInstance().create(localName);
                if (change == null) {
                    throw new SAXException("Unknown LiquiBase extension: "+localName+".  Are you missing a jar from your classpath?");
                }
                change.setChangeSet(changeSet);
                text = new StringBuffer();
                if (change == null) {
                    throw new MigrationFailedException(changeSet, "Unknown change: " + localName);
                }
                change.setFileOpener(resourceAccessor);
                if (change instanceof CustomChangeWrapper) {
                    ((CustomChangeWrapper) change).setClassLoader(resourceAccessor.toClassLoader());
                }
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getLocalName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(change, attributeName, attributeValue);
                }
                change.init();
            } else if (change != null && "column".equals(qName)) {
                ColumnConfig column;
                if (change instanceof LoadDataChange) {
                    column = new LoadDataColumnConfig();
                } else {
                    column = new ColumnConfig();
                }
                for (int i = 0; i < atts.getLength(); i++) {
                    String attributeName = atts.getQName(i);
                    String attributeValue = atts.getValue(i);
                    setProperty(column, attributeName, attributeValue);
                }
                if (change instanceof ChangeWithColumns) {
                    ((ChangeWithColumns) change).addColumn(column);
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
                    lastColumn = ((AddColumnChange) change).getLastColumn();
                } else if (change instanceof CreateTableChange) {
                    lastColumn = ((CreateTableChange) change).getColumns().get(((CreateTableChange) change).getColumns().size() - 1);
                } else if (change instanceof ModifyColumnChange) {
                    lastColumn = ((ModifyColumnChange) change).getColumns().get(((ModifyColumnChange) change).getColumns().size() - 1);
                } else {
                    throw new RuntimeException("Unexpected change: " + change.getClass().getName());
                }
                lastColumn.setConstraints(constraints);
            } else if ("param".equals(qName)) {
                if (change instanceof CustomChangeWrapper) {
                    if (atts.getValue("value") == null) {
                        paramName = atts.getValue("name");
                        text = new StringBuffer();
                    } else {
                        ((CustomChangeWrapper) change).setParam(atts
                                .getValue("name"), atts.getValue("value"));
                    }
                } else {
                    throw new MigrationFailedException(changeSet,
                            "'param' unexpected in " + qName);
                }
            } else if ("where".equals(qName)) {
                text = new StringBuffer();
            } else if ("property".equals(qName)) {
                if (StringUtils.trimToNull(atts.getValue("file")) == null) {
                    this.setParameterValue(atts.getValue("name"), atts.getValue("value"));
                } else {
                    Properties props = new Properties();
                    InputStream propertiesStream = resourceAccessor.getResourceAsStream(atts.getValue("file"));
                    if (propertiesStream == null) {
                        log.info("Could not open properties file " + atts.getValue("file"));
                    } else {
                        props.load(propertiesStream);

                        for (Map.Entry entry : props.entrySet()) {
                            this.setParameterValue(entry.getKey().toString(), entry.getValue().toString());
                        }
                    }
                }
            } else if (change instanceof ExecuteShellCommandChange && "arg".equals(qName)) {
                ((ExecuteShellCommandChange) change).addArg(atts.getValue("value"));
            } else {
                throw new MigrationFailedException(changeSet, "Unexpected tag: " + qName);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error thrown as a SAXException: " + e.getMessage(), e);
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    protected boolean handleIncludedChangeLog(String fileName, boolean isRelativePath, String relativeBaseFileName) throws LiquibaseException {
        if (!(fileName.endsWith(".xml") || fileName.endsWith(".sql"))) {
            log.finest(relativeBaseFileName+"/"+fileName+" is not a recognized file type");
            return false;
        }

        if (isRelativePath) {
            String path = searchPath(relativeBaseFileName);
            fileName = new StringBuilder(path).append(fileName).toString();
        }
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(fileName).parse(fileName, changeLogParameters, resourceAccessor);
        AndPrecondition preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == databaseChangeLog.getPreconditions()) {
                databaseChangeLog.setPreconditions(new PreconditionContainer());
            }
            databaseChangeLog.getPreconditions().addNestedPrecondition(preconditions);
        }
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            databaseChangeLog.addChangeSet(changeSet);
        }

        return true;
    }

    private String searchPath(String relativeBaseFileName) {
        if (relativeBaseFileName == null) {
            return null;
        }
        int lastSeparatePosition = relativeBaseFileName.lastIndexOf(LIQUIBASE_FILE_SEPARATOR);
        if (lastSeparatePosition >= 0) {
            return relativeBaseFileName.substring(0, lastSeparatePosition + 1);
        }
        return relativeBaseFileName;
    }

    private void setProperty(Object object, String attributeName, String attributeValue) throws IllegalAccessException, InvocationTargetException, CustomChangeException {
        ExpressionExpander expressionExpander = new ExpressionExpander(changeLogParameters);
        if (object instanceof CustomChangeWrapper) {
            if (attributeName.equals("class")) {
                ((CustomChangeWrapper) object).setClass(expressionExpander.expandExpressions(attributeValue));
            } else {
                ((CustomChangeWrapper) object).setParam(attributeName, expressionExpander.expandExpressions(attributeValue));
            }
        } else {
            ObjectUtil.setProperty(object, attributeName, expressionExpander.expandExpressions(attributeValue));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String textString = null;
        if (text != null && text.length() > 0) {
            textString = new ExpressionExpander(changeLogParameters).expandExpressions(StringUtils.trimToNull(text.toString()));
        }

        try {
            if (rootPrecondition != null) {
                if ("preConditions".equals(qName)) {
                    if (changeSet == null) {
                        databaseChangeLog.setPreconditions(rootPrecondition);
                        handlePreCondition(rootPrecondition);
                    } else {
                        changeSet.setPreconditions(rootPrecondition);
                    }
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
                } else if (qName.equals("customPrecondition")) {
                    ((CustomPreconditionWrapper) currentPrecondition).setClassLoader(resourceAccessor.toClassLoader());
                }

            } else if (changeSet != null && "rollback".equals(qName)) {
                changeSet.addRollBackSQL(textString);
                inRollback = false;
            } else if (change != null && change instanceof RawSQLChange && "comment".equals(qName)) {
                ((RawSQLChange) change).setComments(textString);
                text = new StringBuffer();
            } else if (change != null && "where".equals(qName)) {
                if (change instanceof UpdateDataChange) {
                    ((UpdateDataChange) change).setWhereClause(textString);
                } else if (change instanceof DeleteDataChange) {
                    ((DeleteDataChange) change).setWhereClause(textString);
                } else {
                    throw new RuntimeException("Unexpected change type: " + change.getClass().getName());
                }
                text = new StringBuffer();
            } else if (change != null && change instanceof CreateProcedureChange && "comment".equals(qName)) {
                ((CreateProcedureChange) change).setComments(textString);
                text = new StringBuffer();
            } else if (change != null && change instanceof CustomChangeWrapper
                    && paramName != null && "param".equals(qName)) {
                ((CustomChangeWrapper) change).setParam(paramName, textString);
                text = new StringBuffer();
                paramName = null;
            } else if (changeSet != null && "comment".equals(qName)) {
                changeSet.setComments(textString);
                text = new StringBuffer();
            } else if (changeSet != null && "changeSet".equals(qName)) {
                handleChangeSet(changeSet);
                changeSet = null;
            } else if (change != null && qName.equals("column") && textString != null) {
                if (change instanceof InsertDataChange) {
                    List<ColumnConfig> columns = ((InsertDataChange) change).getColumns();
                    columns.get(columns.size() - 1).setValue(textString);
                } else if (change instanceof UpdateDataChange) {
                    List<ColumnConfig> columns = ((UpdateDataChange) change).getColumns();
                    columns.get(columns.size() - 1).setValue(textString);
                } else {
                    throw new RuntimeException("Unexpected column with text: "+textString);
                }
                this.text = new StringBuffer();
            } else if (change != null && localName.equals(change.getChangeMetaData().getName())) {
                if (textString != null) {
                    if (change instanceof RawSQLChange) {
                        ((RawSQLChange) change).setSql(textString);
                    } else if (change instanceof CreateProcedureChange) {
                        ((CreateProcedureChange) change).setProcedureBody(textString);
                    } else if (change instanceof CreateViewChange) {
                        ((CreateViewChange) change).setSelectQuery(textString);
                    } else if (change instanceof StopChange) {
                        ((StopChange) change).setMessage(textString);
                    } else {
                        throw new RuntimeException("Unexpected text in " + change.getChangeMetaData().getName());
                    }
                }
                text = null;
                if (inRollback) {
                    changeSet.addRollbackChange(change);
                } else {
                    changeSet.addChange(change);
                }
                change = null;
            } else if (changeSet != null && "validCheckSum".equals(qName)) {
                changeSet.addValidCheckSum(text.toString());
                text = null;
            } else if ("modifySql".equals(qName)) {
                inModifySql = false;
                modifySqlDbmsList = null;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error thrown as a SAXException: " + e.getMessage(), e);
            throw new SAXException(databaseChangeLog.getPhysicalFilePath() + ": " + e.getMessage(), e);
        }
    }

    protected void handlePreCondition(@SuppressWarnings("unused") Precondition precondition) {
        databaseChangeLog.setPreconditions(rootPrecondition);
    }

    protected void handleChangeSet(ChangeSet changeSet) {
        databaseChangeLog.addChangeSet(changeSet);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if (text != null) {
            text.append(new String(ch, start, length));
        }
    }

    public Object getParameterValue(String paramter) {
        return changeLogParameters.get(paramter);
    }

    public void setParameterValue(String paramter, Object value) {
        if (!changeLogParameters.containsKey(paramter)) {
            changeLogParameters.put(paramter, value);
        }
    }

    /**
     * Wrapper for Attributes that expands the value as needed
     */
    private class ExpandingAttributes implements Attributes {
        private Attributes attributes;

        private ExpandingAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public int getLength() {
            return attributes.getLength();
        }

        public String getURI(int index) {
            return attributes.getURI(index);
        }

        public String getLocalName(int index) {
            return attributes.getLocalName(index);
        }

        public String getQName(int index) {
            return attributes.getQName(index);
        }

        public String getType(int index) {
            return attributes.getType(index);
        }

        public String getValue(int index) {
            return attributes.getValue(index);
        }

        public int getIndex(String uri, String localName) {
            return attributes.getIndex(uri, localName);
        }

        public int getIndex(String qName) {
            return attributes.getIndex(qName);
        }

        public String getType(String uri, String localName) {
            return attributes.getType(uri, localName);
        }

        public String getType(String qName) {
            return attributes.getType(qName);
        }

        public String getValue(String uri, String localName) {
            return new ExpressionExpander(changeLogParameters).expandExpressions(attributes.getValue(uri, localName));
        }

        public String getValue(String qName) {
            return new ExpressionExpander(changeLogParameters).expandExpressions(attributes.getValue(qName));
        }
    }
}
