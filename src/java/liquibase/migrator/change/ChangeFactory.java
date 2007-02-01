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
        try {
            return (AbstractChange) tagToClassMap.get(tagName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
