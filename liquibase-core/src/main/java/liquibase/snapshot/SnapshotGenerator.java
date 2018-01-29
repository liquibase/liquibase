package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;

/*
 * A class that implements the SnapshotGenerator interface is capable of creating an object representation
 * ("a snapshot") of one (or more?) object types in one (or more) DBMSs.
 *
 */
public interface SnapshotGenerator {

    int PRIORITY_NONE = -1;
    int PRIORITY_DEFAULT = 1;
    int PRIORITY_DATABASE = 5;
    int PRIORITY_ADDITIONAL = 50;

    /**
     * Inquire if this SnapshotGenerator is capable of snapshotting objects of type
     * objectType (e.g. Table, Index, View etc.) in the DBMS database (e.g. Oracle, Postgres, HyperSQL etc.)
     * A return priority of > 0 will be interpreted as capable. The highest priority for an objectType-database
     * combination wins.
     *
     * @param objectType The object type we are asked to snapshot
     * @param database The DBMS for which the snapshotting should be done
     * @return An integer of PRIORITY_... constants indicating our capability and willingness to snapshot
     */
    int getPriority(Class<? extends DatabaseObject> objectType, Database database);

    /**
     * Commands the SnapshotGenerator to create a snapshot (a representation of a database object in Java object form)
     * of a specific database object.
     * @param example The object we should try to snapshot
     * @param snapshot The Snapshot object representating the result of the snapshot operations so far
     * @param chain A list of other SnapshotGenerators that might be asked to try the same
     * @param <T> The Java object type in which we are to return the snapshot result
     * @return An object of type T if our snapshot attempt is successful
     * @throws DatabaseException If an operation on the database fails
     * @throws InvalidExampleException If, for some reason, we cannot work on the example object (ambiguous naming etc.)
     */
    <T extends DatabaseObject> T snapshot(T example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain)
        throws DatabaseException, InvalidExampleException;

    /**
     * When snapshotting a certain object type (e.g. a table), different types of objects might be discovered in the
     * process.
     * (TODO: Which ones? Needs a more precise description.)
     * @return an array of classes that this SnapshotGenerator might return upon snapshotting a given
     * DatabaseObject type.
     */
    Class<? extends DatabaseObject>[] addsTo();

    /**
     * Returns classes (and superclasses) that this SnapshotGenerator replaces. Return null or empty array to not
     * affect the SnapshotGeneratorChain.
     */
    Class<? extends SnapshotGenerator>[] replaces();
}
