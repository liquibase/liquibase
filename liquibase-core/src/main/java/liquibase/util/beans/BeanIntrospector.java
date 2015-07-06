package liquibase.util.beans;

import java.beans.IntrospectionException;

public interface BeanIntrospector {
    void introspect(IntrospectionContext context) throws IntrospectionException;
}
