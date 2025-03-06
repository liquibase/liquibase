package liquibase.include;

import static liquibase.changelog.DatabaseChangeLog.ENDS_WITH_FILTER;
import static liquibase.changelog.DatabaseChangeLog.ERROR_IF_MISSING_OR_EMPTY;
import static liquibase.changelog.DatabaseChangeLog.IGNORE;
import static liquibase.changelog.DatabaseChangeLog.INCLUDE_ALL_CHANGELOGS;
import static liquibase.changelog.DatabaseChangeLog.LABELS;
import static liquibase.changelog.DatabaseChangeLog.LOGICAL_FILE_PATH;
import static liquibase.changelog.DatabaseChangeLog.MAX_DEPTH;
import static liquibase.changelog.DatabaseChangeLog.MIN_DEPTH;
import static liquibase.changelog.DatabaseChangeLog.PATH;
import static liquibase.changelog.DatabaseChangeLog.RELATIVE_TO_CHANGELOG_FILE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.IncludeAllFilter;
import liquibase.changelog.ModifyChangeSets;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * A final class representing the <a href="https://docs.liquibase.com/change-types/includeall.html">includeAll</a> tag.
 *
 * @author <a href="https://github.com/cagliostro92">Edoardo Patti</a>
 */
@Getter(AccessLevel.PACKAGE)
public final class ChangeLogIncludeAll extends AbstractLiquibaseSerializable implements Conditional, ChangeLogChild {

    private final String path;
    private final Boolean errorIfMissingOrEmpty;
    private final Boolean relativeToChangelogFile;
    private final IncludeAllFilter resourceFilter;
    private final ContextExpression context;
    private final Integer minDepth;
    private final Integer maxDepth;
    private final String endsWithFilter;
    private final String logicalFilePath;
    private final Labels labels;
    private final Boolean ignore;
    private final ResourceAccessor resourceAccessor;
    private final DatabaseChangeLog parentChangeLog;
    private final ModifyChangeSets modifyChangeSets;
    private final Database database = Scope.getCurrentScope().getDatabase();
    private final List<DatabaseChangeLog> nestedChangeLogs = new ArrayList<>(10);
    private boolean markRan = false;
    private PreconditionContainer preconditions;

  public ChangeLogIncludeAll(ParsedNode node, ResourceAccessor resourceAccessor,
                               DatabaseChangeLog parentChangeLog, ModifyChangeSets modifyChangeSets)
        throws ParsedNodeException, SetupException {

        this.resourceAccessor = resourceAccessor;
        this.parentChangeLog = parentChangeLog;
        this.modifyChangeSets = modifyChangeSets;
        this.ignore = node.getChildValue(null, IGNORE, false);
        this.labels = new Labels(node.getChildValue(null, LABELS, String.class));
        this.logicalFilePath = node.getChildValue(null, LOGICAL_FILE_PATH, String.class);
        this.relativeToChangelogFile = node.getChildValue(null, RELATIVE_TO_CHANGELOG_FILE, false);
        this.errorIfMissingOrEmpty = node.getChildValue(null, ERROR_IF_MISSING_OR_EMPTY, false);
        this.minDepth = node.getChildValue(null, MIN_DEPTH, 0);
        this.maxDepth = node.getChildValue(null, MAX_DEPTH, Integer.MAX_VALUE);
        this.endsWithFilter = node.getChildValue(null, ENDS_WITH_FILTER, "");
        this.context = ChangeLogIncludeHelper.getContextExpression(node);
        this.resourceFilter = ChangeLogIncludeAllUtils.getFilterDef(node);
        this.path = node.getChildValue(null, PATH, String.class);
        this.preconditions = ChangeLogIncludeHelper.getPreconditions(node, resourceAccessor);
        this.nestedChangeLogs.addAll(ChangeLogIncludeAllUtils.getNestedChangeLogs(node, this));
    }

    @Override
    public PreconditionContainer getPreconditions() {
        return preconditions;
    }

    @Override
    public void setPreconditions(PreconditionContainer preconditions) {
        this.preconditions = preconditions;
    }

    @Override
    public String getSerializedObjectName() {
      return INCLUDE_ALL_CHANGELOGS;
    }

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays
            .asList("path", "errorIfMissingOrEmpty", "relativeToChangelogFile", "resourceFilter",
                "context", "minDepth", "maxDepth", "endsWithFilter","logicalFilePath"));
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
                        ChangeLogIncludeHelper.sendIncludePreconditionWarningMessage(warningMessage, e);
                    } else if (PreconditionContainer.FailOption.MARK_RAN.equals(preconditionContainer.getOnFail())) {
                        this.markRan = true;
                    } else {
                        this.nestedChangeLogs.clear();
                    }
                } catch (PreconditionErrorException e) {
                    if (PreconditionContainer.ErrorOption.HALT.equals(preconditionContainer.getOnError())) {
                        throw new RuntimeException(e);
                    } else if (PreconditionContainer.ErrorOption.WARN.equals(preconditionContainer.getOnError())) {
                        ChangeLogIncludeHelper.sendIncludePreconditionWarningMessage(warningMessage, e);
                    } else if (PreconditionContainer.ErrorOption.MARK_RAN.equals(preconditionContainer.getOnError())) {
                        this.markRan = true;
                    }
                    else {
                        this.nestedChangeLogs.clear();
                    }
                }
            }
        }
    }
}