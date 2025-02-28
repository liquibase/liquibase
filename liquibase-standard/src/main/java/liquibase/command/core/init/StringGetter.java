package liquibase.command.core.init;

import liquibase.util.StringUtil;

public class StringGetter extends AbstractCommandLineValueGetter<String> {
    private final boolean allowEmpty;
    /**
     * Create a new value getter.
     */
    public StringGetter(boolean allowEmpty) {
        super(String.class);
        this.allowEmpty = allowEmpty;
    }

    @Override
    public boolean validate(String input) {
        if (allowEmpty) {
            return true;
        } else {
            return StringUtil.isNotEmpty(input);
        }
    }

    @Override
    public String convert(String input) {
        return input;
    }

    @Override
    public String describe() {
        if (allowEmpty) {
            return "string";
        } else {
            return "non-empty string";
        }
    }
}