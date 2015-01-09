package liquibase;

import java.util.Set;
import java.util.SortedMap;

/**
 * This interface defines how objects can be extended with additional attributes at runtime without subclassing and exposes the ability to query attributes without resorting to reflection.
 * Implementations of this interface can and should expose get/set methods for standard properties, but those methods should be wrappers around getAttribute/setAttribute, not private fields.
 *
 * If creating an ExtensibleObject, it is usually best to extend {@link liquibase.AbstractExtensibleObject} rather than this interface directly.
 * You should also create a test that extends from AbstractExtensibleObjectTest.
 */
public interface ExtensibleObject {

    /**
     * Return the names of all the set attributes. If an attribute is null the name may or may not be returned.
     */
    Set<String> getAttributes();

    /**
     * Return the current value of the given attribute name, converted to the passed type.
     * If the passed attribute is null or not defined, returns null.
     * If you do not know the type to convert to, pass Object.class as the type.
     * Conversion is done using {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    <T> T getAttribute(String attribute, Class<T> type);

    /**
     * Works like {@link #getAttribute(String, java.lang.Class)} but if the attribute is null or not defined, returns the passed defaultValue.
     * Uses the type of defaultValue to determine the type to convert the current value to.
     *
     * If null is passed to the default value, no conversion of attribute is made if it is set.
     */
    <T> T getAttribute(String attribute, T defaultValue);


    /**
     * Works like {@link #getAttribute(String, Class)} but uses the Enum name as the attribute name.
     * By defining an enum containing the possible attributes for an object, you protect yourself from accidental misspellings and provide some level of intellisense.
     * By convention, Liquibase creates an "Attr" enum in extensible objects containing the standard list of attributes. See {@link liquibase.action.AbstractSqlAction.Attr} for an example.
     */
    <T> T getAttribute(Enum attribute, Class<T> type);

    <T> T getAttribute(Enum attribute, T defaultValue);

    /**
     * Sets the value of the given attribute.
     */
    Object setAttribute(String attribute, Object value);

    Object setAttribute(Enum attribute, Object value);
}
