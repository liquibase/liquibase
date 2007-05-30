package liquibase.migrator.change;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for constructing the correct liquibase.migrator.change.AbstractChange implementation based on the tag name.
 * It is currently implemented by a static array of AbstractChange implementations, although that may change in
 * later revisions.
 *
 * @see liquibase.migrator.change.AbstractChange
 */
public class ChangeFactory {

    private Map<String, Class> tagToClassMap;
    private static ChangeFactory instance;

    private ChangeFactory() {
        tagToClassMap = new HashMap<String, Class>();
        Class[] refactorings = new Class[]{
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
        };

        try {
            for (Class refactoringClass : refactorings) {
                AbstractChange change = (AbstractChange) refactoringClass.newInstance();
                tagToClassMap.put(change.getTagName(), refactoringClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ChangeFactory getInstance() {
        if (instance == null) {
            instance = new ChangeFactory();
        }
        return instance;
    }

    public AbstractChange create(String tagName) {
        Class aClass = tagToClassMap.get(tagName);
        if (aClass == null) {
            throw new RuntimeException("Unknown tag: " + tagName);
        }
        try {
            return (AbstractChange) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
