package liquibase.change.core;

import liquibase.change.Change;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.structure.core.StoredLogicArgument;

import java.util.*;

/** {@link Change} wrapper for {@link StoredLogicArgument}*/
public class StoredLogicArgumentChange extends AbstractLiquibaseSerializable {

    private String name;
    private String type;
    private String mode;

    @Override
    public String getSerializedObjectName() {
        return "argument";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getName() {
        return name;
    }

    public StoredLogicArgumentChange setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public StoredLogicArgumentChange setType(String type) {
        this.type = type;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public StoredLogicArgumentChange setMode(String mode) {
        this.mode = mode;
        return this;
    }

    /** Convert {@link StoredLogicArgument} to {@link StoredLogicArgumentChange} */
    public static List<StoredLogicArgumentChange> convert(Collection<StoredLogicArgument> args) {
        if (args == null) {
            return null;
        }
        List<StoredLogicArgumentChange> result = new ArrayList<StoredLogicArgumentChange>();
        for (StoredLogicArgument arg : args) {
            StoredLogicArgumentChange change = new StoredLogicArgumentChange()
                    .setName(arg.getName())
                    .setMode(arg.getMode())
                    .setType(arg.getType());
            result.add(change);
        }
        return result;
    }
}
