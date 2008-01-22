package liquibase.preconditions;

import java.util.HashMap;
import java.util.Map;

public class PreconditionFactory {
    @SuppressWarnings("unchecked")
    private final Map<String, Class> tagToClassMap;

    private static final PreconditionFactory instance = new PreconditionFactory();

    @SuppressWarnings("unchecked")
    private PreconditionFactory() {
        tagToClassMap = new HashMap<String, Class>();
        Class[] preconditions = new Class[]{
                AndPrecondition.class,
                OrPrecondition.class,
                NotPrecondition.class,
                DBMSPrecondition.class,
                RunningAsPrecondition.class,
                SqlPrecondition.class,
        };

        try {
            for (Class<Precondition> preconditionClass : preconditions) {
                Precondition precondition = preconditionClass.newInstance();
                tagToClassMap.put(precondition.getTagName(), preconditionClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PreconditionFactory getInstance() {
        return instance;
    }

    /**
     * Create a new Precondition subclass based on the given tag name.
     */
    public Precondition create(String tagName) {
        Class<?> aClass = tagToClassMap.get(tagName);
        if (aClass == null) {
            throw new RuntimeException("Unknown tag: " + tagName);
        }
        try {
            return (Precondition) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
