package liquibase.configuration;

import liquibase.Scope;
import liquibase.util.ValueHandlerUtil;

import java.util.concurrent.atomic.AtomicReference;

public class ConfigurationValueUtils {
    /**
     *
     * Call the convert method with the argument key in the current scope
     * so that it can be used in an error message
     * @param   key             The name of the argument
     * @param   value           The argument value
     * @param   converter       The converter method
     * @return  <T>
     *
     */
    public static <T> T convertDataType(String key, T value, ConfigurationValueConverter<T> converter) {
        AtomicReference<T> reference = new AtomicReference<>();
        try {
            Scope.child(ValueHandlerUtil.ARGUMENT_KEY, key, () ->
                    reference.set(converter.convert(value)));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return reference.get();
    }
}
