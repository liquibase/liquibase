package liquibase.structure.core;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {

    public StoredProcedure() {
    }

    public StoredProcedure(String catalogName, String schemaName, String procedureName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(procedureName);
    }

    public String getProcedureName() {
        return getAttribute(PROCEDURE_NAME_ATTR, String.class);
    }

    public StoredProcedure setProcedureName(String procedureName) {
        setAttribute(PROCEDURE_NAME_ATTR, procedureName);
        return this;
    }

    public List<StoredLogicArgument> getArgs() {
        return (List<StoredLogicArgument>) getAttribute(ARGS_ATTR, List.class);
    }

    public StoredProcedure setArgs(List<StoredLogicArgument> args) {
        setAttribute(ARGS_ATTR, args);
        return this;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        List<StoredLogicArgument> args = StoredLogicArgument.loadAll(parsedNode.getChild(null, ARGS_ATTR), resourceAccessor);
        if (args == null || args.isEmpty()) {
            setArgs(null);
        } else {
            setArgs(args);
        }
    }

    /**
     * For databases (like Postgres) that support function overloading {@link #getName()} will contain id (name with input arguments)
     * so actual name should be fetched from {@link #getProcedureName()}
     */
    public static String getProcedureName(DatabaseObject dbObject) {
        if (dbObject instanceof StoredProcedure) {
            StoredProcedure proc = (StoredProcedure) dbObject;
            return StringUtils.isNotEmpty(proc.getProcedureName()) ? proc.getProcedureName() : proc.getName();
        }
        throw new IllegalArgumentException("Expected StoredProcedure, got: " + (dbObject == null ? "null" : dbObject.getClass().getName()));
    }

}
