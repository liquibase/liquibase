package liquibase.command;

import liquibase.Scope;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;
import liquibase.util.LiquibaseUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SnapshotCommand extends AbstractCommand<SnapshotCommand.SnapshotCommandResult> {

    public Set<ObjectReference> relatedObjects = new HashSet<>();

    public SnapshotCommand() {
    }

    public SnapshotCommand(ObjectReference... relatedObjects) {
        if (relatedObjects != null) {
            this.relatedObjects.addAll(Arrays.asList(relatedObjects));
        }
    }

    @Override
    public String getName() {
        return "snapshot";
    }

    @Override
    protected SnapshotCommandResult run(Scope scope) throws Exception {

        Set<Class<? extends DatabaseObject>> types = new HashSet((List) Arrays.asList(Table.class, ForeignKey.class)); //TODO: scope.getSingleton(DatabaseObjectFactory.class).getStandardTypes();

        Snapshot snapshot = new Snapshot(scope);

        for (ObjectReference related : relatedObjects) {
            for (Class type : types) {
                snapshot.addAll(LiquibaseUtil.snapshotAll(type, related, scope));
            }
        }

        return new SnapshotCommandResult(snapshot);
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public static class SnapshotCommandResult extends CommandResult {

        public Snapshot snapshot;

        public SnapshotCommandResult() {
        }

        public SnapshotCommandResult(Snapshot snapshot) {
            this.snapshot = snapshot;
        }
    }
}
