package liquibase.structure.core;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.*;

/**
 * Define Procedure/Function parameter arguments
 * - name -- argument name (optional)
 * - type -- argument data type (required)
 * - mode -- IN/OUT/INOUT/etc mode (optional; if not defined argument is treated as IN by default)
 */
public class StoredLogicArgument extends AbstractLiquibaseSerializable {

    /** sudo-mode to mark method signature that don't accept input arguments */
    public static String EMPTY_ARGUMENTS_MODE = "EMPTY";
    public static Set<String> INPUT_ARGUMENT_MODES = new HashSet<String>();
    static {
        INPUT_ARGUMENT_MODES.add("IN");
    }

    private String name;
    private String mode;
    private String type;

    public String getName() {
        return name;
    }

    public StoredLogicArgument setName(String name) {
        this.name = name;
        return this;
    }

    public StoredLogicArgument setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public StoredLogicArgument setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public String getSerializedObjectName() {
    return "arg";
}

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    /** Convenience method to call {@link #load(ParsedNode, ResourceAccessor)} for each element of arguments list */
    public static List<StoredLogicArgument> loadAll(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        List<StoredLogicArgument> result = new ArrayList<StoredLogicArgument>();
        if (parsedNode == null) {
            return result;
        }
        List<ParsedNode> args = parsedNode.getChildren();
        if (args == null || args.isEmpty()) {
            return result;
        }
        for (ParsedNode arg : args) {
            StoredLogicArgument a = new StoredLogicArgument();
            a.load(arg, resourceAccessor);
            result.add(a);
        }
        return result;
    }

    /** Convenience method to create no-argument function/procedure entry */
    public static StoredLogicArgument empty() {
        return new StoredLogicArgument().setMode(EMPTY_ARGUMENTS_MODE);
    }
}
