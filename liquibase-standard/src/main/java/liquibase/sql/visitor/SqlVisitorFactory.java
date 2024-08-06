package liquibase.sql.visitor;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class SqlVisitorFactory {

    @SuppressWarnings("unchecked")
	private final Map<String, Class> tagToClassMap;

    @Getter
    private static final SqlVisitorFactory instance = new SqlVisitorFactory();

    @SuppressWarnings("unchecked")
	private SqlVisitorFactory() {
        tagToClassMap = new HashMap<>();
        Class[] visitors = new Class[]{
                PrependSqlVisitor.class,
                AppendSqlVisitor.class,
                RegExpReplaceSqlVisitor.class,
                ReplaceSqlVisitor.class,
        };

        try {
            for (Class<SqlVisitor> visitorClass : visitors) {
                SqlVisitor visitor = visitorClass.getConstructor().newInstance();
                tagToClassMap.put(visitor.getName(), visitorClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new Change subclass based on the given tag name.
     */
    public SqlVisitor create(String tagName) {
        Class<?> aClass = tagToClassMap.get(tagName);
        if (aClass == null) {
            return null;
        }
        try {
            return (SqlVisitor) aClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
