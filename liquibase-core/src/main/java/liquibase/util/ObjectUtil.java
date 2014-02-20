package liquibase.util;

import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BigIntType;
import liquibase.datatype.core.DecimalType;
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

	private static Map<Class<?>,Method[]>methodCache = new HashMap<Class<?>, Method[]>();
    
	/**
	 * has <code>dataType</code> auto increment property ?
	 * @param dataType
	 * @return
	 * 
	 * @author justcoon
	 * 
	 * @see DecimalType#isAutoIncrement()
	 * @see BigIntType#isAutoIncrement()
	 */
	public static boolean isAutoIncrement(LiquibaseDataType dataType){
        boolean retVal = false;
        String methodName = "isAutoIncrement";
        Method[] methods = dataType.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
                retVal = true;
                break;
            }
        }

        return retVal;
    }

    public static Object getProperty(Object object, String propertyName) throws IllegalAccessException, InvocationTargetException {
        String methodName = "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
                return method.invoke(object);
            }
        }
        throw new RuntimeException("Property not found: " + propertyName);
    }

    public static void setProperty(Object object, String propertyName, String propertyValue) throws IllegalAccessException, InvocationTargetException {
        String methodName = "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method[] methods;
        
        methods = methodCache.get(object.getClass());
        
        if (methods == null) {
        	methods = object.getClass().getMethods();
        	methodCache.put(object.getClass(), methods);
        }
        
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?> parameterType = method.getParameterTypes()[0];
                if (method.getParameterTypes().length == 1) {
                    if (parameterType.equals(Boolean.class) || parameterType.equals(boolean.class)) {
                        method.invoke(object, Boolean.valueOf(propertyValue));
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
                    } else if (parameterType.equals(DatabaseFunction.class)) {
                        method.invoke(object, new DatabaseFunction(propertyValue));
                        return;
                    } else if (parameterType.equals(SequenceNextValueFunction.class)) {
                        method.invoke(object, new SequenceNextValueFunction(propertyValue));
                        return;
                    } else if (parameterType.equals(SequenceCurrentValueFunction.class)) {
                        method.invoke(object, new SequenceCurrentValueFunction(propertyValue));
                        return;
                    }                }
            }
        }
        throw new RuntimeException("Property '" + propertyName+"' not found on object type "+object.getClass().getName());
    }
}
