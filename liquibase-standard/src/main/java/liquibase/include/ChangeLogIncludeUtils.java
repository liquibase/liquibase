package liquibase.include;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import static liquibase.changelog.DatabaseChangeLog.CONTEXT;
import static liquibase.changelog.DatabaseChangeLog.CONTEXT_FILTER;
import static liquibase.changelog.DatabaseChangeLog.FILTER;
import static liquibase.changelog.DatabaseChangeLog.MODIFY_CHANGE_SETS;
import static liquibase.changelog.DatabaseChangeLog.PRE_CONDITIONS;
import static liquibase.changelog.DatabaseChangeLog.RESOURCE_COMPARATOR;
import static liquibase.changelog.DatabaseChangeLog.RESOURCE_FILTER;
import static liquibase.changelog.DatabaseChangeLog.SEEN_CHANGELOGS_PATHS_SCOPE_KEY;
import static liquibase.changelog.DatabaseChangeLog.normalizePath;

import liquibase.changelog.IncludeAllFilter;
import liquibase.changelog.ModifyChangeSets;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.database.Database;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownChangelogFormatException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.logging.Logger;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.ui.UIService;
import liquibase.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

/**
	<p>
	* A final, non-instantiable utility class that assists the {@link liquibase.changelog.visitor.IncludeVisitor}
	* during the {@link DatabaseChangeLog} flattening process.
	</p>
	*
	* This class is responsible for various tasks, including propagating the {@code MARK_RUN} state
	* to underlying {@link ChangeSet} instances when necessary.
	* @author <a href="https://github.com/cagliostro92">Edoardo Patti</a>
	*/
public final class ChangeLogIncludeUtils {

	private static final String CLASSPATH_PROTOCOL = "classpath:";
	private static final Logger LOG = Scope.getCurrentScope().getLog(ChangeLogIncludeUtils.class);
	private static final Comparator<ChangeSet> CHANGESET_COMPARATOR = Comparator.comparingInt(ChangeSet::getOrder);

	private ChangeLogIncludeUtils() {}

