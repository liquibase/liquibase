package liquibase.changelog;

import liquibase.*;
import liquibase.change.visitor.ChangeVisitor;
import liquibase.change.visitor.ChangeVisitorFactory;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.changelog.visitor.ValidatingVisitorGenerator;
import liquibase.changelog.visitor.ValidatingVisitorGeneratorFactory;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.logging.Logger;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcValue;
import liquibase.logging.mdc.customobjects.DuplicateChangesets;
import liquibase.logging.mdc.customobjects.MdcChangeset;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.ChangeLogParserConfiguration.MissingIncludeConfiguration;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.parser.core.ParserSupportedFileExtension;
import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.ExceptionUtil;
import liquibase.util.FileUtil;
import liquibase.util.LiquibaseLauncherSettings;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.trimToNull;

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
    private static final String CLASSPATH_PROTOCOL = "classpath:";
    public static final String SEEN_CHANGELOGS_PATHS_SCOPE_KEY = "SEEN_CHANGELOG_PATHS";
    public static final String FILE = "file";
    public static final String CONTEXT_FILTER = "contextFilter";
    public static final String CONTEXT = "context";
    public static final String LABELS = "labels";
    public static final String IGNORE = "ignore";
    public static final String RELATIVE_TO_CHANGELOG_FILE = "relativeToChangelogFile";

    public static final String LOGICAL_FILE_PATH = "logicalFilePath";
    public static final String ERROR_IF_MISSING = "errorIfMissing";
    public static final String MODIFY_CHANGE_SETS = "modifyChangeSets";
    public static final String PATH = "path";
    public static final String FILTER = "filter";
    public static final String RESOURCE_FILTER = "resourceFilter";
    public static final String RESOURCE_COMPARATOR = "resourceComparator";
    public static final String MIN_DEPTH = "minDepth";
    public static final String MAX_DEPTH = "maxDepth";
    public static final String ENDS_WITH_FILTER = "endsWithFilter";
    public static final String ERROR_IF_MISSING_OR_EMPTY = "errorIfMissingOrEmpty";
    public static final String CHANGE_SET = "changeSet";
    public static final String DBMS = "dbms";
    public static final String INCLUDE_CHANGELOG = "include";
    public static final String INCLUDE_ALL_CHANGELOGS = "includeAll";
    public static final String PRE_CONDITIONS = "preConditions";
    public static final String REMOVE_CHANGE_SET_PROPERTY = "removeChangeSetProperty";
    public static final String PROPERTY = "property";
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String GLOBAL = "global";

    private final PreconditionContainer preconditionContainer = new GlobalPreconditionContainer();

    @Getter
    @Setter
    private String physicalFilePath;
    @Setter
	 private String logicalFilePath;

    @Setter
	 @Getter
    private ObjectQuotingStrategy objectQuotingStrategy;

    @Getter
    private final List<ChangeVisitor> changeVisitors = new ArrayList<>();

    @Getter
    private final List<ChangeSet> changeSets = new ArrayList<>();
    @Getter
    private final List<ChangeSet> skippedChangeSets = new ArrayList<>();
    @Getter
    private final List<ChangeSet> skippedBecauseOfChangeDbmsChangeSets = new ArrayList<>();
    @Getter
    private final List<ChangeSet> skippedBecauseOfOsMismatchChangeSets = new ArrayList<>();
    @Getter
    private final List<ChangeSet> skippedBecauseOfLicenseChangeSets = new ArrayList<>();
    @Getter
    private final List<ChangeSet> skippedBecauseOfPreconditionsChangeSets = new ArrayList<>();

    @Getter
    @Setter
    @Accessors(chain = true)
    private ChangeLogParameters changeLogParameters;

    @Setter
	 @Getter
    private RuntimeEnvironment runtimeEnvironment;

    @Setter
	 private DatabaseChangeLog rootChangeLog = ROOT_CHANGE_LOG.get();

    @Setter
	 @Getter
    private DatabaseChangeLog parentChangeLog = PARENT_CHANGE_LOG.get();

    @Setter
	 @Getter
    private ContextExpression contextFilter;

    @Setter
	 @Getter
    private ContextExpression includeContextFilter;

    @Getter
    private Labels includeLabels;

    @Setter
	 @Getter
    private boolean includeIgnore;

    @Getter
    private ParsedNode currentlyLoadedChangeSetNode;

    public DatabaseChangeLog() {
    }

    public DatabaseChangeLog(String physicalFilePath) {
       this(physicalFilePath, new ChangeLogParameters());
    }

   public DatabaseChangeLog(String physicalFilePath, ChangeLogParameters changeLogParameters) {
      this.physicalFilePath = physicalFilePath;
      this.changeLogParameters = changeLogParameters;
   }

	public DatabaseChangeLog getRootChangeLog() {
        return (rootChangeLog != null) ? rootChangeLog : this;
    }

	@Override
   public PreconditionContainer getPreconditions() {
        return preconditionContainer;
    }

   @Override
   public void setPreconditions(PreconditionContainer precondition) {
       this.preconditionContainer.addNestedPrecondition(precondition);
   }

    public String getRawLogicalFilePath() {
        return logicalFilePath;
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

	public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return getLogicalFilePath();
        }
    }

	/**
     * @deprecated use {@link #getContextFilter()}
     */
    @Deprecated
    public ContextExpression getContexts() {
        return getContextFilter();
    }

    /**
     * @deprecated use {@link #setContextFilter(ContextExpression)}
     */
    @Deprecated
    public void setContexts(ContextExpression contexts) {
        setContextFilter(contexts);
    }

	/**
     * @deprecated Correct version is {@link #setIncludeLabels(Labels)}. Kept for backwards compatibility.
     */
    @Deprecated
    public void setIncludeLabels(LabelExpression labels) {
        this.includeLabels = new Labels(labels.toString());
    }

    public void setIncludeLabels(Labels labels) {
        this.includeLabels = labels;
    }

	/**
     * @deprecated use {@link #setIncludeContextFilter(ContextExpression)}
     */
    @Deprecated
    public void setIncludeContexts(ContextExpression includeContexts) {
        setIncludeContextFilter(includeContexts);
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
        if (possibleChangeSets.isEmpty()) {
            return null;
        }
        return possibleChangeSets.get(0);
    }

    public List<ChangeSet> getChangeSets(String path, String author, String id) {
        final ArrayList<ChangeSet> changeSetsToReturn = new ArrayList<>();
        final String normalizedPath = normalizePath(path);
        if (normalizedPath != null) {
            for (ChangeSet changeSet : this.changeSets) {
                if (changeSet.getAuthor().equalsIgnoreCase(author) && changeSet.getId().equalsIgnoreCase(id) && isDbmsMatch(changeSet.getDbmsSet())) {
                    final String changesetNormalizedPath = normalizePath(changeSet.getFilePath());
                    if (changesetNormalizedPath != null && changesetNormalizedPath.equalsIgnoreCase(normalizedPath)) {
                        changeSetsToReturn.add(changeSet);
                    }
                }
            }
        }
        return changeSetsToReturn;
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

        ValidatingVisitorGeneratorFactory validatingVisitorGeneratorFactory = Scope.getCurrentScope().getSingleton(ValidatingVisitorGeneratorFactory.class);
        ValidatingVisitorGenerator generator = validatingVisitorGeneratorFactory.getValidatingVisitorGenerator();
        ValidatingVisitor validatingVisitor = generator.generateValidatingVisitor(database.getRanChangeSetList());
        validatingVisitor.validate(database, this);
        logIterator.run(validatingVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        for (String message : validatingVisitor.getWarnings().getMessages()) {
            log.warning(message);
        }

        if (!validatingVisitor.validationPassed()) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED);
            List<MdcChangeset> duplicateChangesetsMdc = validatingVisitor.getDuplicateChangeSets().stream().map(MdcChangeset::fromChangeset).collect(Collectors.toList());
            Scope.getCurrentScope().addMdcValue(MdcKey.DUPLICATE_CHANGESETS, new DuplicateChangesets(duplicateChangesetsMdc));
            Scope.getCurrentScope().getLog(getClass()).info("Change failed validation!");
            throw new ValidationFailedException(validatingVisitor);
        }
    }

    public ChangeSet  getChangeSet(RanChangeSet ranChangeSet) {
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

    public DatabaseChangeLog load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        ExceptionUtil.doSilently(() -> {
            String physicalFilePathLowerCase = this.physicalFilePath.toLowerCase();
            if (ParserSupportedFileExtension.JSON_SUPPORTED_EXTENSIONS.stream().anyMatch(physicalFilePathLowerCase::endsWith)) {
                Scope.getCurrentScope().getAnalyticsEvent().incrementJsonChangelogCount();
            } else if (ParserSupportedFileExtension.XML_SUPPORTED_EXTENSIONS.stream().anyMatch(physicalFilePathLowerCase::endsWith)) {
                Scope.getCurrentScope().getAnalyticsEvent().incrementXmlChangelogCount();
            } else if (ParserSupportedFileExtension.YAML_SUPPORTED_EXTENSIONS.stream().anyMatch(physicalFilePathLowerCase::endsWith)) {
                Scope.getCurrentScope().getAnalyticsEvent().incrementYamlChangelogCount();
            }
        });
        setLogicalFilePath(parsedNode.getChildValue(null, LOGICAL_FILE_PATH, String.class));
        setContextFilter(determineContextExpression(parsedNode));
        String nodeObjectQuotingStrategy = parsedNode.getChildValue(null, "objectQuotingStrategy", String.class);
        if (nodeObjectQuotingStrategy != null) {
            setObjectQuotingStrategy(ObjectQuotingStrategy.valueOf(nodeObjectQuotingStrategy));
        }
        for (ParsedNode childNode : parsedNode.getChildren()) {
            if (childNode.getName().equals((new ChangeSet(null)).getSerializedObjectName())) {
                this.currentlyLoadedChangeSetNode = childNode;
            }
            handleChildNode(childNode, resourceAccessor, new HashMap<>());
        }
        this.currentlyLoadedChangeSetNode = null;
        return this;
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
        handleChildNodeHelper(node, resourceAccessor, nodeScratch);
    }

    public void handleChildNodeHelper(ParsedNode node, ResourceAccessor resourceAccessor, Map<String, Object> nodeScratch)
            throws ParsedNodeException, SetupException {
        expandExpressions(node);
        String nodeName = node.getName();
        switch (nodeName) {
            case CHANGE_SET:
                handleDbmsAttribute(node, resourceAccessor);
                break;
            case MODIFY_CHANGE_SETS:
                handleModifyChangeSets(node, resourceAccessor);
                break;
            case INCLUDE_CHANGELOG: {
                handleInclude(node, resourceAccessor, nodeScratch);
                break;
            }
            case INCLUDE_ALL_CHANGELOGS: {
                handleIncludeAll(node, resourceAccessor, nodeScratch);
                break;
            }
            case PRE_CONDITIONS: {
                handlePrecondition(node, resourceAccessor);
                break;
            }
            case REMOVE_CHANGE_SET_PROPERTY: {
                handleRemoveChangeSet(node, resourceAccessor);
                break;
            }
            case PROPERTY: {
                handleProperty(node, resourceAccessor);
                break;
            }
            default:
                // we want to exclude child nodes that are not changesets or the other things
                // and avoid failing when encountering "child" nodes of the databaseChangeLog which are just
                // XML node attributes (like schemaLocation). If you don't understand, remove the if-condition and run the tests
                // and look at the error output or review the "node" object here with a debugger.
                if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                    throw new ParsedNodeException("Unexpected node found under databaseChangeLog: " + nodeName);
                }
        }
    }

    private void handlePrecondition(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        PreconditionContainer parsedContainer = new PreconditionContainer();
        parsedContainer.load(node, resourceAccessor);
        this.preconditionContainer.addNestedPrecondition(parsedContainer);
    }
    protected void throwIf(ValidationErrors errs) throws ParsedNodeException {
        if(errs.hasErrors()) {
            throw new ParsedNodeException(errs.toString());
        }
    }

    public static boolean getRelativeToChangelog(ParsedNode node, String file) throws ParsedNodeException {
        return node.getChildValue(null, RELATIVE_TO_CHANGELOG_FILE, file.startsWith("."));
    }

    private void handleProperty(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        try {
            ContextExpression propertyContextFilter = determineContextExpression(node);
            String dbms = node.getChildValue(null, DBMS, String.class);
            Labels labels = new Labels(node.getChildValue(null, LABELS, String.class));
            boolean global = node.getChildValue(null, GLOBAL, true ); // behave like liquibase < 3.4 and set global == true by default
            String file = node.getChildValue(null, FILE, String.class);

            // direct referenced property, no file
            String name = node.getChildValue(null, NAME, String.class);
            String value = node.getChildValue(null, VALUE, String.class);
            ValidationErrors validationErrors = new ValidationErrors(PROPERTY);
            if (file == null) {
                if (name == null && value == null) {
                   throw new ParsedNodeException("Either 'file' or 'name' + 'value' must be set for 'property'");
                }
                throwIf(validationErrors.checkRequiredField(NAME, name));
                throwIf(validationErrors.checkRequiredField(VALUE, value, " when 'name' is set", true));

                this.changeLogParameters.set(name, value, propertyContextFilter, labels, dbms, global, this);
            } else {
                throwIf(validationErrors.checkRequiredField(FILE, file));
                if(null != value || null != name) {
                    Scope.getCurrentScope().getLog(getClass()).warning(
                          "If 'file' is set for 'property', then '" + VALUE + "' and '"
                                + NAME + "' are ignored");
                }

                file = file.trim();
                Resource resource = resourceAccessor.getExistingFile(file,
                          getRelativeToChangelog(node, file) ? getPhysicalFilePath() : null,
                          " set as property:file in '" + getPhysicalFilePath() + "'"
                    );

                try (InputStream propertiesStream = resource.openInputStream()) {
                    // read properties from the file
                    Properties props = new Properties();
                    props.load(propertiesStream);

                    for (Map.Entry<Object, Object> entry : props.entrySet()) {
                        this.changeLogParameters.set(
                                entry.getKey().toString(),
                                entry.getValue().toString(),
                                propertyContextFilter,
                                labels,
                                dbms,
                                global,
                                this
                        );
                    }
                }
            }
        } catch (FileNotFoundException e) {
            if (node.getChildValue(null, ERROR_IF_MISSING, true)) {
                throw new ParsedNodeException(e.getMessage());
            } else {
                Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage());
            }
        } catch (IOException e) {
            throw new ParsedNodeException(e);
        }
    }

    private void handleRemoveChangeSet(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        List<ParsedNode> childNodes = node.getChildren();
        Optional<ParsedNode> changeNode = childNodes.stream().filter(n -> n.getName().equalsIgnoreCase("change")).findFirst();
        if(changeNode.isPresent()){
            ChangeVisitor changeVisitor = ChangeVisitorFactory.getInstance().create((String) changeNode.get().getValue());
            if(changeVisitor != null){
                changeVisitor.load(node, resourceAccessor);
                if(DatabaseList.definitionMatches(changeVisitor.getDbms(), changeLogParameters.getDatabase(), false)) {
                    //add changeVisitor to this changeLog only if the running database matches with one of the removeChangeSetProperty's dbms
                    getChangeVisitors().add(changeVisitor);
                }
            }
        }
    }

    private void handleIncludeAll(ParsedNode node, ResourceAccessor resourceAccessor, Map<String, Object> nodeScratch)
            throws ParsedNodeException, SetupException {
        String path = node.getChildValue(null, PATH, String.class);
        throwIf(new ValidationErrors(INCLUDE_ALL_CHANGELOGS).checkRequiredField(PATH, path));
        String resourceFilterDef = node.getChildValue(null, FILTER, String.class);
        if (resourceFilterDef == null) {
            resourceFilterDef = node.getChildValue(null, RESOURCE_FILTER, String.class);
        }
        IncludeAllFilter resourceFilter = null;
        if (resourceFilterDef != null) {
            try {
                resourceFilter = (IncludeAllFilter) Class.forName(resourceFilterDef, true, Thread.currentThread().getContextClassLoader()).getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new SetupException(e);
            }
        }

        String resourceComparatorDef = node.getChildValue(null, RESOURCE_COMPARATOR, String.class);
        Comparator<String> resourceComparator = determineResourceComparator(resourceComparatorDef);

        includeAll(path,
                   getRelativeToChangelog(node, path), resourceFilter,
                   node.getChildValue(null, ERROR_IF_MISSING_OR_EMPTY, true),
                   resourceComparator,
                   resourceAccessor,
                   determineContextExpression(node),
                   new Labels(node.getChildValue(null, LABELS, String.class)),
                   node.getChildValue(null, IGNORE, false),
                   node.getChildValue(null, LOGICAL_FILE_PATH, String.class),
                   node.getChildValue(null, MIN_DEPTH, 0),
                   node.getChildValue(null, MAX_DEPTH, Integer.MAX_VALUE),
                   node.getChildValue(null, ENDS_WITH_FILTER, ""),
                (ModifyChangeSets) nodeScratch.get(MODIFY_CHANGE_SETS));
    }

    private void handleInclude(ParsedNode node, ResourceAccessor resourceAccessor, Map<String, Object> nodeScratch)
            throws ParsedNodeException, SetupException {
        String path = node.getChildValue(null, FILE, String.class);
        throwIf(new ValidationErrors(INCLUDE_CHANGELOG).checkRequiredField(FILE, path));
        path = path.trim().replace('\\', '/');
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, path);
        ContextExpression includeNodeContextFilter = determineContextExpression(node);
        Labels labels = new Labels(node.getChildValue(null, LABELS, String.class));
        try {
            include(path,
                    getRelativeToChangelog(node, path),
                    node.getChildValue(null, ERROR_IF_MISSING, true),
                    resourceAccessor,
                    includeNodeContextFilter,
                    labels,
                    node.getChildValue(null, IGNORE, false),
                    node.getChildValue(null, LOGICAL_FILE_PATH, String.class),
                    OnUnknownFileFormat.FAIL,
                    (ModifyChangeSets) nodeScratch.get(MODIFY_CHANGE_SETS));
        } catch (LiquibaseException e) {
            throw new SetupException(e);
        }
    }

    private static ContextExpression determineContextExpression(ParsedNode node) throws ParsedNodeException {
        ContextExpression includeNodeContextFilter = new ContextExpression(node.getChildValue(null, CONTEXT_FILTER, String.class));
        if (includeNodeContextFilter.isEmpty()) {
            includeNodeContextFilter = new ContextExpression(node.getChildValue(null, CONTEXT, String.class));
        }
        return includeNodeContextFilter;
    }

    private void handleModifyChangeSets(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        Map<String, Object> nodeScratch;
        ModifyChangeSets modifyChangeSets = createModifyChangeSets(node);
        nodeScratch = new HashMap<>();
        nodeScratch.put(MODIFY_CHANGE_SETS, modifyChangeSets);
        for (ParsedNode modifyChildNode : node.getChildren()) {
            handleChildNode(modifyChildNode, resourceAccessor, nodeScratch);
        }
        nodeScratch.remove(MODIFY_CHANGE_SETS);
    }

    private void handleDbmsAttribute(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        if (isDbmsMatch(node.getChildValue(null, DBMS, String.class))) {
            this.addChangeSet(createChangeSet(node, resourceAccessor));
        } else {
            handleSkippedChangeSet(node);
        }
    }

    public Comparator<String> determineResourceComparator(String resourceComparatorDef) {
        Comparator<String> resourceComparator;
        if (resourceComparatorDef == null) {
            resourceComparator = getStandardChangeLogComparator();
        } else {
            try {
                resourceComparator = (Comparator<String>) Class.forName(resourceComparatorDef, true, Thread.currentThread().getContextClassLoader()).getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                //take default comparator
                Scope.getCurrentScope().getLog(getClass()).info("no resourceComparator defined - taking default " +
                        "implementation", e);
                resourceComparator = getStandardChangeLogComparator();
            }
        }
        return resourceComparator;
    }

    private ModifyChangeSets createModifyChangeSets(ParsedNode node) throws ParsedNodeException {
        ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
        ChangeSetService service = factory.createChangeSetService();
        return service.createModifyChangeSets(node);
    }

    //
    // Handle a mismatched DBMS attribute, if necessary
    //
    private void handleSkippedChangeSet(ParsedNode node) throws ParsedNodeException {
        if (node.getChildValue(null, DBMS, String.class) == null) {
            return;
        }
        String id = node.getChildValue(null, "id", String.class);
        String author = node.getChildValue(null, "author", String.class);
        String filePath = trimToNull(node.getChildValue(null, LOGICAL_FILE_PATH, String.class));
        if (filePath == null) {
            filePath = getFilePath();
        } else {
            filePath = filePath.replace("\\\\", "/").replaceFirst("^/", "");
        }
        String dbmsList = node.getChildValue(null, DBMS, String.class);
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
     * @deprecated use {@link DatabaseChangeLog#includeAll(String, boolean, IncludeAllFilter, boolean, Comparator, ResourceAccessor, ContextExpression, Labels, boolean, String, int, int)}
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
                null,
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
                           String logicalFilePath,
                           int minDepth,
                           int maxDepth)
            throws SetupException {
        ChangeSetService changeSetService = ChangeSetServiceFactory.getInstance().createChangeSetService();
        ModifyChangeSets modifyChangeSets = changeSetService.createModifyChangeSets(null, null, false);
        includeAll(pathName, isRelativeToChangelogFile, resourceFilter, errorIfMissingOrEmpty, resourceComparator,
                   resourceAccessor, includeContextFilter, labels, ignore, logicalFilePath, minDepth, maxDepth, "", modifyChangeSets);
    }

    /**
     *
     * @deprecated use {@link DatabaseChangeLog#includeAll(String, boolean, IncludeAllFilter, boolean, Comparator, ResourceAccessor, ContextExpression, Labels, boolean, String, int, int, String, ModifyChangeSets) throws SetupException}
     *
     */
    @Deprecated
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
        includeAll(pathName, isRelativeToChangelogFile, resourceFilter, errorIfMissingOrEmpty, resourceComparator,
                resourceAccessor, includeContextFilter, labels, ignore, null, minDepth, maxDepth, "", modifyChangeSets);
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
                           String logicalFilePath,
                           int minDepth,
                           int maxDepth,
                           String endsWithFilter,
                           ModifyChangeSets modifyChangeSets)
            throws SetupException {
        if (pathName == null) {
            throw new SetupException("No path attribute for includeAll");
        }
        SortedSet<Resource> resources =
                findResources(pathName, isRelativeToChangelogFile, resourceFilter, errorIfMissingOrEmpty, resourceComparator, resourceAccessor, minDepth, maxDepth, endsWithFilter);
        if (resources.isEmpty() && errorIfMissingOrEmpty) {
            throw new SetupException(
                    "Could not find directory or directory was empty for includeAll '" + pathName + "'");
        }
        try {
            Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
            Scope.child(Collections.singletonMap(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, seenChangelogPaths), () -> {
                for (Resource resource : resources) {
                    Scope.getCurrentScope().getLog(getClass()).info("Reading resource: " + resource);
                    include(resource.getPath(), false, errorIfMissingOrEmpty, resourceAccessor, includeContextFilter,
                            labels, ignore, logicalFilePath, OnUnknownFileFormat.WARN, modifyChangeSets);
                }
            });
        } catch (Exception e) {
            throw new SetupException(e);
        }
    }

    /**
     *
     * @deprecated use {@link DatabaseChangeLog#include(String, boolean, boolean, ResourceAccessor, ContextExpression, Labels, Boolean, String, OnUnknownFileFormat)}
     *
     */
    @Deprecated
    public SortedSet<Resource> findResources(
            String pathName,
            boolean isRelativeToChangelogFile,
            IncludeAllFilter resourceFilter,
            boolean errorIfMissingOrEmpty,
            Comparator<String> resourceComparator,
            ResourceAccessor resourceAccessor,
            int minDepth,
            int maxDepth) throws SetupException {
        return findResources(pathName, isRelativeToChangelogFile, resourceFilter, errorIfMissingOrEmpty, resourceComparator,
                resourceAccessor, minDepth, maxDepth, "");
    }

    public SortedSet<Resource> findResources(
                               String pathName,
                               boolean isRelativeToChangelogFile,
                               IncludeAllFilter resourceFilter,
                               boolean errorIfMissingOrEmpty,
                               Comparator<String> resourceComparator,
                               ResourceAccessor resourceAccessor,
                               int minDepth,
                               int maxDepth,
                               String endsWithFilter
                               ) throws SetupException {
        try {
            if (pathName == null) {
                throw new SetupException("No path attribute for findResources");
            }

            String relativeTo = null;
            if (isRelativeToChangelogFile) {
                relativeTo = this.getPhysicalFilePath();
            }

            ResourceAccessor.SearchOptions searchOptions = initializeAndSetMinAndMaxDepth(minDepth, maxDepth);
            searchOptions.setTrimmedEndsWithFilter(endsWithFilter);

            Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
            List<Resource> unsortedResources = getUnsortedResources(pathName, errorIfMissingOrEmpty, resourceAccessor, relativeTo, seenChangelogPaths, searchOptions);
            SortedSet<Resource> resources = new TreeSet<>((o1, o2) -> resourceComparator.compare(o1.getPath(), o2.getPath()));
            if (unsortedResources != null) {
                URI liquibaseHomeInternalUri = getLiquibaseHomeInternalUri();
                for (Resource resourcePath : unsortedResources) {
                    // if the resource is inside a jar located in liquibaseHomeUri/internal, we don't want to include it
                    if (liquibaseHomeInternalUri != null && resourcePath.getUri().toString().startsWith("jar:" + liquibaseHomeInternalUri.toString())) {
                        Scope.getCurrentScope().getLog(getClass()).info("Skipping resource from jar file in liquibase home internal: " + resourcePath);
                        continue;
                    }
                    if ((resourceFilter == null) || resourceFilter.include(resourcePath.getPath())) {
                        resources.add(resourcePath);
                    }
                }
            }

            if (resources.isEmpty() && errorIfMissingOrEmpty) {
                throw new SetupException(
                        "Could not find directory, directory was empty, or no changelogs matched the provided search criteria for includeAll '" + pathName + "'");
            }
            return resources;
        } catch (IOException e) {
            throw new SetupException(e);
        }
    }

    private URI getLiquibaseHomeInternalUri() {
        String liquibaseHome = LiquibaseLauncherSettings.getSetting(LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_HOME);
        URI liquibaseHomeUri = null;
        if (liquibaseHome != null) {
            try {
                liquibaseHomeUri = Paths.get(liquibaseHome + File.separator + "internal").toUri();
            } catch (InvalidPathException e) {
                Scope.getCurrentScope().getLog(DatabaseChangeLog.class).warning("Invalid LIQUIBASE_HOME path: " + liquibaseHome, e);
            }
        }
        return liquibaseHomeUri;
    }

    private List<Resource> getUnsortedResources(String pathName, boolean errorIfMissingOrEmpty, ResourceAccessor resourceAccessor, String relativeTo, Set<String> seenChangelogPaths, ResourceAccessor.SearchOptions searchOptions) throws SetupException, IOException {
        List<Resource> unsortedResources = null;
        try {
            String path = fixPath(pathName, resourceAccessor, relativeTo);

            if (Boolean.TRUE.equals(ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getCurrentValue())) {
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
                throw new IOException(String.format("Could not find/read changelogs from %s directory", pathName), e);
            }
        }
        return unsortedResources;
    }

    private String fixPath(String pathName, ResourceAccessor resourceAccessor, String relativeTo) throws IOException {
        String path;
        if (relativeTo == null) {
            path = pathName;
        } else {
            path = resourceAccessor.get(relativeTo).resolveSibling(pathName).getPath();
            path = normalizePath(path);
        }

        if(path != null) {
            path = path.replace("\\", "/");
            if (StringUtils.isNotEmpty(path) && !(path.endsWith("/"))) {
                path = path + '/';
            }
        }
        return path;
    }

    /**
     * @deprecated use {@link DatabaseChangeLog#include(String, boolean, boolean, ResourceAccessor, ContextExpression, Labels, Boolean, String, OnUnknownFileFormat)}
     */
    @Deprecated
    public boolean include(String fileName,
                           boolean isRelativePath,
                           boolean errorIfMissing,
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
                errorIfMissing,
                resourceAccessor,
                includeContextFilter,
                labels,
                ignore,
                logEveryUnknownFileFormat);
    }

    /**
     * @deprecated use {@link DatabaseChangeLog#include(String, boolean, boolean, ResourceAccessor, ContextExpression, Labels, Boolean, String, OnUnknownFileFormat)}
     */
    @Deprecated
    public boolean include(String fileName,
                           boolean isRelativePath,
                           boolean errorIfMissing,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           boolean logEveryUnknownFileFormat)
            throws LiquibaseException {
        return include(fileName, isRelativePath, errorIfMissing, resourceAccessor, includeContextFilter, labels, ignore, null, logEveryUnknownFileFormat ? OnUnknownFileFormat.WARN : OnUnknownFileFormat.SKIP);
    }

    /**
     * @deprecated use {@link DatabaseChangeLog#include(String, boolean, boolean, ResourceAccessor, ContextExpression, Labels, Boolean, String, OnUnknownFileFormat)}
     */
    public boolean include(String fileName,
                           boolean isRelativePath,
                           boolean errorIfMissing,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           OnUnknownFileFormat onUnknownFileFormat)
            throws LiquibaseException {
        return include(fileName, isRelativePath, errorIfMissing, resourceAccessor, includeContextFilter, labels, ignore, null, onUnknownFileFormat, new ModifyChangeSets(null, null));
    }

    public boolean include(String fileName,
                           boolean isRelativePath,
                           boolean errorIfMissing,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           String logicalFilePath,
                           OnUnknownFileFormat onUnknownFileFormat)
            throws LiquibaseException {
        return include(fileName, isRelativePath, errorIfMissing, resourceAccessor, includeContextFilter, labels, ignore, logicalFilePath, onUnknownFileFormat, new ModifyChangeSets(null, null, false));
    }

    public boolean include(String fileName,
                           boolean isRelativePath,
                           boolean errorIfMissing,
                           ResourceAccessor resourceAccessor,
                           ContextExpression includeContextFilter,
                           Labels labels,
                           Boolean ignore,
                           String logicalFilePath,
                           OnUnknownFileFormat onUnknownFileFormat,
                           ModifyChangeSets modifyChangeSets)
            throws LiquibaseException {
        if (".svn".equalsIgnoreCase(fileName) || "cvs".equalsIgnoreCase(fileName)) {
            return false;
        }

        try {
            Resource res = resourceAccessor.getExistingFile(fileName,
                  isRelativePath ? getPhysicalFilePath() : null,
                  " set as include:file in '" + getPhysicalFilePath() + "'"
            );
            if(isRelativePath) {
                fileName = normalizePath(normalizePathViaPaths(res.getPath(), false));
            }
        } catch (IOException e) {
            if (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getCurrentValue().equals(
                                                                MissingIncludeConfiguration.WARN)
                      || !errorIfMissing) {
                    Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage());
                    return false;
            } else {
                throw new ChangeLogParseException(e.getMessage());
            }
        }
        final String normalizedFilePath = fileName;
        ChangeLogParser parser = null;
        DatabaseChangeLog changeLog;
        try {
            DatabaseChangeLog rootChangeLogInstance = ROOT_CHANGE_LOG.get();
            if (rootChangeLogInstance == null) {
                ROOT_CHANGE_LOG.set(this);
            }
            DatabaseChangeLog parentChangeLogInstance = PARENT_CHANGE_LOG.get();
            PARENT_CHANGE_LOG.set(this);
            try {

                parser = ChangeLogParserFactory.getInstance().getParser(normalizedFilePath, resourceAccessor);

                if (modifyChangeSets != null) {
                    // Some parser need to know it's not a top level changelog, in modifyChangeSets flow 'runWith' attributes are added later on
                    ChangeLogParser finalParser = parser;
                    changeLog = Scope.child(Collections.singletonMap(MODIFY_CHANGE_SETS, true),
                            () -> finalParser.parse(normalizedFilePath, changeLogParameters, resourceAccessor));
                } else {
                    changeLog = parser.parse(normalizedFilePath, changeLogParameters, resourceAccessor);
                }
                changeLog.setIncludeContextFilter(includeContextFilter);
                changeLog.setIncludeLabels(labels);
                changeLog.setIncludeIgnore(ignore != null && ignore);
            } finally {
                if (rootChangeLogInstance == null) {
                    ROOT_CHANGE_LOG.remove();
                }
                if (parentChangeLogInstance == null) {
                    PARENT_CHANGE_LOG.remove();
                } else {
                    PARENT_CHANGE_LOG.set(parentChangeLogInstance);
                }
            }
        } catch (UnknownChangelogFormatException e) {
            if (onUnknownFileFormat == OnUnknownFileFormat.FAIL) {
                throw e;
            }
            // This matches only an extension, but filename can be a full path, too. Is it right?
            boolean matchesFileExtension = StringUtils.trimToEmpty(normalizedFilePath).matches("\\.\\w+$");
            if (matchesFileExtension || onUnknownFileFormat == OnUnknownFileFormat.WARN) {
                Scope.getCurrentScope().getLog(getClass()).warning(
                        "included file " + normalizedFilePath + "/" + normalizedFilePath + " is not a recognized file type", e
                );
            }
            return false;
        } catch (Exception e) {
            throw new LiquibaseException(e.getMessage(), e);
        }
        PreconditionContainer preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == this.getPreconditions()) {
                this.setPreconditions(new PreconditionContainer());
            }
            this.getPreconditions().addNestedPrecondition(preconditions);
        }

        List<RanChangeSet> ranChangeSets = new ArrayList<>();
        Database database = Scope.getCurrentScope().getDatabase();
        if (database != null && database.getConnection() != null) {
            Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).init();
            ranChangeSets = database.getRanChangeSetList();
        }

        String actualLogicalFilePath = getActualLogicalFilePath(logicalFilePath, changeLog);

        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            if (modifyChangeSets != null) {
                modifyChangeSets(modifyChangeSets, changeSet);
            }

            //
            // Do not update the logical file path if the change set has
            // already been executed because this would cause the addition
            // of another DBCL entry.  Also, skip setting the logical file
            // path for raw SQL change sets
            //
            if (actualLogicalFilePath != null && changeSet.getLogicalFilePath() == null &&
                ! (parser instanceof SqlChangeLogParser) && ! ranChangeSetExists(changeSet, ranChangeSets)) {
                changeSet.setLogicalFilePath(actualLogicalFilePath);
                if (StringUtils.isNotEmpty(actualLogicalFilePath)) {
                    changeSet.setFilePath(actualLogicalFilePath);
                }
            }
            addChangeSet(changeSet);
        }
        skippedChangeSets.addAll(changeLog.getSkippedChangeSets());

        return true;
    }

    /**
     * Search for the closest logicalfilePath for this changelog
     */
    private String getActualLogicalFilePath(String logicalFilePath, DatabaseChangeLog changeLog) {
        DatabaseChangeLog currentChangeLog = changeLog;
        do {
            if (StringUtils.isNotBlank(currentChangeLog.getRawLogicalFilePath())) {
                return currentChangeLog.getRawLogicalFilePath();
            }
        } while ((currentChangeLog = currentChangeLog.getParentChangeLog()) != null);

        if (StringUtils.isNotBlank(this.getRawLogicalFilePath())) {
            return this.getRawLogicalFilePath();
        }
        return logicalFilePath;
    }

    /**
     *
     * Return true if there is a RanChangeSet instance for the change set
     *
     * @param  changeSet                 The ChangeSet in question
     * @param  ranChangeSets             The list of RanChangeSet to iterate
     * @return boolean
     *
     */
    private boolean ranChangeSetExists(ChangeSet changeSet, List<RanChangeSet> ranChangeSets) {
        Optional<RanChangeSet> ranChangeSet =
            ranChangeSets.stream().filter( rc ->
                rc.getId().equals(changeSet.getId()) &&
                rc.getAuthor().equals(changeSet.getAuthor()) &&
                rc.getStoredChangeLog().equals(changeSet.getFilePath())).findFirst();
        return ranChangeSet.isPresent();
    }

    private void modifyChangeSets(ModifyChangeSets modifyChangeSets, ChangeSet changeSet) {
        ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
        ChangeSetService service = factory.createChangeSetService();
        service.modifyChangeSets(changeSet, modifyChangeSets);
    }

    protected ChangeSet createChangeSet(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
        ChangeSetService service = factory.createChangeSetService();
        ChangeSet changeSet = service.createChangeSet(this);
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

        if (!GlobalConfiguration.PRESERVE_CLASSPATH_PREFIX_IN_NORMALIZED_PATHS.getCurrentValue() && filePath.startsWith(CLASSPATH_PROTOCOL)) {
            filePath = filePath.substring("classpath:".length());
        }

        if (filePath.contains("\\")) {
            filePath = filePath.replace("\\", "/");
        }

        if (filePath.indexOf(":") == 1) {
            filePath = NO_LETTER_PATTERN.matcher(filePath).replaceFirst("");
        }

        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        String normalized = FilenameUtils.normalizeNoEndSeparator(filePath);
        /*
        Commons IO will return null if the double dot has no parent path segment to work with. In this case,
        we fall back to path normalization using Paths.get(), which might fail on Windows.
         */
        if (normalized == null) {
            normalized = normalizePathViaPaths(filePath, true);
        }
        
        if (normalized == null) {
            return null;
        }

        filePath = normalized;

        if (filePath.contains("\\")) {
            filePath = filePath.replace("\\", "/");
        }

        return filePath;
    }

    private static String normalizePathViaPaths(String filePath, boolean normalizePath) {
        if (filePath == null) {
            return null;
        }

        // preserve URL protocol when normalizing the path
        boolean classpathUrl = filePath.startsWith(CLASSPATH_PROTOCOL);
        Path path = classpathUrl
                ? Paths.get(filePath.substring(CLASSPATH_PROTOCOL.length()))
                : Paths.get(filePath);

        Path normalizedPath = normalizePath ? path.normalize() : path;
        return classpathUrl ? CLASSPATH_PROTOCOL + normalizedPath : normalizedPath.toString();
    }

    public void clearCheckSums() {
        for (ChangeSet changeSet : getChangeSets()) {
            changeSet.clearCheckSum();
        }
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
     *
     * Initialize and set min/max depth values validating maxDepth cannot be a lower value than minDepth
     *
     * @param minDepth            The minDepth for searches
     * @param maxDepth            The maxDepth for searches
     * @return ResourceAccessor.SearchOptions
     * @throws SetupException in case maxDepth is less than minDepth
     *
     */
    private ResourceAccessor.SearchOptions initializeAndSetMinAndMaxDepth(int minDepth, int maxDepth) throws SetupException {
        ResourceAccessor.SearchOptions searchOptions = new ResourceAccessor.SearchOptions();
        try {
            if (maxDepth < minDepth) {
                throw new IllegalArgumentException("maxDepth argument must be greater than minDepth");
            }

            searchOptions.setMinDepth(minDepth);
            searchOptions.setMaxDepth(maxDepth);
        } catch (IllegalArgumentException e) {
            throw new SetupException("Error in includeAll setup: " + e.getMessage(), e);
        }
        return searchOptions;
    }
}
