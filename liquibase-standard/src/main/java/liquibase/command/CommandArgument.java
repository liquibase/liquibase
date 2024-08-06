package liquibase.command;

import lombok.Getter;

import java.util.Objects;

/**
 * @deprecated Used by the old {@link LiquibaseCommand} style of command setup.
 */
@Getter
public class CommandArgument implements Comparable {

    private String name;
    private String description;
    private Class dataType;
    private boolean required;

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((CommandArgument) o).getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandArgument that = (CommandArgument) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
