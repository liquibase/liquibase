package liquibase.include;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
import static liquibase.changelog.DatabaseChangeLog.MODIFY_CHANGE_SETS;
import static liquibase.changelog.DatabaseChangeLog.PRE_CONDITIONS;
import static liquibase.changelog.DatabaseChangeLog.normalizePath;
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
import liquibase.resource.ResourceAccessor;
import liquibase.ui.UIService;
import liquibase.util.FileUtil;
import org.apache.commons.lang3.StringUtils;

public final class ChangeLogIncludeUtils {

 private static final String CLASSPATH_PROTOCOL = "classpath:";

 private ChangeLogIncludeUtils() {}

 public static void flatChangeLogChangeSets(DatabaseChangeLog changeLog) {
	SortedSet<ChangeSet> changeSetAccumulator = new TreeSet<>();
	SortedSet<ChangeSet> skippedChangeSetAccumulator = new TreeSet<>();
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

 static String normalizeFilePath(String file, String parentChangelogPhysicalFilePath, boolean isRelative, ResourceAccessor resourceAccessor) {
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

 static ContextExpression getContextExpression(ParsedNode node) throws ParsedNodeException {
	ContextExpression includeNodeContextFilter = new ContextExpression(node.getChildValue(null, CONTEXT_FILTER, String.class));
	if (includeNodeContextFilter.isEmpty()) {
	 includeNodeContextFilter = new ContextExpression(node.getChildValue(null, CONTEXT, String.class));
	}
	return includeNodeContextFilter;
 }

 static DatabaseChangeLog getChangeLog(ChangeLogIncludeAll include, String file) throws SetupException {
	return getChangeLog(file, include.getResourceAccessor(), include.getErrorIfMissingOrEmpty(),
			include.getModifyChangeSets(), include.getParentChangeLog(), include.getContext(), include.getLabels(), include.getIgnore(), DatabaseChangeLog.OnUnknownFileFormat.WARN);
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
	 cl.getIncludeList().forEach(i -> i.setPreconditions(getMarkRanPreconditions()));
	 cl.getIncludeAllList().forEach(ia -> ia.setPreconditions(getMarkRanPreconditions()));
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
	SortedSet<ChangeSet> result = new TreeSet<>();
	includeAll.getNestedChangeLogs().forEach(changelog -> result.addAll(getNestedChangeSets(includeAll.getDatabase(), includeAll.getLogicalFilePath(),
			changelog, includeAll.getModifyChangeSets(),
			includeAll.isMarkRan())));
	return result;
 }

 private static SortedSet<ChangeSet> getNestedSkippedChangeSets(ChangeLogInclude include) {
	return new TreeSet<>(include.getNestedChangelog().getSkippedChangeSets());
 }

 private static SortedSet<ChangeSet> getNestedSkippedChangeSets(ChangeLogIncludeAll includeAll) {
	SortedSet<ChangeSet> result = new TreeSet<>();
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
	SortedSet<ChangeSet> result = new TreeSet<>();
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
		changeSet.setPreconditions(getMarkRanPreconditions());

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

 private static PreconditionContainer getMarkRanPreconditions() {
	PreconditionContainer result = new PreconditionContainer();
	result.setOnFail(PreconditionContainer.FailOption.MARK_RAN);
	result.addNestedPrecondition(getFailedPrecondition());
	return result;
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
			 throws PreconditionFailedException, PreconditionErrorException {

		throw new PreconditionFailedException(new ArrayList<>());
	 }

	 @Override
	 public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor)
			 throws ParsedNodeException {

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
	 public ParsedNode serialize() throws ParsedNodeException {
		return null;
	 }
	};
 }

}