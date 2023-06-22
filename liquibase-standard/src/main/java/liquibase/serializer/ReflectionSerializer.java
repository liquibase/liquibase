package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionSerializer {

    private static final ReflectionSerializer instance = new ReflectionSerializer();

    public static ReflectionSerializer getInstance() {
        return instance;
    }

    private final Map<Class<?>, Map<String, Field>> reflectionCache = new ConcurrentHashMap<>();

    private ReflectionSerializer() {

    }

    private Map<String, Field> getFieldMap(Object object) {
        return reflectionCache.computeIfAbsent(object.getClass(), ReflectionSerializer::gatherFields);
    }

    private static Map<String, Field> gatherFields(Class<?> classToExtractFieldsFrom) {
        Map<String, Field> fields = new HashMap<>();
        Set<Field> allFields = new HashSet<>();

        while (!classToExtractFieldsFrom.equals(Object.class)) {
            allFields.addAll(Arrays.asList(classToExtractFieldsFrom.getDeclaredFields()));
            classToExtractFieldsFrom = classToExtractFieldsFrom.getSuperclass();
        }

        for (Field field : allFields) {
            if ("serialVersionUID".equals(field.getName()) || "serializableFields".equals(field.getName())) {
                continue;
            }
            if (field.isSynthetic() || "$VRc".equals(field.getName())) { //from emma
                continue;
            }

            fields.put(field.getName(), field);
            field.setAccessible(true);
        }
        return Collections.unmodifiableMap(fields);
    }

    public Set<String> getFields(Object object) {
        return getFieldMap(object).keySet();
    }

    private Field findField(Object object, String field) {
        Field foundField = getFieldMap(object).get(field);
        if (foundField == null) {
            throw new UnexpectedLiquibaseException("No field " + field + " on " + object.getClass());
        }
        return foundField;
    }

    public Object getValue(Object object, String field) {
        try {
            Field foundField = findField(object, field);
            foundField.setAccessible(true);

            return foundField.get(object);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void setValue(Object object, String field, Object value) {
        try {
            Field foundField = findField(object, field);
            foundField.setAccessible(true);

            foundField.set(object, value);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Class<?> getDataTypeClass(Object object, String field) {
        try {
            Field foundField = findField(object, field);
            Type dataType = foundField.getGenericType();
            if (dataType instanceof Class) {
                return (Class<?>) dataType;
            }
            if (dataType instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) dataType).getRawType();
            }
            return Object.class;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public Type[] getDataTypeClassParameters(Object object, String field) {
        try {
            Field foundField = findField(object, field);
            Type dataType = foundField.getGenericType();
            if (dataType instanceof ParameterizedType) {
                return ((ParameterizedType) dataType).getActualTypeArguments();
            }
            return new Type[0];
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
