package liquibase.include;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.DatabaseChangeLog;
import static liquibase.changelog.DatabaseChangeLog.ERROR_IF_MISSING;
import static liquibase.changelog.DatabaseChangeLog.FILE;
import static liquibase.changelog.DatabaseChangeLog.IGNORE;
import static liquibase.changelog.DatabaseChangeLog.INCLUDE_CHANGELOG;
import static liquibase.changelog.DatabaseChangeLog.LABELS;
import static liquibase.changelog.DatabaseChangeLog.LOGICAL_FILE_PATH;
import static liquibase.changelog.DatabaseChangeLog.RELATIVE_TO_CHANGELOG_FILE;
import liquibase.changelog.ModifyChangeSets;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {

    private static final String CLASSPATH_PROTOCOL = "classpath:";
    private static final List<String> CHANGELOG_EXTENSION = Arrays.asList(".xml", ".yml", ".yaml", ".json");
    private final Database database = Scope.getCurrentScope().getDatabase();

    private String file;
    private Boolean relativeToChangelogFile;
    private Boolean errorIfMissing;
    private Boolean ignore;
    private ContextExpression context;
    private PreconditionContainer preconditions;
    private Labels labels;
    private String logicalFilePath;
    private DatabaseChangeLog childChangelog;
    private ResourceAccessor resourceAccessor;
    private DatabaseChangeLog parentChangeLog;
    private ModifyChangeSets modifyChangeSets;

    public ChangeLogInclude(ParsedNode node, ResourceAccessor resourceAccessor,
                            DatabaseChangeLog parentChangeLog, ModifyChangeSets modifyChangeSets)
				throws ParsedNodeException, SetupException {

        this.resourceAccessor = resourceAccessor;
        this.parentChangeLog = parentChangeLog;
        this.modifyChangeSets = modifyChangeSets;
        this.ignore = node.getChildValue(null, IGNORE, Boolean.class);
        this.labels = new Labels(node.getChildValue(null, LABELS, String.class));
        this.logicalFilePath = node.getChildValue(null, LOGICAL_FILE_PATH, String.class);
        Boolean nodeRelativeToChangelogFile = node.getChildValue(null, RELATIVE_TO_CHANGELOG_FILE, Boolean.class);
        this.relativeToChangelogFile = (nodeRelativeToChangelogFile != null) ? nodeRelativeToChangelogFile : false;
        Boolean nodeErrorIfMissing = node.getChildValue(null, ERROR_IF_MISSING, Boolean.class);
        this.errorIfMissing = (nodeErrorIfMissing != null) ? nodeErrorIfMissing : true;
        this.file = node.getChildValue(null, FILE, String.class);
        this.context = ChangeLogIncludeUtils.getContextExpression(node);
        this.preconditions = ChangeLogIncludeUtils.getPreconditions(node, resourceAccessor);
        this.childChangelog = ChangeLogIncludeUtils.getChangeLog(this);
    }

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile", "errorIfMissing", "context"));
    }

    @Override
    public String getSerializedObjectName() {
        return INCLUDE_CHANGELOG;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}