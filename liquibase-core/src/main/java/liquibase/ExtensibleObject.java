package liquibase;

import java.util.List;
import java.util.SortedSet;

/**
 * This interface defines how objects can be extended with additional attributes at runtime without subclassing and exposes the ability to query attributes without resorting to reflection.
 *
 * If creating an ExtensibleObject, it is usually best to extend {@link liquibase.AbstractExtensibleObject} rather than this interface directly.
 * You should also create a test that extends from AbstractExtensibleObjectTest.
 */
public interface ExtensibleObject extends Cloneable {

    /**
     * Return the names of all the set attributes.
     * If an attribute is null the name should not be returned.
     * Should return both "standard" attributes and any custom attributes that have been set.
     */
    SortedSet<String> getAttributes();

    /**
     * Returns the {@link ObjectMetaData} describing this instance.
     */
    ObjectMetaData getObjectMetaData();

    /**
     * Returnsn true if the given attribute is set and not null.
     */
    boolean has(String attribute);

    /**
     * Traverses dot-separated attributes in the attributePath and returns a list containing all the intermediate values.
     *
     *  @param lastType the type to convert the last value in the list to.
     */
    List getValuePath(String attributePath, Class lastType);

    /**
     * Return the current value of the given attribute name, converted to the passed type.
     * If the passed attribute is null or not defined, returns null.
     * If you do not know the type to convert to, pass Object.class as the type.
     * Conversion is done using {@link liquibase.util.ObjectUtil#convert(Object, Class)}.
     * Should traverse dot-separated attributes.
     */
    <T> T get(String attribute, Class<T> type);

    /**
     * Works like {@link #get(String, Class)} but if the attribute is null or not defined, returns the passed defaultValue.
     * Uses the type of defaultValue to determine the type to convert the current value to.
     *
     * If null is passed to the default value, no conversion of attribute is made if it is set.
     * If traversing a dot-separated attribute path, return the default value if any along the path are null.
     */
    <T> T get(String attribute, T defaultValue);

    /**
     * Sets the value of the given attribute.
     * Subclasses can override this method to provide conversion business logic, but must remember that fields can be set directly when no type conversion is needed.
     */
    ExtensibleObject set(String attribute, Object value);

    /**
     * Output a full description of this object. Should include all attributes and values.
     */
    String describe();

    /**
     * Expose {@link Cloneable#clone()} as public
     */
    Object clone();
}
