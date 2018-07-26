package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import liquibase.util.file.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog implements Comparable<DatabaseChangeLog>, Conditional {
    private static final ThreadLocal<DatabaseChangeLog> ROOT_CHANGE_LOG = new ThreadLocal<>();
    private static final ThreadLocal<DatabaseChangeLog> PARENT_CHANGE_LOG = new ThreadLocal<>();
    private static final Logger LOG = LogService.getLog(DatabaseChangeLog.class);

    private PreconditionContainer preconditionContainer = new PreconditionContainer();
    private String physicalFilePath;
    private String logicalFilePath;
    private ObjectQuotingStrategy objectQuotingStrategy;

    private List<ChangeSet> changeSets = new ArrayList<>();
    private ChangeLogParameters changeLogParameters;

    private RuntimeEnvironment runtimeEnvironment;
    private boolean ignoreClasspathPrefix;

    private DatabaseChangeLog rootChangeLog = ROOT_CHANGE_LOG.get();

    private DatabaseChangeLog parentChangeLog = PARENT_CHANGE_LOG.get();

    private ContextExpression contexts;

    private ContextExpression includeContexts;

    public DatabaseChangeLog() {
    }

    public DatabaseChangeLog(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public void setRootChangeLog(DatabaseChangeLog rootChangeLog) {
        this.rootChangeLog = rootChangeLog;
    }

    public DatabaseChangeLog getRootChangeLog() {
        return (rootChangeLog != null) ? rootChangeLog : this;
    }

    public void setParentChangeLog(DatabaseChangeLog parentChangeLog) {
        this.parentChangeLog = parentChangeLog;
    }

    public DatabaseChangeLog getParentChangeLog() {
        return parentChangeLog;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    @Override
    public PreconditionContainer getPreconditions() {
        return preconditionContainer;
    }

    @Override
    public void setPreconditions(PreconditionContainer precond) {
        if (precond == null) {
            this.preconditionContainer = new PreconditionContainer();
        } else {
            preconditionContainer = precond;
        }
    }


    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    public void setChangeLogParameters(ChangeLogParameters changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    public String getPhysicalFilePath() {
        return physicalFilePath;
    }

    public void setPhysicalFilePath(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public String getLogicalFilePath() {
        String returnPath = logicalFilePath;
        if (logicalFilePath == null) {
            returnPath = physicalFilePath;
        }
        return returnPath.replaceAll("\\\\", "/");
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

    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }

    public void setObjectQuotingStrategy(ObjectQuotingStrategy objectQuotingStrategy) {
        this.objectQuotingStrategy = objectQuotingStrategy;
    }

    public ContextExpression getContexts() {
        return contexts;
    }

    public void setContexts(ContextExpression contexts) {
        this.contexts = contexts;
    }

    public ContextExpression getIncludeContexts() {
        return includeContexts;
    }

    public void setIncludeContexts(ContextExpression includeContexts) {
        this.includeContexts = includeContexts;
    }

    @Override
    public String toString() {
        return getFilePath();
    }

    @Override
    public int compareTo(DatabaseChangeLog o) {
        return getFilePath().compareTo(o.getFilePath());
    }


    public ChangeSet getChangeSet(String path, String author, String id) {
        for (ChangeSet changeSet : changeSets) {
            if (normalizePath(changeSet.getFilePath()).equalsIgnoreCase(normalizePath(path))
                    && changeSet.getAuthor().equalsIgnoreCase(author)
                    && changeSet.getId().equalsIgnoreCase(id)
                    && isDbmsMatch(changeSet.getDbmsSet())) {
                return changeSet;
            }
        }

        return null;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    public void addChangeSet(ChangeSet changeSet) {
        if (changeSet.getRunOrder() == null) {
            ListIterator<ChangeSet> it = this.changeSets.listIterator(this.changeSets.size());
            boolean added = false;
            while (it.hasPrevious() && !added) {
                if (!"last".equals(it.previous().getRunOrder())) {
                    it.next();
                    it.add(changeSet);
                    added = true;
                }
            }
            if (!added) {
                it.add(changeSet);
            }

        } else if ("first".equals(changeSet.getRunOrder())) {
            ListIterator<ChangeSet> it = this.changeSets.listIterator();
            boolean added = false;
            while (it.hasNext() && !added) {
                if (!"first".equals(it.next().getRunOrder())) {
                    it.previous();
                    it.add(changeSet);
                    added = true;
                }
            }
            if (!added) {
                this.changeSets.add(changeSet);
            }
        } else if ("last".equals(changeSet.getRunOrder())) {
            this.changeSets.add(changeSet);
        } else {
            throw new UnexpectedLiquibaseException("Unknown runOrder: " + changeSet.getRunOrder());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        DatabaseChangeLog that = (DatabaseChangeLog) o;

        return getFilePath().equals(that.getFilePath());

    }

    @Override
    public int hashCode() {
        return getFilePath().hashCode();
    }

    public void validate(Database database, String... contexts) throws LiquibaseException {
        this.validate(database, new Contexts(contexts), new LabelExpression());
    }

    public void validate(Database database, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {

        database.setObjectQuotingStrategy(objectQuotingStrategy);

        ChangeLogIterator logIterator = new ChangeLogIterator(
                this,
                new DbmsChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression)
        );

        ValidatingVisitor validatingVisitor = new ValidatingVisitor(database.getRanChangeSetList());
        validatingVisitor.validate(database, this);
        logIterator.run(validatingVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

        for (String message : validatingVisitor.getWarnings().getMessages()) {
            LogService.getLog(getClass()).warning(LogType.LOG, message);
        }

        if (!validatingVisitor.validationPassed()) {
            throw new ValidationFailedException(validatingVisitor);
        }
    }

    public ChangeSet getChangeSet(RanChangeSet ranChangeSet) {
        return getChangeSet(ranChangeSet.getChangeLog(), ranChangeSet.getAuthor(), ranChangeSet.getId());
    }

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor)
            throws ParsedNodeException, SetupException {
        setLogicalFilePath(parsedNode.getChildValue(null, "logicalFilePath", String.class));
        setContexts(new ContextExpression(parsedNode.getChildValue(null, "context", String.class)));
        String objectQuotingStrategy = parsedNode.getChildValue(null, "objectQuotingStrategy", String.class);
        if (objectQuotingStrategy != null) {
            setObjectQuotingStrategy(ObjectQuotingStrategy.valueOf(objectQuotingStrategy));
        }
        for (ParsedNode childNode : parsedNode.getChildren()) {
            handleChildNode(childNode, resourceAccessor);
        }
    }

    protected void expandExpressions(ParsedNode parsedNode) {
        if (changeLogParameters == null) {
            return;
        }
        try {
            Object value = parsedNode.getValue();
            if ((value != null) && (value instanceof String)) {
                parsedNode.setValue(changeLogParameters.expandExpressions(parsedNode.getValue(String.class), this));
            }

            List<ParsedNode> children = parsedNode.getChildren();
            if (children != null) {
                for (ParsedNode child : children) {
                    expandExpressions(child);
                }
            }
        } catch (ParsedNodeException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected void handleChildNode(ParsedNode node, ResourceAccessor resourceAccessor)
            throws ParsedNodeException, SetupException {
        expandExpressions(node);
        String nodeName = node.getName();
        switch (nodeName) {
            case"changeSet":
            if (isDbmsMatch(node.getChildValue(null, "dbms", String.class))) {this.addChangeSet(createChangeSet(node, resourceAccessor));}
        break;
            case"include": {
            String path = node.getChildValue(null, "file", String.class);
            if (path == null) {
                throw new UnexpectedLiquibaseException("No 'file' attribute on 'include'");
            }
            path = path.replace('\\', '/');
            ContextExpression includeContexts = new ContextExpression(node.getChildValue(null, "context", String.class));
            try {
                include(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceAccessor, includeContexts, true);
            } catch (LiquibaseException e) {
                throw new SetupException(e);}
                break;
            }
            case "includeAll": {
                String path = node.getChildValue(null, "path", String.class);
                String resourceFilterDef = node.getChildValue(null, "filter", String.class);
                if (resourceFilterDef == null) {
                    resourceFilterDef = node.getChildValue(null, "resourceFilter", String.class);
                }
                IncludeAllFilter resourceFilter = null;
                if (resourceFilterDef != null) {
                    try {
                        resourceFilter = (IncludeAllFilter) Class.forName(resourceFilterDef).newInstance();
                    } catch (InstantiationException|IllegalAccessException|ClassNotFoundException e) {
                        throw new SetupException(e);
                    }
                }

                String resourceComparatorDef = node.getChildValue(null, "resourceComparator", String.class);
                Comparator<String> resourceComparator = null;
                if (resourceComparatorDef != null) {
                    try {
                        resourceComparator = (Comparator<String>) Class.forName(resourceComparatorDef).newInstance();
                    } catch (InstantiationException|IllegalAccessException|ClassNotFoundException e) {
                        //take default comparator
                        LogService.getLog(getClass()).info(LogType.LOG, "no resourceComparator defined - taking default " +
                         "implementation");
                        resourceComparator = getStandardChangeLogComparator();
                    }
                }

                ContextExpression includeContexts = new ContextExpression(node.getChildValue(null, "context", String.class));
                includeAll(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceFilter,
                        node.getChildValue(null, "errorIfMissingOrEmpty", true),
                        resourceComparator, resourceAccessor, includeContexts);
                break;
            }
            case "preConditions": {
                this.preconditionContainer = new PreconditionContainer();
                try {
                    this.preconditionContainer.load(node, resourceAccessor);
                } catch (ParsedNodeException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "property": {
                try {
                    String context = node.getChildValue(null, "context", String.class);
                    String dbms = node.getChildValue(null, "dbms", String.class);
                    String labels = node.getChildValue(null, "labels", String.class);
                    Boolean global = node.getChildValue(null, "global", Boolean.class);
                    if (global == null) {
                        // okay behave like liquibase < 3.4 and set global == true
                        global = true;
                    }

                    String file = node.getChildValue(null, "file", String.class);

                    if (file == null) {
                        // direct referenced property, no file
                        String name = node.getChildValue(null, "name", String.class);
                        String value = node.getChildValue(null, "value", String.class);

                        this.changeLogParameters.set(name, value, context, labels, dbms, global, this);
                    } else {
                        // read properties from the file
                        Properties props = new Properties();
                        InputStream propertiesStream = StreamUtil.singleInputStream(file, resourceAccessor);
                        if (propertiesStream == null) {
                            LogService.getLog(getClass()).info(LogType.LOG, "Could not open properties file " + file);
                        } else {
                            props.load(propertiesStream);

                            for (Map.Entry entry : props.entrySet()) {
                                this.changeLogParameters.set(
                                        entry.getKey().toString(),
                                        entry.getValue().toString(),
                                        context,
                                        labels,
                                        dbms,
                                        global,
                                        this
                                );
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new ParsedNodeException(e);
                }

                break;
            }
        }
    }

    public boolean isDbmsMatch(String dbmsList) {
        return isDbmsMatch(DatabaseList.toDbmsSet(dbmsList));
    }

    public boolean isDbmsMatch(Set<String> dbmsSet) {
        return dbmsSet == null
                || changeLogParameters == null
                || changeLogParameters.getValue("database.typeName", this) == null
                || dbmsSet.isEmpty()
                || dbmsSet.contains(changeLogParameters.getValue("database.typeName", this).toString());
    }

    public void includeAll(String pathName, boolean isRelativeToChangelogFile, IncludeAllFilter resourceFilter,
                           boolean errorIfMissingOrEmpty,
                           Comparator<String> resourceComparator,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContexts)
            throws SetupException {
        try {
            if (pathName == null) {
                throw new SetupException("No path attribute for includeAll");
            }
            pathName = pathName.replace('\\', '/');

            if (!(pathName.endsWith("/"))) {
                pathName = pathName + '/';
            }
            LOG.debug(LogType.LOG, "includeAll for " + pathName);
            LOG.debug(LogType.LOG, "Using file opener for includeAll: " + resourceAccessor.toString());

            String relativeTo = null;
            if (isRelativeToChangelogFile) {
                relativeTo = this.getPhysicalFilePath();
            }

            Set<String> unsortedResources = null;
            try {
                unsortedResources = resourceAccessor.list(relativeTo, pathName, true, false, true);
            } catch (FileNotFoundException e) {
                if (errorIfMissingOrEmpty) {
                    throw e;
                }
            }
            SortedSet<String> resources = new TreeSet<>(resourceComparator);
            if (unsortedResources != null) {
                for (String resourcePath : unsortedResources) {
                    if ((resourceFilter == null) || resourceFilter.include(resourcePath)) {
                        resources.add(resourcePath);
                    }
                }
            }

            if (resources.isEmpty() && errorIfMissingOrEmpty) {
                throw new SetupException(
                        "Could not find directory or directory was empty for includeAll '" + pathName + "'");
            }

            for (String path : resources) {
                include(path, false, resourceAccessor, includeContexts, false);
            }
        } catch (Exception e) {
            throw new SetupException(e);
        }
    }

    public boolean include(String fileName, boolean isRelativePath, ResourceAccessor resourceAccessor,
                           ContextExpression includeContexts, boolean logEveryUnknownFileFormat
    ) throws LiquibaseException {

        if (".svn".equalsIgnoreCase(fileName) || "cvs".equalsIgnoreCase(fileName)) {
            return false;
        }

        String relativeBaseFileName = this.getPhysicalFilePath();
        if (isRelativePath) {
            // workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
            String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
            if (tempFile != null && new File(tempFile).exists() == true) {
                fileName = tempFile;
            } else {
                fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
            }
        }
        DatabaseChangeLog changeLog;
        try {
            DatabaseChangeLog rootChangeLog = ROOT_CHANGE_LOG.get();
            if (rootChangeLog == null) {
                ROOT_CHANGE_LOG.set(this);
            }
            DatabaseChangeLog parentChangeLog = PARENT_CHANGE_LOG.get();
            PARENT_CHANGE_LOG.set(this);
            try {
                ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor);
                changeLog = parser.parse(fileName, changeLogParameters, resourceAccessor);
                changeLog.setIncludeContexts(includeContexts);
            } finally {
                if (rootChangeLog == null) {
                    ROOT_CHANGE_LOG.remove();
                }
                if (parentChangeLog == null) {
                    PARENT_CHANGE_LOG.remove();
                } else {
                    PARENT_CHANGE_LOG.set(parentChangeLog);
                }
            }
        } catch (UnknownChangelogFormatException e) {
            // This matches only an extension, but filename can be a full path, too. Is it right?
            boolean matchesFileExtension = StringUtil.trimToEmpty(fileName).matches("\\.\\w+$");
            if (matchesFileExtension || logEveryUnknownFileFormat) {
                LogService.getLog(getClass()).warning(
                        LogType.LOG, "included file " + relativeBaseFileName + "/" + fileName + " is not a recognized file type"
                );
            }
            return false;
        }
        PreconditionContainer preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == this.getPreconditions()) {
                this.setPreconditions(new PreconditionContainer());
            }
            this.getPreconditions().addNestedPrecondition(preconditions);
        }
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            addChangeSet(changeSet);
        }

        return true;
    }

    protected ChangeSet createChangeSet(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ChangeSet changeSet = new ChangeSet(this);
        changeSet.setChangeLogParameters(this.getChangeLogParameters());
        changeSet.load(node, resourceAccessor);
        return changeSet;
    }

    protected Comparator<String> getStandardChangeLogComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                //by ignoring WEB-INF/classes in path all changelog Files independent
                //whehther they are in a WAR or in a JAR are order following the same rule
                return o1.replace("WEB-INF/classes/", "").compareTo(o2.replace("WEB-INF/classes/", ""));
            }
        };
    }

    public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
    }

    public boolean ignoreClasspathPrefix() {
        return ignoreClasspathPrefix;
    }

    protected String normalizePath(String filePath) {
        if (ignoreClasspathPrefix) {
            return filePath.replaceFirst("^classpath:", "");
        }
        return filePath;
    }

    public void clearCheckSums() {
        for (ChangeSet changeSet : getChangeSets()) {
            changeSet.clearCheckSum();
        }
    }

}
