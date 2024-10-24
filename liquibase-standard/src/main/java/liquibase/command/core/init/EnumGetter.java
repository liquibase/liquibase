package liquibase.command.core.init;

import liquibase.util.StringUtil;

import java.util.Arrays;

public class EnumGetter<E extends Enum> extends AbstractCommandLineValueGetter<E> {

    private final Class<E> e;
    private final boolean ignoreCase;

    public EnumGetter(Class<E> e, boolean ignoreCase) {
        super(e);
        this.e = e;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean validate(E input) {
        return true;
    }

    @Override
    public E convert(String input) {
        if (ignoreCase) {
            return getEnumIgnoreCase(input, e);
        } else {
            return (E) Enum.valueOf(e, input);
        }
    }

    public static <T extends Enum> T getEnumIgnoreCase(String name, Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(name)).findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find matching value. Valid options include: '%s'.",
                        StringUtil.join(enumClass.getEnumConstants(), "', '", Object::toString))));
    }
}