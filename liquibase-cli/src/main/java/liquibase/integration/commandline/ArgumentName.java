package liquibase.integration.commandline;

import java.util.Objects;

public class ArgumentName {
    private final String argumentName;
    /**
     * If true, the alias will be printed to the console when the help output is generated.
     */
    private final boolean forcePrintAlias;

    public ArgumentName(String argumentName, boolean forcePrintAlias) {
        this.argumentName = argumentName;
        this.forcePrintAlias = forcePrintAlias;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public boolean isForcePrintAlias() {
        return forcePrintAlias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgumentName that = (ArgumentName) o;
        return forcePrintAlias == that.forcePrintAlias && Objects.equals(argumentName, that.argumentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argumentName, forcePrintAlias);
    }
}
