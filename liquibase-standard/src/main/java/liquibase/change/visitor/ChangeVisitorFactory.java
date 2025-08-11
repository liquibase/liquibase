package liquibase.change.visitor;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
/**
 * Factory class for constructing the liquibase.change.ChangeVisitor implementation based on a change name.
 *
 * @see liquibase.change.visitor.ChangeVisitor
 */
public class ChangeVisitorFactory {
    @SuppressWarnings("unchecked")
    private final Map<String, Class> tagToClassMap;

    @Getter
    private static final ChangeVisitorFactory instance = new ChangeVisitorFactory();

    @SuppressWarnings("unchecked")
    private ChangeVisitorFactory() {
        tagToClassMap = new HashMap<>();
        Class[] visitors = new Class[]{
                AddColumnChangeVisitor.class,
        };

        try {
            for (Class<ChangeVisitor> visitorClass : visitors) {
                ChangeVisitor visitor = visitorClass.getConstructor().newInstance();
                tagToClassMap.put(visitor.getName(), visitorClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new ChangeVisitor subclass based on the given tag name.
     */
    public ChangeVisitor create(String tagName) {
        Class<?> aClass = tagToClassMap.get(tagName);
        if (aClass == null) {
            return null;
        }
        try {
            return (ChangeVisitor) aClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
