package liquibase;

import liquibase.util.StringUtil;

import java.lang.reflect.Type;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Contains metadata about an {@link ExtensibleObject}.
 * <p/>
 * Because attributes can be dynamically added or removed, different instances of the same type may return different metadata.
 */
public class ObjectMetaData {

    /**
     * Metadata about all the object's attributes.
     */
    public SortedSet<Attribute> attributes = new TreeSet<>();

    /**
     * Convenience method to return the {@link liquibase.ObjectMetaData.Attribute} information for the given attribute.
     * Returns null if the attribute name doesn't exist.
     */
    public Attribute getAttribute(String attributeName) {
        for (Attribute attribute : attributes) {
            if (attribute.name.equals(attributeName)) {
                return attribute;
            }
        }
        return null;
    }

    /**
     * Metadata about a particular attribute.
     */
    public static class Attribute implements Comparable<Attribute> {

        /**
         * Name of the attribute.
         */
        public String name;

        /**
         * Description of the attribute.
         */
        public String description;

        /**
         * True if the attribute is required.
         * What "required" means can depend on the type of object, but in general it should mean that the object is not "valid" if a value isn't set.
         */
        public Boolean required;

        /**
         * Return the stored type of the given attribute. Include any applicable generics information.
         */
        public Type type;


        public Attribute() {
        }

        public Attribute(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(Attribute o) {
            return StringUtil.trimToEmpty(this.name).compareTo(StringUtil.trimToEmpty(o.name));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Attribute)) {
                return false;
            }
            return this.name.equals(((Attribute) obj).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
