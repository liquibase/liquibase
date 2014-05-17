package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ObjectUtil {

    private static Map<Class<?>, Method[]> methodCache = new HashMap<Class<?>, Method[]>();

    public static Object getProperty(Object object, String propertyName) throws IllegalAccessException, InvocationTargetException {
        Method readMethod = getReadMethod(object, propertyName);
        if (readMethod == null) {
            throw new UnexpectedLiquibaseException("Property '" + propertyName + "' not found on object type " + object.getClass().getName());
        }

        return readMethod.invoke(object);
    }

    public static boolean hasProperty(Object object, String propertyName) {
        return hasReadProperty(object, propertyName) && hasWriteProperty(object, propertyName);
    }

    public static boolean hasReadProperty(Object object, String propertyName) {
        return getReadMethod(object, propertyName) != null;
    }

    public static boolean hasWriteProperty(Object object, String propertyName) {
        return getWriteMethod(object, propertyName) != null;
    }

    public static void setProperty(Object object, String propertyName, String propertyValue) {
        Method method = getWriteMethod(object, propertyName);
        if (method == null) {
            throw new UnexpectedLiquibaseException("Property '" + propertyName + "' not found on object type " + object.getClass().getName());
        }

        Class<?> parameterType = method.getParameterTypes()[0];
        Object finalValue = propertyValue;
        if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
            finalValue = Boolean.valueOf(propertyValue);
        } else if (parameterType.equals(Integer.class)) {
            finalValue = Integer.valueOf(propertyValue);
        } else if (parameterType.equals(Long.class)) {
            finalValue = Long.valueOf(propertyValue);
        } else if (parameterType.equals(BigInteger.class)) {
            finalValue = new BigInteger(propertyValue);
        } else if (parameterType.equals(DatabaseFunction.class)) {
            finalValue = new DatabaseFunction(propertyValue);
        } else if (parameterType.equals(SequenceNextValueFunction.class)) {
            finalValue = new SequenceNextValueFunction(propertyValue);
        } else if (parameterType.equals(SequenceCurrentValueFunction.class)) {
            finalValue = new SequenceCurrentValueFunction(propertyValue);
        } else if (Enum.class.isAssignableFrom(parameterType)) {
            finalValue = Enum.valueOf((Class<Enum>) parameterType, propertyValue);
        }
        try {
            method.invoke(object, finalValue);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (IllegalArgumentException e) {
            throw new UnexpectedLiquibaseException("Cannot call "+method.toString()+" with value of type "+finalValue.getClass().getName());
        } catch (InvocationTargetException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private static Method getReadMethod(Object object, String propertyName) {
        String getMethodName = "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        String isMethodName = "is" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);

        Method[] methods = getMethods(object);

        for (Method method : methods) {
            if ((method.getName().equals(getMethodName) || method.getName().equals(isMethodName)) && method.getParameterTypes().length == 0) {
                return method;
            }
        }
        return null;
    }

    private static Method getWriteMethod(Object object, String propertyName) {
        String methodName = "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method[] methods = getMethods(object);

        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return null;
    }

    private static Method[] getMethods(Object object) {
        Method[] methods = methodCache.get(object.getClass());

        if (methods == null) {
            methods = object.getClass().getMethods();
            methodCache.put(object.getClass(), methods);
        }
        return methods;
    }

}
