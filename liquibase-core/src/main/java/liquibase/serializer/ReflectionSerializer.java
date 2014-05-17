package liquibase.serializer;

import liquibase.change.DatabaseChangeProperty;
import liquibase.exception.UnexpectedLiquibaseException;

import java.lang.reflect.Field;
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

    public Object getValue(Object object, String field) {
        try {
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
                throw new UnexpectedLiquibaseException("No field "+field+" on "+object.getClass());
            }
            foundField.setAccessible(true);

            return foundField.get(object);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
