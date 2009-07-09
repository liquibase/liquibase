package liquibase.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;

public class ObjectUtil {

    public static Object getProperty(Object object, String propertyName) throws IllegalAccessException, InvocationTargetException {
        String methodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
                return method.invoke(object);
            }
        }
        throw new RuntimeException("Property not found: " + propertyName);
    }

    public static void setProperty(Object object, String propertyName, String propertyValue) throws IllegalAccessException, InvocationTargetException {
        String methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?> parameterType = method.getParameterTypes()[0];
                if (method.getParameterTypes().length == 1) {
                    if (parameterType.equals(Boolean.class)) {
                        method.invoke(object, Boolean.valueOf(propertyValue));
                        return;
                    } else if (parameterType.equals(String.class)) {
                        method.invoke(object, propertyValue);
                        return;
                    } else if (parameterType.equals(String.class)) {
                        method.invoke(object, propertyValue);
                        return;
                    } else if (parameterType.equals(Integer.class)) {
                        method.invoke(object, Integer.valueOf(propertyValue));
                        return;
                    } else if (parameterType.equals(Long.class)) {
                        method.invoke(object, Long.valueOf(propertyValue));
                        return;
                    } else if (parameterType.equals(BigInteger.class)) {
                        method.invoke(object, new BigInteger(propertyValue));
                        return;
                    }
                }
            }
        }
        throw new RuntimeException("Property not found: " + propertyName);
    }
}
