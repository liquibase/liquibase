package liquibase.include;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.DatabaseChangeLog;
import static liquibase.changelog.DatabaseChangeLog.ENDS_WITH_FILTER;
import static liquibase.changelog.DatabaseChangeLog.ERROR_IF_MISSING_OR_EMPTY;
import static liquibase.changelog.DatabaseChangeLog.FILTER;
import static liquibase.changelog.DatabaseChangeLog.IGNORE;
import static liquibase.changelog.DatabaseChangeLog.INCLUDE_ALL_CHANGELOGS;
import static liquibase.changelog.DatabaseChangeLog.LABELS;
import static liquibase.changelog.DatabaseChangeLog.LOGICAL_FILE_PATH;
import static liquibase.changelog.DatabaseChangeLog.MAX_DEPTH;
import static liquibase.changelog.DatabaseChangeLog.MIN_DEPTH;
import static liquibase.changelog.DatabaseChangeLog.PATH;
import static liquibase.changelog.DatabaseChangeLog.RELATIVE_TO_CHANGELOG_FILE;
import static liquibase.changelog.DatabaseChangeLog.RESOURCE_COMPARATOR;
import static liquibase.changelog.DatabaseChangeLog.RESOURCE_FILTER;
import static liquibase.changelog.DatabaseChangeLog.SEEN_CHANGELOGS_PATHS_SCOPE_KEY;
import static liquibase.changelog.DatabaseChangeLog.normalizePath;
import liquibase.changelog.IncludeAllFilter;
import liquibase.changelog.ModifyChangeSets;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.SetupException;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
public class ChangeLogIncludeAll extends AbstractLiquibaseSerializable implements Conditional, ChangeLogChild {

    private static final Logger LOG = Scope.getCurrentScope().getLog(ChangeLogIncludeAll.class);
    private String path;
    private Boolean errorIfMissingOrEmpty;
    private Boolean relativeToChangelogFile;
    private IncludeAllFilter resourceFilter;
    private ContextExpression context;
    private Integer minDepth;
    private Integer maxDepth;
    private String endsWithFilter;
    private String logicalFilePath;
    private Labels labels;
    private Boolean ignore;
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private PreconditionContainer preconditions;
    private ResourceAccessor resourceAccessor;
    private DatabaseChangeLog parentChangeLog;
    private ModifyChangeSets modifyChangeSets;
    private final Database database = Scope.getCurrentScope().getDatabase();
    private List<DatabaseChangeLog> nestedChangeLogs = new ArrayList<>(10);
    private boolean markRan = false;

