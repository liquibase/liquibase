package liquibase.change;

import liquibase.change.custom.CustomChangeWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for constructing the correct liquibase.change.Change implementation based on the tag name.
 * It is currently implemented by a static array of Change implementations, although that may change in
 * later revisions.  The best way to get an instance of ChangeFactory is off the Liquibase.getChangeFactory() method.
 *
 * @see liquibase.change.Change
 */
public class ChangeFactory {

    @SuppressWarnings("unchecked")
	private final Map<String, Class> tagToClassMap;

    private static final ChangeFactory instance = new ChangeFactory();

    @SuppressWarnings("unchecked")
	private ChangeFactory() {
        tagToClassMap = new HashMap<String, Class>();
        Class[] changes = new Class[]{
                AddColumnChange.class,
                AlterSequenceChange.class,
                CreateIndexChange.class,
                CreateSequenceChange.class,
                CreateTableChange.class,
                DropColumnChange.class,
                DropIndexChange.class,
                DropSequenceChange.class,
                DropTableChange.class,
                InsertDataChange.class,
                ModifyColumnChange.class,
                RawSQLChange.class,
                RenameColumnChange.class,
                RenameTableChange.class,
                AddNotNullConstraintChange.class,
                DropNotNullConstraintChange.class,
                CreateViewChange.class,
                DropViewChange.class,
                MergeColumnChange.class,
                RenameViewChange.class,
                AddForeignKeyConstraintChange.class,
                DropForeignKeyConstraintChange.class,
                AddLookupTableChange.class,
                AddPrimaryKeyChange.class,
                DropPrimaryKeyChange.class,
                AddAutoIncrementChange.class,
                AddDefaultValueChange.class,
                DropDefaultValueChange.class,
                AddUniqueConstraintChange.class,
                DropUniqueConstraintChange.class,
                SQLFileChange.class,
                CustomChangeWrapper.class,
                CreateProcedureChange.class,
                ExecuteShellCommandChange.class,
                UpdateDataChange.class,
                DeleteDataChange.class,
                TagDatabaseChange.class,
                LoadDataChange.class,
        };

        try {
            for (Class<Change> changeClass : changes) {
                Change change = changeClass.newInstance();
                tagToClassMap.put(change.getTagName(), changeClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ChangeFactory getInstance() {
        return instance;
    }

    /**
     * Create a new Change subclass based on the given tag name.
     */
    public Change create(String tagName) {
        Class<?> aClass = tagToClassMap.get(tagName);
        if (aClass == null) {
            throw new RuntimeException("Unknown tag: " + tagName);
        }
        try {
            return (Change) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
