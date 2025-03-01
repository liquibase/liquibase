package liquibase.include;

import static liquibase.changelog.DatabaseChangeLog.CONTEXT;
import static liquibase.changelog.DatabaseChangeLog.CONTEXT_FILTER;
import static liquibase.changelog.DatabaseChangeLog.MODIFY_CHANGE_SETS;
import static liquibase.changelog.DatabaseChangeLog.PRE_CONDITIONS;

import java.io.IOException;
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
import liquibase.changelog.ModifyChangeSets;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.database.Database;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.SetupException;
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
public final class ChangeLogIncludeHelper {

	private static final ThreadLocal<DatabaseChangeLog> ROOT = new ThreadLocal<>();
	static final Comparator<ChangeSet> CHANGESET_COMPARATOR = Comparator.comparingInt(ChangeSet::getOrder);

	private ChangeLogIncludeHelper() {}

	public static void flatChangeLogChangeSets(DatabaseChangeLog changeLog) {
		SortedSet<ChangeSet> changeSetAccumulator = new TreeSet<>(CHANGESET_COMPARATOR);
		SortedSet<ChangeSet> skippedChangeSetAccumulator = new TreeSet<>(CHANGESET_COMPARATOR);
		changeLog.getIncludeList().forEach(i -> ChangeLogIncludeUtils.flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator));
		changeLog.getIncludeAllList().forEach(i -> ChangeLogIncludeAllUtils.flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator));
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

	static void propagateMarkRan(List<DatabaseChangeLog> changeLogs) {
		changeLogs.forEach(cl -> {
			cl.getIncludeList().forEach(i -> i.setPreconditions(getMarkRanPrecondition()));
			cl.getIncludeAllList().forEach(ia -> ia.setPreconditions(getMarkRanPrecondition()));
		});
	}

	static void flatChangeLogChangeSets(SortedSet<ChangeSet> changeSetAccumulator,
																																					SortedSet<ChangeSet> skippedChangeSetAccumulator,
																																					DatabaseChangeLog changeLog) {
		for(ChangeLogInclude i : changeLog.getIncludeList()) {
			ChangeLogIncludeUtils.flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator);
		}
		for(ChangeLogIncludeAll i : changeLog.getIncludeAllList()) {
			ChangeLogIncludeAllUtils.flatChangeLogChangeSets(i, changeSetAccumulator, skippedChangeSetAccumulator);
		}
	}

	static DatabaseChangeLog getChangeLog(String file, ResourceAccessor resourceAccessor, Boolean errorIfMissing,
																																							ModifyChangeSets modifyChangeSets, DatabaseChangeLog parentChangeLog,
																																							ContextExpression contextExpression, Labels labels, Boolean ignore,
																																							DatabaseChangeLog.OnUnknownFileFormat onUnknownFileFormat)
			throws SetupException {
		DatabaseChangeLog root = null;
		try {
			root = ROOT.get();
			if(root == null)
				ROOT.set(parentChangeLog);

			parentChangeLog.setRootChangeLog(root);
			if (".svn".equalsIgnoreCase(file) || "cvs".equalsIgnoreCase(file)) {
				return null;
			}
			try {
				if (!resourceAccessor.get(file).exists()) {
					if (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getCurrentValue()
							.equals(ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN)
							|| Boolean.FALSE.equals(errorIfMissing)) {
						Scope.getCurrentScope().getLog(ChangeLogIncludeHelper.class).warning(FileUtil.getFileNotFoundMessage(file));
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
				childChangeLog.setParentChangeLog(parentChangeLog);
				childChangeLog.setRootChangeLog(ROOT.get());
				return childChangeLog;
			}
			catch (UnknownChangelogFormatException e) {
				if (onUnknownFileFormat == DatabaseChangeLog.OnUnknownFileFormat.FAIL) {
					throw new SetupException(e);
				}
				boolean matchesFileExtension = StringUtils.trimToEmpty(file).matches("\\.\\w+$");
				if (matchesFileExtension || onUnknownFileFormat == DatabaseChangeLog.OnUnknownFileFormat.WARN) {
					Scope.getCurrentScope().getLog(ChangeLogIncludeHelper.class).warning(
							"included file " + file + "/" + file + " is not a recognized file type", e
					);
				}
				return null;
			}

			catch (Exception e) {
				throw new SetupException(e);
			}
		} finally {
				if (root == null)
					ROOT.remove();
		}
	}

	static SortedSet<ChangeSet> getNestedChangeSets(Database database, String logicalFilePath,
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

			if (changeSetLogicalFilePath == null
					&&!isRawSql && !ranChangeSetExists(changeSet, ranChangeSets)) {
				String finalLogicalFilePath = (logicalFilePath != null)
						? logicalFilePath : getActualLogicalFilePath(changeSet.getChangeLog());
				changeSet.setLogicalFilePath(finalLogicalFilePath);
				if (StringUtils.isNotEmpty(finalLogicalFilePath)) {
					changeSet.setFilePath(finalLogicalFilePath);
				}
			}
			if(markRan)
				changeSet.setPreconditions(getMarkRanPrecondition());

			result.add(changeSet);
		}
		return result;
	}

	/**
		* Search for the closest logicalfilePath for this changelog
		*/
	private static String getActualLogicalFilePath(DatabaseChangeLog changeLog) {
		DatabaseChangeLog currentChangeLog = changeLog;
		do {
			if (StringUtils.isNotBlank(currentChangeLog.getRawLogicalFilePath())) {
				return currentChangeLog.getRawLogicalFilePath();
			}
		} while ((currentChangeLog = currentChangeLog.getParentChangeLog()) != null);
		return null;
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
			public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) { }

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