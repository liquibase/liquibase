package liquibase.command;

import liquibase.Scope;
import liquibase.action.core.DropTableAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.actionlogic.ActionResult;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DropAllCommand extends AbstractCommand {

    public Set<ObjectReference> containers = new HashSet<>();

    public DropAllCommand() {
    }

    public DropAllCommand(ObjectReference... containers) {
        if (containers != null) {
            this.containers.addAll(Arrays.asList(containers));
        }
    }

    @Override
    public String getName() {
        return "dropAll";
    }


    @Override
    protected CommandResult run(Scope scope) throws Exception {
        SnapshotCommand snapshotCommand = new SnapshotCommand();
        snapshotCommand.relatedObjects.addAll(containers);
        SnapshotCommand.SnapshotCommandResult snapshotResult = snapshotCommand.execute(scope);

        for (Table table : snapshotResult.snapshot.get(Table.class)) {
            scope.getSingleton(ActionExecutor.class).execute(new DropTableAction(table.getName()), scope);
        }

        return new CommandResult();
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

}
