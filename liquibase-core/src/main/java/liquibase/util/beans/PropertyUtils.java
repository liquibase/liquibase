package liquibase.util.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PropertyUtils {
    private static final PropertyUtils INSTANCE = new PropertyUtils();

    public static PropertyUtils getInstance() {
        return INSTANCE;
    }

    private final List<BeanIntrospector> introspectors = new ArrayList<BeanIntrospector>(Arrays.asList(
            new DefaultBeanIntrospector(),
            new FluentPropertyBeanIntrospector()));

    public PropertyDescriptor[] getDescriptors(Class<?> targetClass) throws IntrospectionException {
        IntrospectionContext context = new IntrospectionContext(targetClass);
        for (BeanIntrospector introspector : introspectors) {
            introspector.introspect(context);
        }
        return context.getDescriptors();
    }

    private PropertyUtils() {
    }
}
