package liquibase.util;

import static java.util.Locale.ROOT;
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
    return lookup(readMethods, propertyName);
  }

  Method getWriteMethod(String propertyName) {
    return lookup(writeMethods, propertyName);
  }

  /**
   * Finds the method for a property name. Before the methods were indexed by property name the lookup built
   * "get"/"is"/"set" plus the name with its first letter capitalized, so changelogs could spell a property with
   * either an initial lowercase or uppercase letter (e.g. {@code SchemaName} for {@code setSchemaName}). That
   * is kept as a fallback when there is no method for the exact name.
   */
  private Method lookup(Map<String, Method> methods, String propertyName) {
    Method method = methods.get(propertyName);
    if (method != null || propertyName == null || propertyName.isEmpty()) {
      return method;
    }
    String capitalized = propertyName.substring(0, 1).toUpperCase(ROOT) + propertyName.substring(1);
    return methods.get(Introspector.decapitalize(capitalized));
  }

}