    public ChangeLogIncludeAll(ParsedNode node, ResourceAccessor resourceAccessor,
                            DatabaseChangeLog parentChangeLog, ModifyChangeSets modifyChangeSets)
				throws ParsedNodeException, SetupException {

        this.resourceAccessor = resourceAccessor;
        this.parentChangeLog = parentChangeLog;
        this.modifyChangeSets = modifyChangeSets;
        this.ignore = node.getChildValue(null, IGNORE, false);
        this.labels = new Labels(node.getChildValue(null, LABELS, String.class));
        this.logicalFilePath = node.getChildValue(null, LOGICAL_FILE_PATH, String.class);
        Boolean nodeRelativeToChangelogFile = node.getChildValue(null, RELATIVE_TO_CHANGELOG_FILE, Boolean.class);
        this.relativeToChangelogFile = (nodeRelativeToChangelogFile != null) ? nodeRelativeToChangelogFile : false;
        Boolean nodeErrorIfMissing = node.getChildValue(null, ERROR_IF_MISSING_OR_EMPTY, Boolean.class);
        this.errorIfMissingOrEmpty = (nodeErrorIfMissing != null) ? nodeErrorIfMissing : true;
        this.minDepth = node.getChildValue(null, MIN_DEPTH, 0);
        this.maxDepth = node.getChildValue(null, MAX_DEPTH, Integer.MAX_VALUE);
        this.endsWithFilter = node.getChildValue(null, ENDS_WITH_FILTER, "");
        this.context = ChangeLogIncludeUtils.getContextExpression(node);
        this.setFilterDef(node);
        this.path = node.getChildValue(null, PATH, String.class);
        this.preconditions = ChangeLogIncludeUtils.getPreconditions(node, resourceAccessor);
        this.setNestedChangeLogs(node);
    }

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("path", "errorIfMissingOrEmpty", "relativeToChangelogFile", "resourceFilter", "context", "minDepth", "maxDepth", "endsWithFilter","logicalFilePath"));
    }

    @Override
    public String getSerializedObjectName() {
        return INCLUDE_ALL_CHANGELOGS;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    void checkPreconditions() {
        PreconditionContainer preconditionContainer = this.getPreconditions();
        if(preconditionContainer != null) {

            for(Precondition p : preconditionContainer.getNestedPreconditions()) {
                String warningMessage = null;
                try {
                    warningMessage = String.format("Error occurred while evaluating precondition %s", p.getName());
                    p.check(this.getDatabase(), this.getParentChangeLog());
                } catch (PreconditionFailedException e) {
                    if (PreconditionContainer.FailOption.HALT.equals(preconditionContainer.getOnFail())) {
                        throw new RuntimeException(e);
                    } else if (PreconditionContainer.FailOption.WARN.equals(preconditionContainer.getOnFail())) {
                        ChangeLogIncludeUtils.sendIncludePreconditionWarningMessage(warningMessage, e);
                    } else if (PreconditionContainer.FailOption.MARK_RAN.equals(preconditionContainer.getOnFail())) {
                        this.setMarkRan(true);
                    } else {
                        this.nestedChangeLogs.clear();
                    }
                } catch (PreconditionErrorException e) {
                    if (PreconditionContainer.ErrorOption.HALT.equals(preconditionContainer.getOnError())) {
                        throw new RuntimeException(e);
                    } else if (PreconditionContainer.ErrorOption.WARN.equals(preconditionContainer.getOnError())) {
                        ChangeLogIncludeUtils.sendIncludePreconditionWarningMessage(warningMessage, e);
                    } else if (PreconditionContainer.ErrorOption.MARK_RAN.equals(preconditionContainer.getOnError())) {
                        this.setMarkRan(true);
                    }
                    else {
                        this.nestedChangeLogs.clear();
                    }
                }
            }
        }
    }

    private void setNestedChangeLogs(ParsedNode node) throws ParsedNodeException, SetupException {
        Comparator<String> comparator = this.determineResourceComparator(node);
        SortedSet<Resource> resources = this.findResources(comparator);
        if (resources.isEmpty() && errorIfMissingOrEmpty) {
            throw new SetupException(
                "Could not find directory or directory was empty for includeAll '" + this.path + "'");
        }

        try {
            Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
            Scope.child(Collections.singletonMap(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, seenChangelogPaths), () -> {
                for (Resource resource : resources) {
                    DatabaseChangeLog changeLog =
                        ChangeLogIncludeUtils.getChangeLog(this, resource.getPath());
                    if(changeLog != null)
                        this.nestedChangeLogs.add(changeLog);
                    LOG.info("Reading resource: " + resource);
                }
            });
        } catch (Exception e) {
            throw new SetupException(e);
        }

    }
    private String getAbsolutePath() throws SetupException {
        String path = this.path;
        if (path == null) {
            throw new SetupException("No path attribute for includeAll");
        }
        String relativeTo = null;
        if (this.relativeToChangelogFile) {
            relativeTo = this.parentChangeLog.getPhysicalFilePath();
        }
        if (relativeTo != null) {

				 try {
					path = resourceAccessor.get(relativeTo).resolveSibling(path).getPath();
				 } catch (IOException e) {
					throw new SetupException(e);
				 }
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

    private void setFilterDef(ParsedNode node) throws ParsedNodeException, SetupException {
        String resourceFilterDef = node.getChildValue(null, FILTER, String.class);
        if (resourceFilterDef == null) {
            resourceFilterDef = node.getChildValue(null, RESOURCE_FILTER, String.class);
        }
        if (resourceFilterDef != null) {
            try {
                this.resourceFilter = (IncludeAllFilter) Class.forName(resourceFilterDef).getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new SetupException(e);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private Comparator<String> determineResourceComparator(ParsedNode node) throws ParsedNodeException {
        String resourceComparatorDef = node.getChildValue(null, RESOURCE_COMPARATOR, String.class);
        Comparator<String> defaultComparator = Comparator.comparing(o -> o.replace("WEB-INF/classes/", ""));
        Comparator<String> resourceComparator;
        if (resourceComparatorDef == null) {
            resourceComparator = defaultComparator;
        } else {
            try {
                resourceComparator = (Comparator<String>) Class.forName(resourceComparatorDef).getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                Scope.getCurrentScope().getLog(getClass()).info("no resourceComparator defined - taking default " +
                    "implementation", e);
                resourceComparator = defaultComparator;
            }
        }
        return resourceComparator;
    }

    public SortedSet<Resource> findResources(Comparator<String> resourceComparator) throws SetupException {
        try {

            ResourceAccessor.SearchOptions searchOptions = initializeAndSetMinAndMaxDepth();
            searchOptions.setTrimmedEndsWithFilter(this.endsWithFilter);

            Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
            List<Resource> unsortedResources = getUnsortedResources(seenChangelogPaths, searchOptions);
            SortedSet<Resource> resources = new TreeSet<>((o1, o2) -> resourceComparator.compare(o1.getPath(), o2.getPath()));
            if (unsortedResources != null) {
                for (Resource resourcePath : unsortedResources) {
                    if ((this.resourceFilter == null) || this.resourceFilter.include(resourcePath.getPath())) {
                        resources.add(resourcePath);
                    }
                }
            }

            if (resources.isEmpty() && this.errorIfMissingOrEmpty) {
                throw new SetupException(
                    "Could not find directory, directory was empty, or no changelogs matched the provided search criteria for includeAll '" + this.path + "'");
            }
            return resources;
        } catch (IOException e) {
            throw new SetupException(e);
        }
    }

    private ResourceAccessor.SearchOptions initializeAndSetMinAndMaxDepth() throws SetupException {
        ResourceAccessor.SearchOptions searchOptions = new ResourceAccessor.SearchOptions();
        try {
            if (this.maxDepth < this.minDepth) {
                throw new IllegalArgumentException("maxDepth argument must be greater than minDepth");
            }

            searchOptions.setMinDepth(this.minDepth);
            searchOptions.setMaxDepth(this.maxDepth);
        } catch (IllegalArgumentException e) {
            throw new SetupException("Error in includeAll setup: " + e.getMessage(), e);
        }
        return searchOptions;
    }

    private List<Resource> getUnsortedResources(Set<String> seenChangelogPaths, ResourceAccessor.SearchOptions searchOptions) throws SetupException, IOException {
        List<Resource> unsortedResources = null;
        if(this.path == null)
            throw new SetupException("No path attribute for includeAll");
        String absolutePath = getAbsolutePath();
        try {
            if (Boolean.TRUE.equals(ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getCurrentValue())) {
                if (seenChangelogPaths.contains(absolutePath)) {
                    throw new SetupException("Circular reference detected in '" + absolutePath + "'. Set " + ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getKey() + " if you'd like to ignore this error.");
                }
            }
            seenChangelogPaths.add(absolutePath);
            LOG.fine("includeAll for " + absolutePath);
            LOG.fine("Using file opener for includeAll: " + this.resourceAccessor.toString());

            unsortedResources = this.resourceAccessor.search(absolutePath, searchOptions);
        } catch (IOException e) {
            if (this.errorIfMissingOrEmpty) {
                throw new IOException(String.format("Could not find/read changelogs from %s directory", absolutePath), e);
            }
        }
        return unsortedResources;
    }

}