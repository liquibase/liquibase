package liquibase.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Boolean.class)) {
                    method.invoke(object, Boolean.valueOf(propertyValue));
                    return;
                } else
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(String.class)) {
                    method.invoke(object, propertyValue);
                    return;
                } else
                if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Integer.class)) {
                    method.invoke(object, Integer.valueOf(propertyValue));
                    return;
                }
            }
        }
        throw new RuntimeException("Property not found: " + propertyName);
    }
}
