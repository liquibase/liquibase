package liquibase.command;

import liquibase.Scope;
import liquibase.structure.core.Schema;

public class DropAllCommand extends AbstractCommand {

    public Schema schema;

    public DropAllCommand() {
    }

    public DropAllCommand(Schema schema) {
        this.schema = schema;
    }

    @Override
    public String getName() {
        return "dropAll";
    }


    @Override
    protected CommandResult run(Scope scope) throws Exception {
        return null;
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

}
