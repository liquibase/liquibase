package liquibase.util.beans;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;

public class FluentPropertyBeanIntrospector implements BeanIntrospector {
    @Override
    public void introspect(IntrospectionContext context) throws IntrospectionException {
        for (Method method : context.getTargetClass().getMethods()) {
            try {
                Class<?>[] argTypes = method.getParameterTypes();
                int argCount = argTypes.length;
                if (argCount == 1 && method.getName().startsWith("set")) {
                    String propertyName = Introspector.decapitalize(method.getName().substring(3));
                    if (!propertyName.equals("class")) {
                        PropertyDescriptor pd = context.getDescriptor(propertyName);
                        boolean setWriteMethod = false;
                        if (pd == null) {
                            pd = new PropertyDescriptor(propertyName, null, method);
                            context.addDescriptor(pd);
                            setWriteMethod = true;
                        } else if (pd.getWriteMethod() == null
                                && pd.getReadMethod() != null
                                && pd.getReadMethod().getReturnType() == argTypes[0]) {

                            pd.setWriteMethod(method);
                            setWriteMethod = true;
                        }
                        if (setWriteMethod) {
                            for (Class<?> type : method.getExceptionTypes()) {
                                if (type == PropertyVetoException.class) {
                                    pd.setConstrained(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IntrospectionException ignored) {
            }
        }
    }
}
