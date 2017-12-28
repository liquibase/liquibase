package liquibase.structure;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Schema;

import java.util.Set;

/**
 * Interface for all types of database objects can be manipulated using ChangeSets. Objects represented by
 * subclasses are not specific to any RDBMS and thus only contain "high-level" properties that can be found in most
 * DBMS. Examples for things that are represented using this interface are {@link liquibase.structure.core.Table},
 * {@link liquibase.structure.core.PrimaryKey} and {@link liquibase.structure.core.Column}.
 * <p>
 * The most important abilities of every DatabaseObject are:
 * <ul>
 * <li>Maintaining a list of attributes (key/value combinations representing the properties of a
 * DatabaseObject) and methods to manipulate them</li>
 * <li>Storing the full name of the object ([catalog and/or schema], object name etc.) to store the object in the
 * database and retrieve it</li>
 * </ul>
 *
 * @see AbstractDatabaseObject
 */
public interface DatabaseObject extends Comparable, LiquibaseSerializable {

    String getSnapshotId();

    void setSnapshotId(String id);

    DatabaseObject[] getContainingObjects();

    String getObjectTypeName();

    String getName();

    <T> T setName(String name);

    Schema getSchema();

    boolean snapshotByDefault();

    Set<String> getAttributes();

    <T> T getAttribute(String attribute, Class<T> type);

    <T> T getAttribute(String attribute, T defaultValue);

    DatabaseObject setAttribute(String attribute, Object value);

}

