package liquibase.util.beans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class DefaultBeanIntrospector implements BeanIntrospector {
    @Override
    public void introspect(IntrospectionContext context) throws IntrospectionException {
        PropertyDescriptor[] descriptors = Introspector.getBeanInfo(context.getTargetClass()).getPropertyDescriptors();
        if (descriptors != null) {
            context.addDescriptors(descriptors);
        }
    }
}
