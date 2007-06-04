package liquibase.migrator.parser;

import liquibase.migrator.change.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for constructing the correct liquibase.migrator.change.AbstractChange implementation based on the tag name.
 * It is currently implemented by a static array of AbstractChange implementations, although that may change in
 * later revisions.  The best way to get an instance of ChangeFactory is off the Migrator.getChangeFactory() method.
 *
 * @see liquibase.migrator.change.AbstractChange
 * @see liquibase.migrator.Migrator#getChangeFactory()
 */
public class ChangeFactory {

    private final Map<String, Class> tagToClassMap;

    public ChangeFactory() {
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

    /**
     * Create a new AbstractChange subclass based on the given tag name.
     */
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
