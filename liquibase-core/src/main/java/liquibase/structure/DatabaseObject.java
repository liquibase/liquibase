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

    /**
     * Sets the name for the database object.
     *
     * @param name the new name for the database object
     * @return a reference to the same object (implementing classes are expected to return a reference to the same
     * object).
     */
    DatabaseObject setName(String name);

    Schema getSchema();

    boolean snapshotByDefault();

    /**
     * Returns the name of all attributes currently stored for this {@link DatabaseObject}.
     *
     * @return the Set of all attribute names
     */
    Set<String> getAttributes();

    /**
     * Retrieves the value of a {@link DatabaseObject}'s attributes and cast it into the desired type.
     * @param attribute case-sensitive name of the attribute for which the value will be retrieved
     * @param type class compatible with the desired type T of the return value
     * @param <T> the desired type of the value
     * @return <ul>
     *     <li>if the attribute name exists, and the current value can be cast into the desired class, then the
     *     value is returned in the desired form. Note that null is a valid value, too.</li>
     *     <li>if the attribute name does not exist, null is returned.</li>
     *     <li>if the attribute has a value, but that value cannot be cast into the desired class, a
     *     {@link RuntimeException} will occur.</li>
     *     </ul>
     */
    <T> T getAttribute(String attribute, Class<T> type);

    /**
     * Retrieves the value of a {@link DatabaseObject}'s attributes and cast it into the desired type.
     * @param attribute case-sensitive name of the attribute for which the value will be retrieved
     * @param defaultValue the value to be returned if no value (not even null) is stored for the attribute name in the
     *                     object.
     * @param <T> the desired type of the value
     * @return <ul>
     *     <li>if the attribute name exists, and the current value can be cast into a type compatible with T, then
     *     value is returned in the desired form. Note that null is a valid value, too.</li>
     *     <li>if the attribute name does not exist, defaultValue is returned.</li>
     *     <li>if the attribute has a value, but that value cannot be cast into a type compatible with T, a
     *     {@link RuntimeException} will occur.</li>
     *     </ul>
     */
    <T> T getAttribute(String attribute, T defaultValue);

    /**
     * Sets a given attribute for this object to the specified value.
     * @param attribute case-sensitive name of the attribute
     * @param value value to be set
     * @return a reference to the same object
     */
    DatabaseObject setAttribute(String attribute, Object value);

}

