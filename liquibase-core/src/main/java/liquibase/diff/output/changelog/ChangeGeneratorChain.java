package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;

public class ChangeGeneratorChain {
    private Iterator<ChangeGenerator> changeGenerators;

    public ChangeGeneratorChain(SortedSet<ChangeGenerator> changeGenerators) {
        if (changeGenerators != null) {
            this.changeGenerators = changeGenerators.iterator();
        }
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase) {
        if (missingObject == null) {
            return null;
        }

        if (control.getObjectChangeFilter() != null && !control.getObjectChangeFilter().includeMissing(missingObject, referenceDatabase, comparisionDatabase)) {
            return null;
        }

//        if (!snapshot.getSnapshotControl().shouldInclude(example.getClass())) {
//            return null;
//        }

        if (changeGenerators == null) {
            return null;
        }

        if (!changeGenerators.hasNext()) {
            return null;
        }

        if (control.alreadyHandledMissing(missingObject, comparisionDatabase)) {
            return null;
        }

        Change[] changes = ((MissingObjectChangeGenerator) changeGenerators.next()).fixMissing(missingObject, control, referenceDatabase, comparisionDatabase, this);
        if (changes == null) {
            return null;
        }
        if (changes.length == 0) {
            return null;
        }
        return changes;
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase) {
        if (unexpectedObject == null) {
            return null;
        }

        if (control.getObjectChangeFilter() != null && !control.getObjectChangeFilter().includeUnexpected(unexpectedObject, referenceDatabase, comparisionDatabase)) {
            return null;
        }

//        if (!snapshot.getSnapshotControl().shouldInclude(example.getClass())) {
//            return null;
//        }

        if (changeGenerators == null) {
            return null;
        }

        if (!changeGenerators.hasNext()) {
            return null;
        }

        if (control.alreadyHandledUnexpected(unexpectedObject, comparisionDatabase)) {
            return null;
        }

        Change[] changes = ((UnexpectedObjectChangeGenerator) changeGenerators.next()).fixUnexpected(unexpectedObject, control, referenceDatabase, comparisionDatabase, this);
        if (changes == null) {
            return null;
        }
        if (changes.length == 0) {
            return null;
        }
        return changes;
    }

    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase) {
        if (changedObject == null) {
            return null;
        }

        if (control.getObjectChangeFilter() != null && !control.getObjectChangeFilter().includeChanged(changedObject, differences, referenceDatabase, comparisionDatabase)) {
            return null;
        }

//        if (!snapshot.getSnapshotControl().shouldInclude(example.getClass())) {
//            return null;
//        }

        if (changeGenerators == null) {
            return null;
        }

        if (!changeGenerators.hasNext()) {
            return null;
        }

        if (control.alreadyHandledChanged(changedObject, comparisionDatabase)) {
            return null;
        }

        Change[] changes = ((ChangedObjectChangeGenerator) changeGenerators.next()).fixChanged(changedObject, differences, control, referenceDatabase, comparisionDatabase, this);
        if (changes == null) {
            return null;
        }
        if (changes.length == 0) {
            return null;
        }
        return changes;
    }
}
