package liquibase.include;

import static liquibase.changelog.DatabaseChangeLog.ERROR_IF_MISSING;
import static liquibase.changelog.DatabaseChangeLog.FILE;
import static liquibase.changelog.DatabaseChangeLog.IGNORE;
import static liquibase.changelog.DatabaseChangeLog.INCLUDE_CHANGELOG;
import static liquibase.changelog.DatabaseChangeLog.LABELS;
import static liquibase.changelog.DatabaseChangeLog.LOGICAL_FILE_PATH;
import static liquibase.changelog.DatabaseChangeLog.RELATIVE_TO_CHANGELOG_FILE;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.DatabaseChangeLog;
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
import lombok.Setter;

/**
 * A final class representing the <a href="https://docs.liquibase.com/change-types/include.html">include</a> tag.
 *
 * @author <a href="https://github.com/cagliostro92">Edoardo Patti</a>
 */
@Getter(AccessLevel.PACKAGE)
public final class ChangeLogInclude extends AbstractLiquibaseSerializable implements Conditional, ChangeLogChild {

    private final Database database = Scope.getCurrentScope().getDatabase();

    private final String file;
    private final Boolean relativeToChangelogFile;
    private final Boolean errorIfMissing;
    private final Boolean ignore;
    private final ContextExpression context;
    private final Labels labels;
    private final String logicalFilePath;
    private DatabaseChangeLog nestedChangelog;
    private final ResourceAccessor resourceAccessor;
    private final DatabaseChangeLog parentChangeLog;
    private final ModifyChangeSets modifyChangeSets;
    private boolean markRan = false;
    private PreconditionContainer preconditions;

  public ChangeLogInclude(ParsedNode node, ResourceAccessor resourceAccessor,
                            DatabaseChangeLog parentChangeLog, ModifyChangeSets modifyChangeSets)
        throws ParsedNodeException, SetupException {

        this.resourceAccessor = resourceAccessor;
        this.parentChangeLog = parentChangeLog;
        this.modifyChangeSets = modifyChangeSets;
        this.ignore = node.getChildValue(null, IGNORE, Boolean.class);
        this.labels = new Labels(node.getChildValue(null, LABELS, String.class));
        this.logicalFilePath = node.getChildValue(null, LOGICAL_FILE_PATH, String.class);
        this.relativeToChangelogFile = node.getChildValue(null, RELATIVE_TO_CHANGELOG_FILE, false);
        this.errorIfMissing = node.getChildValue(null, ERROR_IF_MISSING, true);
        this.file = node.getChildValue(null, FILE, String.class);
        this.context = ChangeLogIncludeHelper.getContextExpression(node);
        this.preconditions = ChangeLogIncludeHelper.getPreconditions(node, resourceAccessor);
        this.nestedChangelog = ChangeLogIncludeUtils.getChangeLog(this);
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
    public String getSerializedObjectNamespace() {
      return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile",
            "errorIfMissing", "context"));
    }

    @Override
    public String getSerializedObjectName() {
      return INCLUDE_CHANGELOG;
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
                        this.nestedChangelog = null;
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
                        this.nestedChangelog = null;
                    }
                }
            }
        }
    }
}