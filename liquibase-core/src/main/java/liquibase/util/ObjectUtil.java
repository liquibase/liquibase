package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Various methods that make it easier to read and write object properties using the propertyName, instead of having
 * to look up the correct reader/writer methods manually first. All methods in this class are static by nature.
 */
public class ObjectUtil {

    /**
     * Cache for the methods of classes that we have been queried about so far.
     */
    private static Map<Class<?>, Method[]> methodCache = new HashMap<>();

    /**
     * For a given object, try to find the appropriate reader method and return the value, if set
     * for the object. If the property is currently not set for the object, an
     * {@link UnexpectedLiquibaseException} run-time exception occurs.
     *
     * @param object                        the object to examine
     * @param propertyName                  the property name for which the value should be read
     * @return                              the stored value
     */
    public static Object getProperty(Object object, String propertyName)
        throws UnexpectedLiquibaseException {
        Method readMethod = getReadMethod(object, propertyName);
        if (readMethod == null) {
            throw new UnexpectedLiquibaseException(
                String.format("Property [%s] was not found for object type [%s]", propertyName,
                    object.getClass().getName()
                ));
        }

        try {
            return readMethod.invoke(object);
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Tried to determine the appropriate reader method for a given propertyName of a given object and, if found,
     * returns the class of its return type.
     * @param object the object to examine
     * @param propertyName the property name whose reading method should be searched
     * @return the class name of the return type if the reading method is found, null if it is not found.
     */
    public static Class getPropertyType(Object object, String propertyName) {
        Method readMethod = getReadMethod(object, propertyName);
        if (readMethod == null) {
            return null;
        }
        return readMethod.getReturnType();
    }

    /**
     * Examines the given object's class and returns true if reader and writer methods both exist for the
     * given property name.
     * @param object the object for which the class should be examined
     * @param propertyName the property name to search
     * @return true if both reader and writer methods exist
     */
    public static boolean hasProperty(Object object, String propertyName) {
        return hasReadProperty(object, propertyName) && hasWriteProperty(object, propertyName);
    }

    /**
     * Examines the given object's class and returns true if a reader method exists for the
     * given property name.
     * @param object the object for which the class should be examined
     * @param propertyName the property name to search
     * @return true if a reader method exists
     */
    public static boolean hasReadProperty(Object object, String propertyName) {
        return getReadMethod(object, propertyName) != null;
    }

    /**
     * Examines the given object's class and returns true if a writer method exists for the
     * given property name.
     * @param object the object for which the class should be examined
     * @param propertyName the property name to search
     * @return true if a writer method exists
     */
    public static boolean hasWriteProperty(Object object, String propertyName) {
        return getWriteMethod(object, propertyName) != null;
    }

    /**
     * Tries to guess the "real" data type of propertyValue by the given propertyName, then sets the
     * selected property of the given object to that value.
     * @param object                        the object whose property should be set
     * @param propertyName                  name of the property to set
     * @param propertyValue                 new value of the property, as String
     */
    public static void setProperty(Object object, String propertyName, String propertyValue)  {
        Method method = getWriteMethod(object, propertyName);
        if (method == null) {
            throw new UnexpectedLiquibaseException (
                String.format("Property [%s] was not found for object type [%s]", propertyName,
                    object.getClass().getName()
                ));
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
        } else if (parameterType.equals(BigDecimal.class)) {
            finalValue = new BigDecimal(propertyValue);
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
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (IllegalArgumentException e) {
            throw new UnexpectedLiquibaseException("Cannot call " + method.toString()
                + " with value of type " + finalValue.getClass().getName());
        }
    }

    /**
     * Sets the selected property of the given object to propertyValue. A run-time exception will occur if the
     * type of value is incompatible with the reader/writer method signatures of the given propertyName.
     * @param object                        the object whose property should be set
     * @param propertyName                  name of the property to set
     * @param propertyValue                 new value of the property
     */
    public static void setProperty(Object object, String propertyName, Object propertyValue) {
        Method method = getWriteMethod(object, propertyName);
        if (method == null) {
            throw new UnexpectedLiquibaseException (
                String.format("Property [%s] was not found for object type [%s]", propertyName,
                    object.getClass().getName()
                ));
        }

        try {
            if (propertyValue == null) {
                setProperty(object, propertyName, null);
                return;
            }
            if (!method.getParameterTypes()[0].isAssignableFrom(propertyValue.getClass())) {
                setProperty(object, propertyName, propertyValue.toString());
                return;
            }

            method.invoke(object, propertyValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (IllegalArgumentException e) {
            throw new UnexpectedLiquibaseException("Cannot call " + method.toString() + " with value of type "
                + (propertyValue == null ? "null" : propertyValue.getClass().getName()));
        }
    }

    /**
     * Tries to find the Java method to read a given propertyName for the given object.
     * @param object the object whose class will be examined
     * @param propertyName the property name for which the read method should be searched
     * @return the {@link Method} if found, null in all other cases.
     */
    private static Method getReadMethod(Object object, String propertyName) {
        String getMethodName = "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH)
            + propertyName.substring(1);
        String isMethodName = "is" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH)
            + propertyName.substring(1);

        Method[] methods = getMethods(object);

        for (Method method : methods) {
            if ((method.getName().equals(getMethodName) || method.getName().equals(isMethodName)) && (method
                .getParameterTypes().length == 0)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Tries to find the Java method to write a new value for a given propertyName to the given object.
     * @param object the object whose class will be examined
     * @param propertyName the property name for which the write method is to be searched
     * @return the {@link Method} if found, null in all other cases.
     */
    private static Method getWriteMethod(Object object, String propertyName) {
        String methodName = "set"
            + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method[] methods = getMethods(object);

        for (Method method : methods) {
            if (method.getName().equals(methodName) && (method.getParameterTypes().length == 1)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Determines the class of a given object and returns an array of that class's methods. The information might come
     * from a cache.
     * @param object the object to examine
     * @return a list of methods belonging to the class of the object
     */
    private static Method[] getMethods(Object object) {
        Method[] methods = methodCache.get(object.getClass());

        if (methods == null) {
            methods = object.getClass().getMethods();
            methodCache.put(object.getClass(), methods);
        }
        return methods;
    }

      /**
     * Converts the given object to the targetClass
     */
    public static <T> T convert(Object object, Class<T> targetClass) throws IllegalArgumentException {
        if (object == null) {
            return null;
        }
        if (targetClass.isAssignableFrom(object.getClass())) {
            return (T) object;
        }

        try {
            if (Enum.class.isAssignableFrom(targetClass)) {
                return (T) Enum.valueOf((Class<Enum>) targetClass, object.toString());
            } else if (Number.class.isAssignableFrom(targetClass)) {
                if (object instanceof Number) {
                    Number number = (Number) object;
                    String numberAsString = number.toString();
                    numberAsString = numberAsString.replaceFirst("\\.0+$", ""); //remove zero decimal so int/long/etc. can parse it correctly.

                    if (targetClass.equals(Byte.class)) {
                        long value = Long.valueOf(numberAsString);
                        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                            raiseOverflowException(number, targetClass);
                        }
                        return (T) (Byte) number.byteValue();
                    } else if (targetClass.equals(Short.class)) {
                        long value = Long.valueOf(numberAsString);
                        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                            raiseOverflowException(number, targetClass);
                        }
                        return (T) (Short) number.shortValue();
                    } else if (targetClass.equals(Integer.class)) {
                        long value = Long.valueOf(numberAsString);
                        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                            raiseOverflowException(number, targetClass);
                        }
                        return (T) (Integer) number.intValue();
                    } else if (targetClass.equals(Long.class)) {
                        return (T) Long.valueOf(numberAsString);
                    } else if (targetClass.equals(Float.class)) {
                        return (T) (Float) number.floatValue();
                    } else if (targetClass.equals(Double.class)) {
                        return (T) (Double) number.doubleValue();
                    } else if (targetClass.equals(BigInteger.class)) {
                        return (T) new BigInteger(numberAsString);
                    } else if (targetClass.equals(BigDecimal.class)) {
                        // using BigDecimal(String) here, to avoid unpredictability of BigDecimal(double)
                        // (see BigDecimal javadoc for details)
                        return (T) new BigDecimal(numberAsString);
                    } else {
                        return raiseUnknownConversionException(object, targetClass);
                    }
                } else if (object instanceof String) {
                    String string = (String) object;
                    if (string.contains(".")) {
                        string = string.replaceFirst("\\.0+$", "");
                    }
                    if (string.equals("")) {
                        string = "0";
                    }
                    if (targetClass.equals(Byte.class)) {
                        return (T) Byte.decode(string);
                    } else if (targetClass.equals(Short.class)) {
                        return (T) Short.decode(string);
                    } else if (targetClass.equals(Integer.class)) {
                        return (T) Integer.decode(string);
                    } else if (targetClass.equals(Long.class)) {
                        return (T) Long.decode(string);
                    } else if (targetClass.equals(Float.class)) {
                        return (T) Float.valueOf(string);
                    } else if (targetClass.equals(Double.class)) {
                        return (T) Double.valueOf(string);
                    } else if (targetClass.equals(BigInteger.class)) {
                        return (T) new BigInteger(string);
                    } else if (targetClass.equals(BigDecimal.class)) {
                        return (T) new BigDecimal(string);
                    } else {
                        return raiseUnknownConversionException(object, targetClass);
                    }
                } else {
                    return raiseUnknownConversionException(object, targetClass);
                }
            } else if (targetClass.isAssignableFrom(Boolean.class)) {
                String lowerCase = object.toString().toLowerCase();
                return (T) (Boolean) (lowerCase.equals("true") || lowerCase.equals("t") || lowerCase.equals("1") || lowerCase.equals("1.0") || lowerCase.equals("yes"));
            } else if (targetClass.isAssignableFrom(String.class)) {
                return (T) object.toString();
            } else if (targetClass.isAssignableFrom(List.class)) {
                if (object instanceof List) {
                    return (T) object;
                } else if (object instanceof Collection) {
                    return (T) new ArrayList((Collection) object);
                } else if (object instanceof Object[]) {
                    return (T) new ArrayList(Arrays.asList((Object[]) object));
                } else {
                    return (T) object;
                }
            } else if (targetClass.isAssignableFrom(StringClauses.class)) {
                return (T) new StringClauses().append(object.toString());
            } else if (targetClass.isAssignableFrom(Class.class)) {
                try {
                    return (T) Class.forName(object.toString());
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return (T) object;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static <T> T raiseUnknownConversionException(Object object, Class<T> targetClass) {
        throw new IllegalArgumentException("Could not convert '" + object + "' of type " + object.getClass().getName() + " to unknown target class " + targetClass.getName());
    }

    private static void raiseOverflowException(Number number, Class targetClass) {
        throw new IllegalArgumentException("Could not convert '" + number + "' of type " + number.getClass().getName() + " to target class " + targetClass.getName() + ": overflow");
    }

    /**
     * Return the defaultValue if the passed value is null. Otherwise, return the original value.
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }
}
