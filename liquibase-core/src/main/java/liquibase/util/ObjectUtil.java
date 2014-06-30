package liquibase.util;

import liquibase.ContextExpression;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

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
        } else if (ContextExpression.class.isAssignableFrom(parameterType)) {
            finalValue = new ContextExpression(propertyValue);
        } else if (Set.class.isAssignableFrom(parameterType)) {
            finalValue = new HashSet(Arrays.asList(propertyValue.split("\\s*,\\s*")));
        }
        try {
            method.invoke(object, finalValue);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (IllegalArgumentException e) {
            throw new UnexpectedLiquibaseException("Cannot call " + method.toString() + " with value of type " + finalValue.getClass().getName());
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

    public static <T> T convert(Object object, Class<T> targetClass) throws IllegalArgumentException {
        if (object == null) {
            return null;
        }
        if (targetClass.isAssignableFrom(object.getClass())) {
            return (T) object;
        }

        try {
            if (Number.class.isAssignableFrom(targetClass)) {
                if (object instanceof Number) {
                    Number number = (Number) object;
                    if (targetClass.equals(Byte.class)) {
                        long value = number.longValue();
                        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                            raiseOverflowException(number, targetClass);
                        }
                        return (T) (Byte) number.byteValue();
                    } else if (targetClass.equals(Short.class)) {
                        long value = number.longValue();
                        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                            raiseOverflowException(number, targetClass);
                        }
                        return (T) (Short) number.shortValue();
                    } else if (targetClass.equals(Integer.class)) {
                        long value = number.longValue();
                        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                            raiseOverflowException(number, targetClass);
                        }
                        return (T) (Integer) number.intValue();
                    } else if (targetClass.equals(Long.class)) {
                        return (T) (Long) number.longValue();
                    } else if (targetClass.equals(Float.class)) {
                        return (T) (Float) number.floatValue();
                    } else if (targetClass.equals(Double.class)) {
                        return (T) (Double) number.doubleValue();
                    } else if (targetClass.equals(BigInteger.class)) {
                        return (T) BigInteger.valueOf(number.longValue());
                    } else if (targetClass.equals(BigDecimal.class)) {
                        // using BigDecimal(String) here, to avoid unpredictability of BigDecimal(double)
                        // (see BigDecimal javadoc for details)
                        return (T) new BigDecimal(number.toString());
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
            }
            return (T) object;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected static <T> T raiseUnknownConversionException(Object object, Class<T> targetClass) {
        throw new IllegalArgumentException("Could not convert '" + object + "' of type " + object.getClass().getName() + " to unknown target class " + targetClass.getName());
    }

    private static void raiseOverflowException(Number number, Class targetClass) {
        throw new IllegalArgumentException("Could not convert '" + number + "' of type " + number.getClass().getName() + " to target class " + targetClass.getName() + ": overflow");
    }
}
