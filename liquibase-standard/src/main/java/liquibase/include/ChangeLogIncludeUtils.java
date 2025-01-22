package liquibase.include;

import static liquibase.changelog.DatabaseChangeLog.normalizePath;
import static liquibase.include.ChangeLogIncludeHelper.CHANGESET_COMPARATOR;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ResourceAccessor;

/**
	* @author <a href="https://github.com/cagliostro92">Edoardo Patti</a>
	*/
final class ChangeLogIncludeUtils {

	private static final String CLASSPATH_PROTOCOL = "classpath:";

	private ChangeLogIncludeUtils() { }

	static DatabaseChangeLog getChangeLog(ChangeLogInclude include) throws SetupException {
		String normalizedFilePath = normalizeFilePath(include.getFile(), include.getParentChangeLog().getPhysicalFilePath(), include.getRelativeToChangelogFile(), include.getResourceAccessor());
		return ChangeLogIncludeHelper.getChangeLog(normalizedFilePath, include.getResourceAccessor(), include.getErrorIfMissing(),
				include.getModifyChangeSets(), include.getParentChangeLog(), include.getContext(), include.getLabels(), include.getIgnore(), DatabaseChangeLog.OnUnknownFileFormat.FAIL);
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

	static void flatChangeLogChangeSets(ChangeLogInclude include,
																																					SortedSet<ChangeSet> changeSetAccumulator,
																																					SortedSet<ChangeSet> skippedChangeSetAccumulator) {

		include.checkPreconditions();
		DatabaseChangeLog changeLog = include.getNestedChangelog();
		if(include.isMarkRan())
			ChangeLogIncludeHelper.propagateMarkRan(Collections.singletonList(changeLog));

		if(changeLog != null) {
			changeSetAccumulator.addAll(getNestedChangeSets(include));
			skippedChangeSetAccumulator.addAll(getNestedSkippedChangeSets(include));
			ChangeLogIncludeHelper.flatChangeLogChangeSets(changeSetAccumulator, skippedChangeSetAccumulator, changeLog);
		}
	}

	static SortedSet<ChangeSet> getNestedSkippedChangeSets(ChangeLogInclude include) {
		SortedSet<ChangeSet> result = new TreeSet<>(CHANGESET_COMPARATOR);
		result.addAll(include.getNestedChangelog().getSkippedChangeSets());
		return result;
	}

	private static SortedSet<ChangeSet> getNestedChangeSets(ChangeLogInclude include) {
		return ChangeLogIncludeHelper.getNestedChangeSets(include.getDatabase(), include.getLogicalFilePath(),
				include.getNestedChangelog(), include.getModifyChangeSets(), include.isMarkRan());
	}
}