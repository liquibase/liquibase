package liquibase.migrator.change;

import java.util.HashMap;
import java.util.Map;

public class ChangeFactory {

    private Map<String, Class> tagToClassMap;
    private static ChangeFactory instance;

    private ChangeFactory() {
        if (tagToClassMap == null) {
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
