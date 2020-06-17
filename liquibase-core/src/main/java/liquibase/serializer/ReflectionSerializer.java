package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ReflectionSerializer {

    private static ReflectionSerializer instance = new ReflectionSerializer();

    public static ReflectionSerializer getInstance() {
        return instance;
    }

    private Map<Class, Map<String, Field>> reflectionCache = new HashMap<>();

    private ReflectionSerializer() {

    }

    public Set<String> getFields(Object object) {

        if (!reflectionCache.containsKey(object.getClass())) {

            Map<String, Field> fields = new HashMap<>();
            Set<Field> allFields = new HashSet<>();

            Class classToExtractFieldsFrom = object.getClass();
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

            reflectionCache.put(object.getClass(), fields);
        }

        return reflectionCache.get(object.getClass()).keySet();
    }

    private Field findField(Object object, String field) {
        Field foundField = null;
        Class<? extends Object> classToCheck = object.getClass();
        while ((foundField == null) && !classToCheck.equals(Object.class)) {
            try {
                foundField = classToCheck.getDeclaredField(field);
            } catch (NoSuchFieldException e) {
                classToCheck = classToCheck.getSuperclass();
            }
        }
        if (foundField == null) {
            throw new UnexpectedLiquibaseException("No field " + field + " on " + object.getClass());
        }
        return foundField;
    }

    public Object getValue(Object object, String field) {
        if (!reflectionCache.containsKey(object.getClass())) {
            getFields(object); //fills cache
        }

        Map<String, Field> fieldsByName = reflectionCache.get(object.getClass());
        Field foundField = fieldsByName.get(field);

        try {
            if (foundField == null) {
                foundField = findField(object, field);
                foundField.setAccessible(true);

                fieldsByName.put(field, foundField);
            }

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

    public Class getDataTypeClass(Object object, String field) {
        try {
            Field foundField = findField(object, field);
            Type dataType = foundField.getGenericType();
            if (dataType instanceof Class) {
                return (Class) dataType;
            }
            if (dataType instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) dataType).getRawType();
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
