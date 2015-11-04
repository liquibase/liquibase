package liquibase.command;

import liquibase.Scope;
import liquibase.action.core.DropForeignKeyAction;
import liquibase.action.core.DropTablesAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DropAllCommand extends AbstractCommand {

    public Set<DatabaseObject> containers = new HashSet<>();

    public DropAllCommand() {
    }

    public DropAllCommand(DatabaseObject... containers) {
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

        for (ForeignKey foreignKey : snapshotResult.snapshot.get(ForeignKey.class)) {
            scope.getSingleton(ActionExecutor.class).execute(new DropForeignKeyAction(foreignKey.getName(), foreignKey.columnChecks.get(0).baseColumn.container), scope);
        }

        for (Table table : snapshotResult.snapshot.get(Table.class)) {
            scope.getSingleton(ActionExecutor.class).execute(new DropTablesAction(table.getName()), scope);
        }

        return new CommandResult();
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

}
