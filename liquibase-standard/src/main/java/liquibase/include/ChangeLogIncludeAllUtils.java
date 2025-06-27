package liquibase.include;

import static liquibase.changelog.DatabaseChangeLog.FILTER;
import static liquibase.changelog.DatabaseChangeLog.RESOURCE_COMPARATOR;
import static liquibase.changelog.DatabaseChangeLog.RESOURCE_FILTER;
import static liquibase.changelog.DatabaseChangeLog.SEEN_CHANGELOGS_PATHS_SCOPE_KEY;
import static liquibase.changelog.DatabaseChangeLog.normalizePath;
import static liquibase.include.ChangeLogIncludeHelper.CHANGESET_COMPARATOR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.IncludeAllFilter;
import liquibase.exception.SetupException;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.StringUtils;

/**
	* @author <a href="https://github.com/cagliostro92">Edoardo Patti</a>
	*/
final class ChangeLogIncludeAllUtils {

	private static final Logger LOG = Scope.getCurrentScope().getLog(ChangeLogIncludeAllUtils.class);

	private ChangeLogIncludeAllUtils() { }

	static void flatChangeLogChangeSets(ChangeLogIncludeAll includeAll,
																																					SortedSet<ChangeSet> changeSetAccumulator,
																																					SortedSet<ChangeSet> skippedChangeSetAccumulator) {

		includeAll.checkPreconditions();
		if(includeAll.isMarkRan())
			ChangeLogIncludeHelper.propagateMarkRan(includeAll.getNestedChangeLogs());

		if(!includeAll.getNestedChangeLogs().isEmpty()) {
			changeSetAccumulator.addAll(getNestedChangeSets(includeAll));
			skippedChangeSetAccumulator.addAll(getNestedSkippedChangeSets(includeAll));
			includeAll.getNestedChangeLogs().forEach(changeLog ->
					ChangeLogIncludeHelper.flatChangeLogChangeSets(changeSetAccumulator, skippedChangeSetAccumulator, changeLog));
		}
	}

	static List<DatabaseChangeLog> getNestedChangeLogs(ParsedNode node, ChangeLogIncludeAll includeAll) throws ParsedNodeException, SetupException {
		List<DatabaseChangeLog> result = new ArrayList<>();
		Comparator<String> comparator = determineResourceComparator(node);
		SortedSet<Resource> resources = findResources(comparator, includeAll);
		if (resources.isEmpty() && Boolean.TRUE.equals(includeAll.getErrorIfMissingOrEmpty())) {
			throw new SetupException(
					"Could not find directory or directory was empty for includeAll '" + includeAll.getPath() + "'");
		}

		try {
			Set<String> seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
			Scope.child(Collections.singletonMap(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, seenChangelogPaths), () -> {
				for (Resource resource : resources) {
					DatabaseChangeLog changeLog =
							getChangeLog(includeAll, resource.getPath());
					if(changeLog != null)
						result.add(changeLog);
					LOG.info("Reading resource: " + resource);
				}
			});
			return result;
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
		return ChangeLogIncludeHelper.getChangeLog(file, include.getResourceAccessor(), include.getErrorIfMissingOrEmpty(),
				include.getModifyChangeSets(), include.getParentChangeLog(), include.getContext(), include.getLabels(), include.getIgnore(), DatabaseChangeLog.OnUnknownFileFormat.WARN);
	}

	@SuppressWarnings({"unchecked"})
	private static Comparator<String> determineResourceComparator(ParsedNode node) throws
			ParsedNodeException {
		String resourceComparatorDef = node.getChildValue(null, RESOURCE_COMPARATOR, String.class);
		Comparator<String> defaultComparator = Comparator.comparing(o -> o.replace("WEB-INF/classes/", ""));
		Comparator<String> resourceComparator;
		if (resourceComparatorDef == null) {
			resourceComparator = defaultComparator;
		} else {
			try {
				resourceComparator = (Comparator<String>) Class.forName(resourceComparatorDef).getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				LOG.info("no resourceComparator defined - taking default " +
						"implementation", e);
				resourceComparator = defaultComparator;
			}
		}
		return resourceComparator;
	}

	private static SortedSet<Resource> findResources(Comparator<String> resourceComparator, ChangeLogIncludeAll includeAll) throws
			SetupException {
		try {

			ResourceAccessor.SearchOptions searchOptions = initializeAndSetMinAndMaxDepth(includeAll);
			searchOptions.setTrimmedEndsWithFilter(includeAll.getEndsWithFilter());

			Set<String>
					seenChangelogPaths = Scope.getCurrentScope().get(SEEN_CHANGELOGS_PATHS_SCOPE_KEY, new HashSet<>());
			List<Resource>
					unsortedResources = getUnsortedResources(seenChangelogPaths, searchOptions, includeAll);
			SortedSet<Resource> resources = new TreeSet<>((o1, o2) -> resourceComparator.compare(o1.getPath(), o2.getPath()));
			if (unsortedResources != null) {
				for (Resource resourcePath : unsortedResources) {
					if (includeAll.getResourceFilter() == null || includeAll.getResourceFilter().include(resourcePath.getPath())) {
						resources.add(resourcePath);
					}
				}
			}

			if (resources.isEmpty() && Boolean.TRUE.equals(includeAll.getErrorIfMissingOrEmpty())) {
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
			if (Boolean.TRUE.equals(ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getCurrentValue()) && seenChangelogPaths.contains(absolutePath)) {
				throw new SetupException("Circular reference detected in '" + absolutePath + "'. Set " + ChangeLogParserConfiguration.ERROR_ON_CIRCULAR_INCLUDE_ALL.getKey() + " if you'd like to ignore this error.");
			}
			seenChangelogPaths.add(absolutePath);
			LOG.fine("includeAll for " + absolutePath);
			LOG.fine("Using file opener for includeAll: " + includeAll.getResourceAccessor().toString());

			unsortedResources = includeAll.getResourceAccessor().search(absolutePath, searchOptions);
		} catch (IOException e) {
			if (Boolean.TRUE.equals(includeAll.getErrorIfMissingOrEmpty())) {
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
		if (Boolean.TRUE.equals(includeAll.getRelativeToChangelogFile())) {
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

	private static SortedSet<ChangeSet> getNestedChangeSets(ChangeLogIncludeAll includeAll) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		includeAll.getNestedChangeLogs().forEach(changelog -> result.addAll(ChangeLogIncludeHelper.getNestedChangeSets(includeAll.getDatabase(), includeAll.getLogicalFilePath(),
				changelog, includeAll.getModifyChangeSets(),
				includeAll.isMarkRan())));
		return result;
	}

	private static SortedSet<ChangeSet> getNestedSkippedChangeSets(ChangeLogIncludeAll includeAll) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		includeAll.getNestedChangeLogs().forEach(changelog -> result.addAll(changelog.getSkippedChangeSets()));
		return result;
	}
}