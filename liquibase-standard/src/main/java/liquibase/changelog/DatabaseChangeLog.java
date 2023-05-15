package liquibase.changelog;

import liquibase.*;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.logging.Logger;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcValue;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog implements Comparable<DatabaseChangeLog>, Conditional {
    private static final ThreadLocal<DatabaseChangeLog> ROOT_CHANGE_LOG = new ThreadLocal<>();
    private static final ThreadLocal<DatabaseChangeLog> PARENT_CHANGE_LOG = new ThreadLocal<>();
    private static final Logger LOG = Scope.getCurrentScope().getLog(DatabaseChangeLog.class);
    private static final Pattern SLASH_PATTERN = Pattern.compile("^/");

    private static final Pattern DOUBLE_BACK_SLASH_PATTERN = Pattern.compile("\\\\");
    private static final Pattern NO_LETTER_PATTERN = Pattern.compile("^[a-zA-Z]:");
    private static final String SEEN_CHANGELOGS_PATHS_SCOPE_KEY = "SEEN_CHANGELOG_PATHS";

    private PreconditionContainer preconditionContainer = new GlobalPreconditionContainer();
    private String physicalFilePath;
    private String logicalFilePath;
    private String changeLogId;
    private ObjectQuotingStrategy objectQuotingStrategy;

    private List<ChangeSet> changeSets = new ArrayList<>();
    private List<ChangeSet> skippedChangeSets = new ArrayList<>();
    private ChangeLogParameters changeLogParameters;

    private RuntimeEnvironment runtimeEnvironment;

    private DatabaseChangeLog rootChangeLog = ROOT_CHANGE_LOG.get();

    private DatabaseChangeLog parentChangeLog = PARENT_CHANGE_LOG.get();

    private ContextExpression contextFilter;

    private ContextExpression includeContextFilter;

    private Labels includeLabels;
    private boolean includeIgnore;

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
        this.preconditionContainer.addNestedPrecondition(precond);
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

    public String getChangeLogId() {
        return changeLogId;
    }

    public void setChangeLogId(String changeLogId) {
        this.changeLogId = changeLogId;
    }

    public String getLogicalFilePath() {
        String returnPath = logicalFilePath;
        if (logicalFilePath == null) {
            returnPath = physicalFilePath;
        }
        if (returnPath == null) {
            return null;
        }
        String path = DOUBLE_BACK_SLASH_PATTERN.matcher(returnPath).replaceAll("/");
        return SLASH_PATTERN.matcher(path).replaceFirst("");
    }

    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return getLogicalFilePath();
        }
    }

    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }

    public void setObjectQuotingStrategy(ObjectQuotingStrategy objectQuotingStrategy) {
        this.objectQuotingStrategy = objectQuotingStrategy;
    }

    /**
     * @deprecated use {@link #getContextFilter()}
     */
    public ContextExpression getContexts() {
        return getContextFilter();
    }

    /**
     * @deprecated use {@link #setContextFilter(ContextExpression)}
     */
    public void setContexts(ContextExpression contexts) {
        setContextFilter(contexts);
    }

    public ContextExpression getContextFilter() {
        return contextFilter;
    }

    public void setContextFilter(ContextExpression contextFilter) {
        this.contextFilter = contextFilter;
    }

    public ContextExpression getIncludeContextFilter() {
        return includeContextFilter;
    }

    /**
     * @deprecated Correct version is {@link #setIncludeLabels(Labels)}. Kept for backwards compatibility.
     */
    public void setIncludeLabels(LabelExpression labels) {
        this.includeLabels = new Labels(labels.toString());
    }

    public void setIncludeLabels(Labels labels) {
        this.includeLabels = labels;
    }

    public Labels getIncludeLabels() {
        return includeLabels;
    }

    public void setIncludeIgnore(boolean ignore) {
        this.includeIgnore = ignore;
    }

    public boolean isIncludeIgnore() {
        return this.includeIgnore;
    }

    /**
     * @deprecated use {@link #setIncludeContextFilter(ContextExpression)}
     */
    public void setIncludeContexts(ContextExpression includeContexts) {
        setIncludeContextFilter(includeContexts);
    }

    public void setIncludeContextFilter(ContextExpression includeContextFilter) {
        this.includeContextFilter = includeContextFilter;
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
	    final List<ChangeSet> possibleChangeSets = getChangeSets(path, author, id);
	    if (possibleChangeSets.isEmpty()){
	    	return null;
	    }
	    return possibleChangeSets.get(0);
    }

    public List<ChangeSet> getChangeSets(String path, String author, String id) {
	    final ArrayList<ChangeSet> changeSetsToReturn = new ArrayList<>();
	    for (ChangeSet changeSet : this.changeSets) {
            final String normalizedPath = normalizePath(changeSet.getFilePath());
            if (normalizedPath != null &&
                    normalizedPath.equalsIgnoreCase(normalizePath(path)) &&
                    changeSet.getAuthor().equalsIgnoreCase(author) &&
                    changeSet.getId().equalsIgnoreCase(id) &&
                    isDbmsMatch(changeSet.getDbmsSet())) {
                changeSetsToReturn.add(changeSet);
            }
        }
        return changeSetsToReturn;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    public List<ChangeSet> getSkippedChangeSets() {
        return skippedChangeSets;
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

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        for (String message : validatingVisitor.getWarnings().getMessages()) {
            log.warning(message);
        }

        if (!validatingVisitor.validationPassed()) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED);
            Scope.getCurrentScope().getLog(getClass()).info("Change failed validation!");
            throw new ValidationFailedException(validatingVisitor);
        }
    }

    public ChangeSet getChangeSet(RanChangeSet ranChangeSet) {
        final ChangeSet changeSet = getChangeSet(ranChangeSet.getChangeLog(), ranChangeSet.getAuthor(), ranChangeSet.getId());
        if (changeSet != null) {
            changeSet.setStoredFilePath(ranChangeSet.getStoredChangeLog());
        }
        return changeSet;
    }

    public List<ChangeSet> getChangeSets(RanChangeSet ranChangeSet) {
        List<ChangeSet> changesets = getChangeSets(ranChangeSet.getChangeLog(), ranChangeSet.getAuthor(), ranChangeSet.getId());
        changesets.forEach(c -> c.setStoredFilePath(ranChangeSet.getStoredChangeLog()));
        return changesets;
    }

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        setChangeLogId(parsedNode.getChildValue(null, "changeLogId", String.class));
        setLogicalFilePath(parsedNode.getChildValue(null, "logicalFilePath", String.class));

        String context = parsedNode.getChildValue(null, "contextFilter", String.class);
        if (context == null) {
            context = parsedNode.getChildValue(null, "context", String.class);
        }

        setContextFilter(new ContextExpression(context));
        String nodeObjectQuotingStrategy = parsedNode.getChildValue(null, "objectQuotingStrategy", String.class);
        if (nodeObjectQuotingStrategy != null) {
            setObjectQuotingStrategy(ObjectQuotingStrategy.valueOf(nodeObjectQuotingStrategy));
        }
        for (ParsedNode childNode : parsedNode.getChildren()) {
            handleChildNode(childNode, resourceAccessor, new HashMap<>());
        }
    }

    protected void expandExpressions(ParsedNode parsedNode) throws UnknownChangeLogParameterException {
        if (changeLogParameters == null) {
            return;
        }
        try {
            Object value = parsedNode.getValue();
            if ((value instanceof String)) {
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
        handleChildNode(node, resourceAccessor, new HashMap<>());
    }

    protected void handleChildNode(ParsedNode node, ResourceAccessor resourceAccessor, Map<String, Object> nodeScratch)
            throws ParsedNodeException, SetupException {
        expandExpressions(node);
        String nodeName = node.getName();
        switch (nodeName) {
            case "changeSet":
                if (isDbmsMatch(node.getChildValue(null, "dbms", String.class))) {
                    this.addChangeSet(createChangeSet(node, resourceAccessor));
                } else {
                    handleSkippedChangeSet(node);
                }
                break;
            case "modifyChangeSets":
                ModifyChangeSets modifyChangeSets = new ModifyChangeSets(
                        (String)node.getChildValue(null, "runWith"),
                        (String)node.getChildValue(null, "runWithSpoolFile"));
                nodeScratch = new HashMap<>();
                nodeScratch.put("modifyChangeSets", modifyChangeSets);
                for (ParsedNode modifyChildNode : node.getChildren()) {
                    handleChildNode(modifyChildNode, resourceAccessor, nodeScratch);
                }
                nodeScratch.remove("modifyChangeSets");
                break;
            case "include": {
                String path = node.getChildValue(null, "file", String.class);
                if (path == null) {
                    throw new UnexpectedLiquibaseException("No 'file' attribute on 'include'");
                }
                path = path.replace('\\', '/');
                ContextExpression includeContextFilter = new ContextExpression(node.getChildValue(null, "contextFilter", String.class));
                if (includeContextFilter.isEmpty()) {
                    includeContextFilter = new ContextExpression(node.getChildValue(null, "context", String.class));
                }
                Labels labels = new Labels(node.getChildValue(null, "labels", String.class));
                Boolean ignore = node.getChildValue(null, "ignore", Boolean.class);
                try {
                    include(path,
                            node.getChildValue(null, "relativeToChangelogFile", false),
                            resourceAccessor,
                            includeContextFilter,
                            labels,
                            ignore,
                            OnUnknownFileFormat.FAIL,
                            (ModifyChangeSets)nodeScratch.get("modifyChangeSets"));
                } catch (LiquibaseException e) {
                    throw new SetupException(e);
                }
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
                        resourceFilter = (IncludeAllFilter) Class.forName(resourceFilterDef).getConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new SetupException(e);
                    }
                }

                String resourceComparatorDef = node.getChildValue(null, "resourceComparator", String.class);
                Comparator<String> resourceComparator = null;
                if (resourceComparatorDef == null) {
                    resourceComparator = getStandardChangeLogComparator();
                } else {
                    try {
                        resourceComparator = (Comparator<String>) Class.forName(resourceComparatorDef).getConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        //take default comparator
                        Scope.getCurrentScope().getLog(getClass()).info("no resourceComparator defined - taking default " +
                                "implementation");
                        resourceComparator = getStandardChangeLogComparator();
                    }
                }

                ContextExpression includeContextFilter = new ContextExpression(node.getChildValue(null, "contextFilter", String.class));
                if (includeContextFilter.isEmpty()) {
                    includeContextFilter = new ContextExpression(node.getChildValue(null, "context", String.class));
                }
                Labels labels = new Labels(node.getChildValue(null, "labels", String.class));
                Boolean ignore = node.getChildValue(null, "ignore", Boolean.class);
                if (ignore == null) {
                    ignore = false;
                }
                includeAll(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceFilter,
                        node.getChildValue(null, "errorIfMissingOrEmpty", true),
                        resourceComparator,
                        resourceAccessor,
                        includeContextFilter,
                        labels,
                        ignore,
                        node.getChildValue(null, "minDepth", 1),
                        node.getChildValue(null, "maxDepth", Integer.MAX_VALUE),
                        (ModifyChangeSets)nodeScratch.get("modifyChangeSets"));
                break;
            }
            case "preConditions": {
                PreconditionContainer parsedContainer = new PreconditionContainer();
                parsedContainer.load(node, resourceAccessor);
                this.preconditionContainer.addNestedPrecondition(parsedContainer);

                break;
            }
            case "property": {
                try {
                    String contextFilter = node.getChildValue(null, "contextFilter", String.class);
                    if (StringUtil.isEmpty(contextFilter)) {
                        contextFilter = node.getChildValue(null, "context", String.class);
                    }
                    String dbms = node.getChildValue(null, "dbms", String.class);
                    String labels = node.getChildValue(null, "labels", String.class);
                    Boolean global = node.getChildValue(null, "global", Boolean.class);
                    if (global == null) {
                        // okay behave like liquibase < 3.4 and set global == true
                        global = true;
                    }

                    String file = node.getChildValue(null, "file", String.class);
                    Boolean relativeToChangelogFile = node.getChildValue(null, "relativeToChangelogFile", Boolean.FALSE);
                    Resource resource;

                    if (file == null) {
                        // direct referenced property, no file
                        String name = node.getChildValue(null, "name", String.class);
                        String value = node.getChildValue(null, "value", String.class);

                        this.changeLogParameters.set(name, value, contextFilter, labels, dbms, global, this);
                    } else {
                        // get relative path if specified
                        if (relativeToChangelogFile) {
                            resource = resourceAccessor.get(this.getPhysicalFilePath()).resolveSibling(file);
                        } else {
                            resource = resourceAccessor.get(file);
                        }

                        // read properties from the file
                        Properties props = new Properties();
                        if (!resource.exists()) {
                            Scope.getCurrentScope().getLog(getClass()).info("Could not open properties file " + file);
                        } else {
                            try (InputStream propertiesStream = resource.openInputStream()) {
                                props.load(propertiesStream);

                                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                                    this.changeLogParameters.set(
                                            entry.getKey().toString(),
                                            entry.getValue().toString(),
                                            contextFilter,
                                            labels,
                                            dbms,
                                            global,
                                            this
                                    );
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new ParsedNodeException(e);
                }

                break;
            }
            default:
                // we want to exclude child nodes that are not changesets or the other things
                // and avoid failing when encountering "child" nodes of the databaseChangeLog which are just
                // XML node attributes (like schemaLocation). If you don't understand, remove the if and run the tests
                // and look at the error output or review the "node" object here with a debugger.
                if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                    throw new ParsedNodeException("Unexpected node found under databaseChangeLog: " + nodeName);
                }
        }
    }

    //
    // Handle a mismatched DBMS attribute, if necessary
    //
    private void handleSkippedChangeSet(ParsedNode node) throws ParsedNodeException {
        if (node.getChildValue(null, "dbms", String.class) == null) {
            return;
        }
        String id = node.getChildValue(null, "id", String.class);
        String author = node.getChildValue(null, "author", String.class);
        String filePath = StringUtil.trimToNull(node.getChildValue(null, "logicalFilePath", String.class));
        if (filePath == null) {
            filePath = getFilePath();
        } else {
            filePath = filePath.replace("\\\\", "/").replaceFirst("^/", "");
        }
        String dbmsList = node.getChildValue(null, "dbms", String.class);
        ChangeSet skippedChangeSet =
            new ChangeSet(id, author, false, false, filePath, null, dbmsList, this);
        skippedChangeSets.add(skippedChangeSet);
    }

    public boolean isDbmsMatch(String dbmsList) {
        return isDbmsMatch(DatabaseList.toDbmsSet(dbmsList));
    }

    public boolean isDbmsMatch(Set<String> dbmsSet) {
        return dbmsSet == null
                || changeLogParameters == null
                || changeLogParameters.getValue("database.typeName", this) == null
                || DatabaseList.definitionMatches(dbmsSet, changeLogParameters.getValue("database.typeName", this).toString(), true);
    }

    /**
     * @deprecated Incorrect LabelExpression parameter. Kept for backwards compatibility
     */
    @Deprecated
    public void includeAll(String pathName,
                           boolean isRelativeToChangelogFile,
                           IncludeAllFilter resourceFilter,
                           boolean errorIfMissingOrEmpty,
                           Comparator<String> resourceComparator,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           LabelExpression labelExpression,
                           boolean ignore)
            throws SetupException {
        Labels labels = null;
        if (labelExpression != null && !labelExpression.isEmpty()) {
            labels = new Labels(labelExpression.toString());
        }
        includeAll(pathName,
                isRelativeToChangelogFile,
                resourceFilter,
                errorIfMissingOrEmpty,
                resourceComparator,
                resourceAccessor,
                includeContextFilter,
                labels,
                ignore,
                0,
                Integer.MAX_VALUE);
    }
    public void includeAll(String pathName,
                           boolean isRelativeToChangelogFile,
                           IncludeAllFilter resourceFilter,
                           boolean errorIfMissingOrEmpty,
                           Comparator<String> resourceComparator,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           boolean ignore,
                           int minDepth,
                           int maxDepth)
            throws SetupException {
        includeAll(pathName, isRelativeToChangelogFile, resourceFilter, errorIfMissingOrEmpty, resourceComparator,
                   resourceAccessor, includeContextFilter, labels, ignore, minDepth, maxDepth, new ModifyChangeSets(null, null));
    }

    public void includeAll(String pathName,
                           boolean isRelativeToChangelogFile,
                           IncludeAllFilter resourceFilter,
                           boolean errorIfMissingOrEmpty,
                           Comparator<String> resourceComparator,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           boolean ignore,
                           int minDepth,
                           int maxDepth,
                           ModifyChangeSets modifyChangeSets)
            throws SetupException {
        try {
            if (pathName == null) {
                throw new SetupException("No path attribute for includeAll");
            }

            String relativeTo = null;
            if (isRelativeToChangelogFile) {
                relativeTo = this.getPhysicalFilePath();
            }

            ResourceAccessor.SearchOptions searchOptions = initializeAndSetMinAndMaxDepth(minDepth, maxDepth);

            List<Resource> unsortedResources = null;
            Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
            try {
                String path;
                if (relativeTo == null) {
                    path = pathName;
                } else {
                    path = resourceAccessor.get(relativeTo).resolveSibling(pathName).getPath();
                    path = Paths.get(path).normalize().toString()
                            .replace("\\", "/");
                }

                path = path.replace("\\", "/");
                if (StringUtil.isNotEmpty(path) && !(path.endsWith("/"))) {
                    path = path + '/';
                }

                if (ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getCurrentValue()) {
                    if (seenChangelogPaths.contains(path)) {
                        throw new SetupException("Circular reference detected in '" + path + "'. Set " + ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getKey() + " if you'd like to ignore this error.");
                    }
                }
                seenChangelogPaths.add(path);
                LOG.fine("includeAll for " + pathName);
                LOG.fine("Using file opener for includeAll: " + resourceAccessor.toString());

                unsortedResources = resourceAccessor.search(path, searchOptions);
            } catch (IOException e) {
                if (errorIfMissingOrEmpty) {
                    throw e;
                }
            }
            SortedSet<Resource> resources = new TreeSet<>((o1, o2) -> resourceComparator.compare(o1.getPath(), o2.getPath()));
            if (unsortedResources != null) {
                for (Resource resourcePath : unsortedResources) {
                    if ((resourceFilter == null) || resourceFilter.include(resourcePath.getPath())) {
                        resources.add(resourcePath);
                    }
                }
            }

            if (resources.isEmpty() && errorIfMissingOrEmpty) {
                throw new SetupException(
                        "Could not find directory or directory was empty for includeAll '" + pathName + "'");
            }

            Scope.child(Collections.singletonMap(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, seenChangelogPaths), () -> {
                for (Resource resource : resources) {
                    Scope.getCurrentScope().getLog(getClass()).info("Reading resource: " + resource);
                    include(resource.getPath(), false, resourceAccessor, includeContextFilter,
                            labels, ignore, OnUnknownFileFormat.WARN, modifyChangeSets);
                }
            });
        } catch (Exception e) {
            throw new SetupException(e);
        }
    }

    /**
     * @deprecated Incorrect LabelExpression parameter. Kept for backwards compatibility
     */
    @Deprecated
    public boolean include(String fileName,
                           boolean isRelativePath,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           LabelExpression labelExpression,
                           Boolean ignore,
                           boolean logEveryUnknownFileFormat)
            throws LiquibaseException {
        Labels labels = null;
        if (labelExpression != null && !labelExpression.isEmpty()) {
            labels = new Labels(labelExpression.getLabels());
        }

        return include(fileName,
                isRelativePath,
                resourceAccessor,
                includeContextFilter,
                labels,
                ignore,
                logEveryUnknownFileFormat);
    }

    /**
     * @deprecated
     */
    public boolean include(String fileName,
                           boolean isRelativePath,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           boolean logEveryUnknownFileFormat)
            throws LiquibaseException {
        return include(fileName, isRelativePath, resourceAccessor, includeContextFilter, labels, ignore, logEveryUnknownFileFormat ? OnUnknownFileFormat.WARN : OnUnknownFileFormat.SKIP);
    }

    public boolean include(String fileName,
                           boolean isRelativePath,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           OnUnknownFileFormat onUnknownFileFormat)
            throws LiquibaseException {
        return include(fileName, isRelativePath, resourceAccessor, includeContextFilter, labels, ignore, onUnknownFileFormat, new ModifyChangeSets(null, null));
    }

    public boolean include(String fileName,
                           boolean isRelativePath,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           OnUnknownFileFormat onUnknownFileFormat,
                           ModifyChangeSets modifyChangeSets)
            throws LiquibaseException {
        if (".svn".equalsIgnoreCase(fileName) || "cvs".equalsIgnoreCase(fileName)) {
            return false;
        }

        if (isRelativePath) {
            try {
                fileName = resourceAccessor.get(this.getPhysicalFilePath()).resolveSibling(fileName).getPath();
                fileName = Paths.get(fileName).normalize().toString()
                        .replace("\\", "/");
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
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
                if(!resourceAccessor.get(fileName).exists()) {
                    if (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getCurrentValue().equals(ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN)) {
                        Scope.getCurrentScope().getLog(getClass()).warning(FileUtil.getFileNotFoundMessage(fileName));
                        return false;
                    } else {
                        throw new ChangeLogParseException(FileUtil.getFileNotFoundMessage(fileName));
                    }
                }
                ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor);
                changeLog = parser.parse(fileName, changeLogParameters, resourceAccessor);
                changeLog.setIncludeContextFilter(includeContextFilter);
                changeLog.setIncludeLabels(labels);
                changeLog.setIncludeIgnore(ignore != null ? ignore.booleanValue() : false);
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
            if (onUnknownFileFormat == OnUnknownFileFormat.FAIL) {
                throw e;
            }
            // This matches only an extension, but filename can be a full path, too. Is it right?
            boolean matchesFileExtension = StringUtil.trimToEmpty(fileName).matches("\\.\\w+$");
            if (matchesFileExtension || onUnknownFileFormat == OnUnknownFileFormat.WARN) {
                Scope.getCurrentScope().getLog(getClass()).warning(
                        "included file " + fileName + "/" + fileName + " is not a recognized file type"
                );
            }
            return false;
        }
        catch (IOException e) {
            throw new LiquibaseException(e.getMessage(), e);
        }
        PreconditionContainer preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == this.getPreconditions()) {
                this.setPreconditions(new PreconditionContainer());
            }
            this.getPreconditions().addNestedPrecondition(preconditions);
        }
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            if (changeSet.getRunWith() == null) {
                changeSet.setRunWith(modifyChangeSets != null ? modifyChangeSets.getRunWith() : null);
            }
            if (changeSet.getRunWithSpoolFile() == null) {
                changeSet.setRunWithSpoolFile(modifyChangeSets != null ? modifyChangeSets.getRunWithSpool() : null);
            }
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
        return Comparator.comparing(o -> o.replace("WEB-INF/classes/", ""));
    }

    public static String normalizePath(String filePath) {
        if (filePath == null) {
            return null;
        }

        if (filePath.startsWith("classpath:")) {
            filePath = filePath.substring("classpath:".length());
        }

        if (filePath.contains("\\")) {
            filePath = filePath.replace("\\", "/");
        }

        while (filePath.contains("//")) {
            filePath = filePath.replace("//", "/");
        }

        if (filePath.contains("/./")) {
            filePath = filePath.replace("/./", "/");
        }

        if (filePath.indexOf(":") == 1) {
            filePath = NO_LETTER_PATTERN.matcher(filePath).replaceFirst("");
        }

        if (filePath.startsWith(".")) {
            filePath = filePath.substring(1);
        }
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        return filePath;
    }

    public void clearCheckSums() {
        for (ChangeSet changeSet : getChangeSets()) {
            changeSet.clearCheckSum();
        }
    }

    /**
     *
     * Container class to handle the modifyChangeSets tag.
     * Other attributes may be added later
     *
     */
    private static class ModifyChangeSets {
        private final String runWith;
        private final String runWithSpool;

        /**
         *
         * @param  runWith                The native executor to execute all included change sets with. Can be null
         * @param  runWithSpool           The name of the spool file to be created
         *
         */
        public ModifyChangeSets(String runWith, String runWithSpool) {
            this.runWith = runWith;
            this.runWithSpool = runWithSpool;
        }

        public String getRunWith() {
            return runWith;
        }
        public String getRunWithSpool() { return runWithSpool; }
    }

    /**
     * Holder for the PreconditionContainer for this changelog, plus any nested changelogs.
     */
    @LiquibaseService(skip = true)
    private static class GlobalPreconditionContainer extends PreconditionContainer {

        /**
         * This container should always be TEST because it may contain a mix of containers which may or may not get tested during update-sql
         */
        @Override
        public OnSqlOutputOption getOnSqlOutput() {
            return OnSqlOutputOption.TEST;
        }

        @Override
        public void addNestedPrecondition(Precondition precondition) {
            super.addNestedPrecondition(precondition);
        }
    }

    /**
     * Controls what to do when including a file with a format that isn't recognized by a changelog parser.
     */
    public enum OnUnknownFileFormat {

        /**
         * Silently skip unknown files.
         */
        SKIP,

        /**
         * Log a warning about the file not being in a recognized format, but continue on
         */
        WARN,

        /**
         * Fail parsing with an error
         */
        FAIL
    }

    /**
     * Initialize and set min/max depth values validating maxDepth cannot be a lower value than minDepth
     * @param minDepth
     * @param maxDepth
     * @return ResourceAccessor.SearchOptions
     * @throws SetupException in case maxDepth is less than minDepth
     */
    private ResourceAccessor.SearchOptions initializeAndSetMinAndMaxDepth(int minDepth, int maxDepth) throws SetupException {
        ResourceAccessor.SearchOptions searchOptions = new ResourceAccessor.SearchOptions();
        try {
            if (maxDepth < minDepth) {
                throw new IllegalArgumentException("maxDepth argument must be greater than minDepth");
            }

            searchOptions.setMinDepth(minDepth);
            searchOptions.setMaxDepth(maxDepth);
        }
        catch (IllegalArgumentException e){
            throw new SetupException("Error in includeAll setup: "+ e.getMessage(), e);
        }
        return searchOptions;
    }

}