	public static void flatChangeLogChangeSets(DatabaseChangeLog changeLog) {
		SortedSet<ChangeSet> changeSetAccumulator = new TreeSet<>(CHANGESET_COMPARATOR);
		SortedSet<ChangeSet> skippedChangeSetAccumulator = new TreeSet<>(CHANGESET_COMPARATOR);
		changeLog.getIncludeList().forEach(i -> flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator));
		changeLog.getIncludeAllList().forEach(i -> flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator));
		changeSetAccumulator.addAll(changeLog.getChangeSets());
		skippedChangeSetAccumulator.addAll(changeLog.getSkippedChangeSets());
		changeLog.getChangeSets().clear();
		changeLog.getSkippedChangeSets().clear();
		changeSetAccumulator.forEach(changeLog::addChangeSet);
		changeLog.getSkippedChangeSets().addAll(skippedChangeSetAccumulator);
	}

	static void sendIncludePreconditionWarningMessage(String message, Throwable e) {
		Scope currentScope = Scope.getCurrentScope();
		Logger logger = currentScope.getLog(DatabaseChangeLog.class);
		UIService ui = currentScope.getUI();
		logger.warning(message, e);
		ui.sendMessage(message);
	}

	static ContextExpression getContextExpression(ParsedNode node) throws ParsedNodeException {
		ContextExpression includeNodeContextFilter = new ContextExpression(node.getChildValue(null, CONTEXT_FILTER, String.class));
		if (includeNodeContextFilter.isEmpty()) {
			includeNodeContextFilter = new ContextExpression(node.getChildValue(null, CONTEXT, String.class));
		}
		return includeNodeContextFilter;
	}

	static DatabaseChangeLog getChangeLog(ChangeLogInclude include) throws SetupException {
		String normalizedFilePath = normalizeFilePath(include.getFile(), include.getParentChangeLog().getPhysicalFilePath(), include.getRelativeToChangelogFile(), include.getResourceAccessor());
		return getChangeLog(normalizedFilePath, include.getResourceAccessor(), include.getErrorIfMissing(),
				include.getModifyChangeSets(), include.getParentChangeLog(), include.getContext(), include.getLabels(), include.getIgnore(), DatabaseChangeLog.OnUnknownFileFormat.FAIL);
	}

	static PreconditionContainer getPreconditions(ParsedNode node, ResourceAccessor resourceAccessor)
			throws ParsedNodeException {
		PreconditionContainer result = null;
		ParsedNode preconditionsNode = node.getChild(null, PRE_CONDITIONS);
		if (preconditionsNode != null) {
			result = new PreconditionContainer();
			result.load(preconditionsNode, resourceAccessor);
		}
		return result;
	}

	static void setNestedChangeLogs(ParsedNode node, ChangeLogIncludeAll includeAll) throws ParsedNodeException, SetupException {
		Comparator<String> comparator = ChangeLogIncludeUtils.determineResourceComparator(node);
		SortedSet<Resource> resources = ChangeLogIncludeUtils.findResources(comparator, includeAll);
		if (resources.isEmpty() && includeAll.getErrorIfMissingOrEmpty()) {
			throw new SetupException(
					"Could not find directory or directory was empty for includeAll '" + includeAll.getPath() + "'");
		}

		try {
			Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
			Scope.child(Collections.singletonMap(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, seenChangelogPaths), () -> {
				for (Resource resource : resources) {
					DatabaseChangeLog changeLog =
							ChangeLogIncludeUtils.getChangeLog(includeAll, resource.getPath());
					if(changeLog != null)
						includeAll.getNestedChangeLogs().add(changeLog);
					LOG.info("Reading resource: " + resource);
				}
			});
		} catch (Exception e) {
			throw new SetupException(e);
		}

	}

	static IncludeAllFilter getFilterDef(ParsedNode node) throws ParsedNodeException, SetupException {
		String resourceFilterDef = node.getChildValue(null, FILTER, String.class);
		if (resourceFilterDef == null) {
			resourceFilterDef = node.getChildValue(null, RESOURCE_FILTER, String.class);
		}
		if (resourceFilterDef != null) {
			try {
				return (IncludeAllFilter) Class.forName(resourceFilterDef).getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw new SetupException(e);
			}
		}
		return null;
	}

	private static DatabaseChangeLog getChangeLog(ChangeLogIncludeAll include, String file) throws SetupException {
		return getChangeLog(file, include.getResourceAccessor(), include.getErrorIfMissingOrEmpty(),
				include.getModifyChangeSets(), include.getParentChangeLog(), include.getContext(), include.getLabels(), include.getIgnore(), DatabaseChangeLog.OnUnknownFileFormat.WARN);
	}

	private static String normalizeFilePath(String file, String parentChangelogPhysicalFilePath, boolean isRelative, ResourceAccessor resourceAccessor) {
		if (file == null) {
			throw new UnexpectedLiquibaseException("No 'file' attribute on 'include'");
		}
		file = file.replace('\\', '/');
		if (isRelative) {
			try {
				file = resourceAccessor.get(parentChangelogPhysicalFilePath).resolveSibling(file).getPath();
				file = normalizePath(normalizePathViaPaths(file));
			} catch (IOException e) {
				throw new UnexpectedLiquibaseException(e);
			}
		}
		return file;
	}

	private static String normalizePathViaPaths(String filePath) {
		if (filePath == null) {
			return null;
		}
		boolean classpathUrl = filePath.startsWith(CLASSPATH_PROTOCOL);
		Path path = classpathUrl
				? Paths.get(filePath.substring(CLASSPATH_PROTOCOL.length()))
				: Paths.get(filePath);

		return classpathUrl ? CLASSPATH_PROTOCOL + path : path.toString();
	}

	private static void flatChangeLogChangeSets(ChangeLogInclude include,
																																													SortedSet<ChangeSet> changeSetAccumulator,
																																													SortedSet<ChangeSet> skippedChangeSetAccumulator) {

		include.checkPreconditions();
		DatabaseChangeLog changeLog = include.getNestedChangelog();
		if(include.isMarkRan())
			propagateMarkRan(Collections.singletonList(changeLog));

		if(changeLog != null) {
			changeSetAccumulator.addAll(ChangeLogIncludeUtils.getNestedChangeSets(include));
			skippedChangeSetAccumulator.addAll(ChangeLogIncludeUtils.getNestedSkippedChangeSets(include));
			flatChangeLogChangeSets(changeSetAccumulator, skippedChangeSetAccumulator, changeLog);
		}
	}

	private static void flatChangeLogChangeSets(ChangeLogIncludeAll includeAll,
																																													SortedSet<ChangeSet> changeSetAccumulator,
																																													SortedSet<ChangeSet> skippedChangeSetAccumulator) {

		includeAll.checkPreconditions();
		if(includeAll.isMarkRan())
			propagateMarkRan(includeAll.getNestedChangeLogs());

		if(!includeAll.getNestedChangeLogs().isEmpty()) {
			changeSetAccumulator.addAll(ChangeLogIncludeUtils.getNestedChangeSets(includeAll));
			skippedChangeSetAccumulator.addAll(ChangeLogIncludeUtils.getNestedSkippedChangeSets(includeAll));
			includeAll.getNestedChangeLogs().forEach(changeLog ->
					flatChangeLogChangeSets(changeSetAccumulator, skippedChangeSetAccumulator, changeLog));
		}
	}

	private static void propagateMarkRan(List<DatabaseChangeLog> changeLogs) {
		changeLogs.forEach(cl -> {
			cl.getIncludeList().forEach(i -> i.setPreconditions(getMarkRanPrecondition()));
			cl.getIncludeAllList().forEach(ia -> ia.setPreconditions(getMarkRanPrecondition()));
		});
	}

	private static void flatChangeLogChangeSets(SortedSet<ChangeSet> changeSetAccumulator,
																																													SortedSet<ChangeSet> skippedChangeSetAccumulator,
																																													DatabaseChangeLog changeLog) {
		for(ChangeLogInclude i : changeLog.getIncludeList()) {
			flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator);
		}
		for(ChangeLogIncludeAll i : changeLog.getIncludeAllList()) {
			flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator);
		}
	}

	private static SortedSet<ChangeSet> getNestedChangeSets(ChangeLogInclude include) {
		return getNestedChangeSets(include.getDatabase(), include.getLogicalFilePath(),
				include.getNestedChangelog(), include.getModifyChangeSets(), include.isMarkRan());
	}

	private static SortedSet<ChangeSet> getNestedChangeSets(ChangeLogIncludeAll includeAll) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		includeAll.getNestedChangeLogs().forEach(changelog -> result.addAll(getNestedChangeSets(includeAll.getDatabase(), includeAll.getLogicalFilePath(),
				changelog, includeAll.getModifyChangeSets(),
				includeAll.isMarkRan())));
		return result;
	}

	private static SortedSet<ChangeSet> getNestedSkippedChangeSets(ChangeLogInclude include) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		result.addAll(include.getNestedChangelog().getSkippedChangeSets());
		return result;
	}

	private static SortedSet<ChangeSet> getNestedSkippedChangeSets(ChangeLogIncludeAll includeAll) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		includeAll.getNestedChangeLogs().forEach(changelog -> result.addAll(changelog.getSkippedChangeSets()));
		return result;
	}

	private static DatabaseChangeLog getChangeLog(String file, ResourceAccessor resourceAccessor, Boolean errorIfMissing,
																																															ModifyChangeSets modifyChangeSets, DatabaseChangeLog parentChangeLog,
																																															ContextExpression contextExpression, Labels labels, Boolean ignore,
																																															DatabaseChangeLog.OnUnknownFileFormat onUnknownFileFormat)
			throws SetupException {
		if (".svn".equalsIgnoreCase(file) || "cvs".equalsIgnoreCase(file)) {
			return null;
		}
		try {
			if (!resourceAccessor.get(file).exists()) {
				if (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getCurrentValue()
						.equals(ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN)
						|| !errorIfMissing) {
					Scope.getCurrentScope().getLog(ChangeLogIncludeUtils.class).warning(FileUtil.getFileNotFoundMessage(file));
					return null;
				} else {
					throw new ChangeLogParseException(FileUtil.getFileNotFoundMessage(file));
				}
			}
		} catch (IOException | ChangeLogParseException e) {
			throw new SetupException(e);
		}
		try (MdcObject mdcObject = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, file)) {
			DatabaseChangeLog childChangeLog;
			ChangeLogParser parser =
					ChangeLogParserFactory.getInstance().getParser(file, resourceAccessor);
			if (modifyChangeSets != null) {
				childChangeLog = Scope.child(Collections.singletonMap(MODIFY_CHANGE_SETS, true),
						() -> parser.parse(file, parentChangeLog.getChangeLogParameters(),
								resourceAccessor));
			} else {
				childChangeLog = parser.parse(file, parentChangeLog.getChangeLogParameters(),
						resourceAccessor);
			}
			prepareParentChangeLogs(childChangeLog, parentChangeLog, contextExpression, labels, ignore);
			return childChangeLog;
		}
		catch (UnknownChangelogFormatException e) {
			if (onUnknownFileFormat == DatabaseChangeLog.OnUnknownFileFormat.FAIL) {
				throw new SetupException(e);
			}
			boolean matchesFileExtension = StringUtils.trimToEmpty(file).matches("\\.\\w+$");
			if (matchesFileExtension || onUnknownFileFormat == DatabaseChangeLog.OnUnknownFileFormat.WARN) {
				Scope.getCurrentScope().getLog(ChangeLogIncludeUtils.class).warning(
						"included file " + file + "/" + file + " is not a recognized file type", e
				);
			}
			return null;
		}

		catch (Exception e) {
			throw new SetupException(e);
		}
	}

	private static void prepareParentChangeLogs(DatabaseChangeLog childChangelog,
																																													DatabaseChangeLog parentChangeLog,
																																													ContextExpression contextExpression,
																																													Labels labels,
																																													Boolean ignore) {
		childChangelog.setIncludeContextFilter(contextExpression);
		childChangelog.setIncludeLabels(labels);
		childChangelog.setIncludeIgnore(ignore != null && ignore);
		PreconditionContainer preconditions = childChangelog.getPreconditions();
		if (preconditions != null) {
			if (null == parentChangeLog.getPreconditions()) {
				parentChangeLog.setPreconditions(new PreconditionContainer());
			}
			parentChangeLog.getPreconditions().addNestedPrecondition(preconditions);
		}
	}

	private static SortedSet<ChangeSet> getNestedChangeSets(Database database, String logicalFilePath,
																																																									DatabaseChangeLog childChangeLog, ModifyChangeSets modifyChangeSets,
																																																									boolean markRan) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		List<RanChangeSet> ranChangeSets = new ArrayList<>(1);
		if (database != null && logicalFilePath != null) {
			try {
				ranChangeSets = database.getRanChangeSetList();
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			}
		}
		for (ChangeSet changeSet : childChangeLog.getChangeSets()) {
			if (modifyChangeSets != null) {
				modifyChangeSets(changeSet, modifyChangeSets);
			}
			String changeSetLogicalFilePath = changeSet.getLogicalFilePath();

			boolean isRawSql = childChangeLog.getPhysicalFilePath() != null
					&& childChangeLog.getPhysicalFilePath().endsWith(".sql");

			if (logicalFilePath != null && changeSetLogicalFilePath == null
					&&!isRawSql && !ranChangeSetExists(changeSet, ranChangeSets)) {
				changeSet.setLogicalFilePath(logicalFilePath);
				if (StringUtils.isNotEmpty(logicalFilePath)) {
					changeSet.setFilePath(logicalFilePath);
				}
			}
			if(markRan)
				changeSet.setPreconditions(getMarkRanPrecondition());

			result.add(changeSet);
		}
		return result;
	}

	private static void modifyChangeSets(ChangeSet changeSet, ModifyChangeSets modifyChangeSets) {
		ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
		ChangeSetService service = factory.createChangeSetService();
		service.modifyChangeSets(changeSet, modifyChangeSets);
	}

	private static boolean ranChangeSetExists(ChangeSet changeSet, List<RanChangeSet> ranChangeSets) {
		Optional<RanChangeSet> ranChangeSet =
				ranChangeSets.stream().filter( rc ->
						rc.getId().equals(changeSet.getId()) &&
								rc.getAuthor().equals(changeSet.getAuthor()) &&
								rc.getStoredChangeLog().equals(changeSet.getFilePath())).findFirst();
		return ranChangeSet.isPresent();
	}

	private static PreconditionContainer getMarkRanPrecondition() {
		PreconditionContainer result = new PreconditionContainer();
		result.setOnFail(PreconditionContainer.FailOption.MARK_RAN);
		result.addNestedPrecondition(getFailedPrecondition());
		return result;
	}

	@SuppressWarnings({"unchecked"})
	private static Comparator<String> determineResourceComparator(ParsedNode node) throws ParsedNodeException {
		String resourceComparatorDef = node.getChildValue(null, RESOURCE_COMPARATOR, String.class);
		Comparator<String> defaultComparator = Comparator.comparing(o -> o.replace("WEB-INF/classes/", ""));
		Comparator<String> resourceComparator;
		if (resourceComparatorDef == null) {
			resourceComparator = defaultComparator;
		} else {
			try {
				resourceComparator = (Comparator<String>) Class.forName(resourceComparatorDef).getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				Scope.getCurrentScope().getLog(ChangeLogIncludeAll.class).info("no resourceComparator defined - taking default " +
						"implementation", e);
				resourceComparator = defaultComparator;
			}
		}
		return resourceComparator;
	}

	private static SortedSet<Resource> findResources(Comparator<String> resourceComparator, ChangeLogIncludeAll includeAll) throws SetupException {
		try {

			ResourceAccessor.SearchOptions searchOptions = initializeAndSetMinAndMaxDepth(includeAll);
			searchOptions.setTrimmedEndsWithFilter(includeAll.getEndsWithFilter());

			Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
			List<Resource> unsortedResources = getUnsortedResources(seenChangelogPaths, searchOptions, includeAll);
			SortedSet<Resource> resources = new TreeSet<>((o1, o2) -> resourceComparator.compare(o1.getPath(), o2.getPath()));
			if (unsortedResources != null) {
				for (Resource resourcePath : unsortedResources) {
					if (includeAll.getResourceFilter() == null || includeAll.getResourceFilter().include(resourcePath.getPath())) {
						resources.add(resourcePath);
					}
				}
			}

			if (resources.isEmpty() && includeAll.getErrorIfMissingOrEmpty()) {
				throw new SetupException(
						"Could not find directory, directory was empty, or no changelogs matched the provided search criteria for includeAll '" + includeAll.getPath() + "'");
			}
			return resources;
		} catch (IOException e) {
			throw new SetupException(e);
		}
	}

	private static ResourceAccessor.SearchOptions initializeAndSetMinAndMaxDepth(ChangeLogIncludeAll includeAll) throws SetupException {
		ResourceAccessor.SearchOptions searchOptions = new ResourceAccessor.SearchOptions();
		try {
			if (includeAll.getMaxDepth() < includeAll.getMinDepth()) {
				throw new IllegalArgumentException("maxDepth argument must be greater than minDepth");
			}

			searchOptions.setMinDepth(includeAll.getMinDepth());
			searchOptions.setMaxDepth(includeAll.getMaxDepth());
		} catch (IllegalArgumentException e) {
			throw new SetupException("Error in includeAll setup: " + e.getMessage(), e);
		}
		return searchOptions;
	}

	private static List<Resource> getUnsortedResources(Set<String> seenChangelogPaths, ResourceAccessor.SearchOptions searchOptions,
																																																				ChangeLogIncludeAll includeAll) throws SetupException, IOException {
		List<Resource> unsortedResources = null;
		if(includeAll.getPath() == null)
			throw new SetupException("No path attribute for includeAll");
		String absolutePath = getAbsolutePath(includeAll);
		try {
			if (Boolean.TRUE.equals(ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getCurrentValue())) {
				if (seenChangelogPaths.contains(absolutePath)) {
					throw new SetupException("Circular reference detected in '" + absolutePath + "'. Set " + ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getKey() + " if you'd like to ignore this error.");
				}
			}
			seenChangelogPaths.add(absolutePath);
			LOG.fine("includeAll for " + absolutePath);
			LOG.fine("Using file opener for includeAll: " + includeAll.getResourceAccessor().toString());

			unsortedResources = includeAll.getResourceAccessor().search(absolutePath, searchOptions);
		} catch (IOException e) {
			if (includeAll.getErrorIfMissingOrEmpty()) {
				throw new IOException(String.format("Could not find/read changelogs from %s directory", absolutePath), e);
			}
		}
		return unsortedResources;
	}

	private static String getAbsolutePath(ChangeLogIncludeAll includeAll) throws SetupException {
		String path = includeAll.getPath();
		if (path == null) {
			throw new SetupException("No path attribute for includeAll");
		}
		String relativeTo = null;
		if (includeAll.getRelativeToChangelogFile()) {
			relativeTo = includeAll.getParentChangeLog().getPhysicalFilePath();
		}
		if (relativeTo != null) {

			try {
				path = includeAll.getResourceAccessor().get(relativeTo).resolveSibling(path).getPath();
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

	private static Precondition getFailedPrecondition() {

		return new Precondition() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public Warnings warn(Database database) {
				return null;
			}

			@Override
			public ValidationErrors validate(Database database) {
				return null;
			}

			@Override
			public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet,
																					ChangeExecListener changeExecListener)
					throws PreconditionFailedException {

				throw new PreconditionFailedException(new ArrayList<>());
			}

			@Override
			public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) {

			}

			@Override
			public String getSerializedObjectName() {
				return "";
			}

			@Override
			public Set<String> getSerializableFields() {
				return new HashSet<>();
			}

			@Override
			public Object getSerializableFieldValue(String field) {
				return null;
			}

			@Override
			public SerializationType getSerializableFieldType(String field) {
				return null;
			}

			@Override
			public String getSerializableFieldNamespace(String field) {
				return "";
			}

			@Override
			public String getSerializedObjectNamespace() {
				return "";
			}

			@Override
			public ParsedNode serialize() {
				return null;
			}
		};
	}
}