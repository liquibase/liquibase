package liquibase.util;

import static java.util.stream.Collectors.toMap;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

final class ObjectMethods {

  private static final BinaryOperator<Method> ignoreMultipleMethodsWithSameName = (first, second) -> first;
  private final Map<String, Method> writeMethods;
  private final Map<String, Method> readMethods;

  ObjectMethods(Class klass) {
    this(klass.getMethods());
  }

  ObjectMethods(Method[] methods) {
    readMethods = new HashMap<>(methods.length);
    readMethods.putAll(find(methods, 0, "get"));
    readMethods.putAll(find(methods, 0, "is"));
    writeMethods = find(methods, 1, "set");
  }

  private Map<String, Method> find(Method[] methods, int parametersCount, String prefix) {
    return Arrays.stream(methods)
      .filter(m -> m.getParameterTypes().length == parametersCount)
      .filter(m -> m.getName().startsWith(prefix))
      .collect(toMap(m -> propertyName(m.getName(), prefix), m -> m, ignoreMultipleMethodsWithSameName));
  }

  private String propertyName(String methodName, String prefix) {
    return Introspector.decapitalize(methodName.substring(prefix.length()));
  }

  Method getReadMethod(String propertyName) {
    return readMethods.get(propertyName);
  }

  Method getWriteMethod(String propertyName) {
    return writeMethods.get(propertyName);
  }

}
