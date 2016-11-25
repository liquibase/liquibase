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

    private ReflectionSerializer() {

    }

    public Set<String> getFields(Object object) {
        Set<String> returnSet = new HashSet<String>();
        Set<Field> allFields = new HashSet<Field>();

        Class classToExtractFieldsFrom = object.getClass();
        while (!classToExtractFieldsFrom.equals(Object.class)) {
            allFields.addAll(Arrays.asList(classToExtractFieldsFrom.getDeclaredFields()));
            classToExtractFieldsFrom = classToExtractFieldsFrom.getSuperclass();
        }

        for (Field field : allFields) {
            if (field.getName().equals("serialVersionUID") || field.getName().equals("serializableFields")) {
                continue;
            }
            if (field.isSynthetic() || field.getName().equals("$VRc")) { //from emma
                continue;
            }

            returnSet.add(field.getName());
        }

        return returnSet;
    }

    private Field findField(Object object, String field) {
        Field foundField = null;
        Class<? extends Object> classToCheck = object.getClass();
        while (foundField == null && !classToCheck.equals(Object.class)) {
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
