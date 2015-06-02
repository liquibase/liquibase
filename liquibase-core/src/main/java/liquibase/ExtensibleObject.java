package liquibase;

import java.util.Map;
import java.util.Set;

/**
 * This interface defines how objects can be extended with additional attributes at runtime without subclassing and exposes the ability to query attributes without resorting to reflection.
 * Implementations of this interface can and should expose get/set methods for standard properties, but those methods should be wrappers around the get/set methods, not private fields.
 *
 * If creating an ExtensibleObject, it is usually best to extend {@link liquibase.AbstractExtensibleObject} rather than this interface directly.
 * You should also create a test that extends from AbstractExtensibleObjectTest.
 */
public interface ExtensibleObject {

    /**
     * Return the names of all the set attributes. If an attribute is null the name may or may not be returned.
     */
    Set<String> getAttributeNames();

    /**
     * Returns the names of standard attributes. Any other attributes can be set, but this list is helpful for testing and tools.
     */
    Set<String> getStandardAttributeNames();

    boolean has(String attribute);

    boolean has(Enum attribute);

    /**
     * Return the current value of the given attribute name, converted to the passed type.
     * If the passed attribute is null or not defined, returns null.
     * If you do not know the type to convert to, pass Object.class as the type.
     * Conversion is done using {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    <T> T get(String attribute, Class<T> type);

    /**
     * Works like {@link #get(String, java.lang.Class)} but if the attribute is null or not defined, returns the passed defaultValue.
     * Uses the type of defaultValue to determine the type to convert the current value to.
     *
     * If null is passed to the default value, no conversion of attribute is made if it is set.
     */
    <T> T get(String attribute, T defaultValue);


    /**
     * Works like {@link #get(String, Class)} but uses the Enum name as the attribute name.
     * By defining an enum containing the possible attributes for an object, you protect yourself from accidental misspellings and provide some level of intellisense.
     * By convention, Liquibase creates an "Attr" enum in extensible objects containing the standard list of attributes. See {@link liquibase.action.AbstractSqlAction.Attr} for an example.
     */
    <T> T get(Enum attribute, Class<T> type);

    <T> T get(Enum attribute, T defaultValue);

    /**
     * Sets the value of the given attribute.
     */
    ExtensibleObject set(String attribute, Object value);

    ExtensibleObject set(Enum attribute, Object value);


    /**
     * Adds the value to the collection at the given attribute. If the attribute is not defined, a List is created at the attribute.
     * If the attribute contains only a single value, it is converted to a List with the old value plus the new value.
     */
    ExtensibleObject add(String attribute, Object value);

    ExtensibleObject add(Enum attribute, Object value);
}
