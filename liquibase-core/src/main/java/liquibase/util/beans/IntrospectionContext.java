package liquibase.util.beans;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntrospectionContext {
    private final Class<?> targetClass;
    private final Map<String, PropertyDescriptor> descriptors = new HashMap<String, PropertyDescriptor>();

    public IntrospectionContext(Class<?> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException();
        }
        this.targetClass = targetClass;
    }

    public void addDescriptor(PropertyDescriptor descriptor) {
        descriptors.put(descriptor.getName(), descriptor);
    }

    public void addDescriptors(PropertyDescriptor[] descriptors) {
        for (PropertyDescriptor descriptor : descriptors) {
            addDescriptor(descriptor);
        }
    }

    public PropertyDescriptor getDescriptor(String name) {
        return descriptors.get(name);
    }

    public PropertyDescriptor[] getDescriptors() {
        return descriptors.values().toArray(new PropertyDescriptor[descriptors.values().size()]);
    }

    public Set<String> getPropertyNames() {
        return descriptors.keySet();
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public boolean hasProperty(String name) {
        return descriptors.containsKey(name);
    }

    public void removeProperty(String name) {
        descriptors.remove(name);
    }
}
