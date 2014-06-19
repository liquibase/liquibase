package liquibase.action.visitor;

import liquibase.action.visitor.core.AppendSqlVisitor;
import liquibase.action.visitor.core.PrependSqlVisitor;
import liquibase.action.visitor.core.RegExpReplaceSqlVisitor;
import liquibase.action.visitor.core.ReplaceSqlVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton for constructing {@link liquibase.action.visitor.ActionVisitor} instances based on their name.
 */
public class ActionVisitorFactory {

    @SuppressWarnings("unchecked")
	private final Map<String, Class> tagToClassMap;

    private static final ActionVisitorFactory instance = new ActionVisitorFactory();

    @SuppressWarnings("unchecked")
	private ActionVisitorFactory() {
        tagToClassMap = new HashMap<String, Class>();
        Class[] visitors = new Class[]{
                PrependSqlVisitor.class,
                AppendSqlVisitor.class,
                RegExpReplaceSqlVisitor.class,
                ReplaceSqlVisitor.class,
        };

        try {
            for (Class<ActionVisitor> visitorClass : visitors) {
                ActionVisitor visitor = visitorClass.newInstance();
                tagToClassMap.put(visitor.getName(), visitorClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ActionVisitorFactory getInstance() {
        return instance;
    }

    /**
     * Create a new ActionVisitor subclass based on the given name.
     */
    public ActionVisitor create(String tagName) {
        Class<?> aClass = tagToClassMap.get(tagName);
        if (aClass == null) {
            return null;
        }
        try {
            return (ActionVisitor) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
