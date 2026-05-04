package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an &lt;include&gt; element in a DatabaseChangeLog.
 * <p>
 * The include element allows you to break up your change logs into more manageable pieces.
 * Use the include tag in the root changelog to reference other changelog files.
 * </p>
 * <p>
 * The {@code logicalFilePath} attribute can be used to override the file path used to identify
 * changesets in the DATABASECHANGELOG table. This is useful when you need to move or rename
 * changelog files without having Liquibase re-execute the changesets.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {

    /**
     * Path to the changelog file to include.
     * Can be an absolute path or relative to the parent changelog file (if relativeToChangelogFile is true).
     */
    private String file;

    /**
     * Whether the file path should be interpreted as relative to the parent changelog file.
     * If false, the path is interpreted as relative to the classpath root.
     */
    private Boolean relativeToChangelogFile;

    /**
     * Whether to throw an error if the included file cannot be found.
     * Default is true.
     */
    private Boolean errorIfMissing;

    /**
     * Context expression to determine when this include should be processed.
     * Only includes matching the runtime context will be processed.
     */
    private ContextExpression context;

    /**
     * Logical file path to use in the DATABASECHANGELOG table instead of the actual file path.
     * This allows you to rename or move changelog files without causing changesets to be re-executed.
     * <p>
     * When specified, all changesets from the included file will be tracked using this logical path
     * rather than the physical file path.
     * </p>
     *
     * @since 4.30.0
     */
    private String logicalFilePath;

    /**
     * Returns the set of fields that should be serialized.
     *
     * @return A LinkedHashSet containing the names of all serializable fields
     */
    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile", "errorIfMissing", "context", "logicalFilePath"));
    }

    /**
     * Returns the name of this object when serialized.
     *
     * @return "include"
     */
    @Override
    public String getSerializedObjectName() {
        return "include";
    }

    /**
     * Returns the XML namespace for this object when serialized.
     *
     * @return The standard Liquibase changelog namespace
     */
    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
