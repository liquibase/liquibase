package liquibase.structure;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.core.Catalog;
import liquibase.util.StringUtils;

import java.util.*;

public class ObjectReference extends AbstractExtensibleObject implements Comparable<ObjectReference> {

    public Class<? extends DatabaseObject> type;
    public String name;
    public ObjectReference container;
    public boolean virtual;

    /**
     * Construct an ObjectName from the given string. If the string contains dots, it will be split into containers on the dots.
     * If null is passed, return an empty ObjectName
     */
    public static ObjectReference parse(String string) {
        if (string == null) {
            return new ObjectReference(null);
        }

//        String[] split = string.split("\\.");
//        return new ObjectReference(split);

        return null;
    }

    public ObjectReference() {
    }

    public ObjectReference(Class<? extends DatabaseObject> type, ObjectReference container, String... names) {
        this.type = type;
        if (names == null || names.length == 0) {
            this.container = container.container;
            this.name = container.name;
        } else if (names.length == 1) {
            this.container = container;
            this.name = names[0];
        } else {
            for (String name : names) {
                container = new ObjectReference(container, name);
            }
            this.container = container.container;
            this.name = container.name;
        }
    }

    public ObjectReference(ObjectReference container, String... names) {
        this(null, container, names);
    }

    public ObjectReference(Class<? extends DatabaseObject> type, String... names) {
        this(type, null, names);
    }

    public ObjectReference(String... names) {
        this(null, null, names);
    }

    public String toShortString() {
        return StringUtils.defaultIfEmpty(name, "#UNSET");
    }

    @Override
    public String toString() {
        String returnString;
        List<String> list = asList();
        if (list.size() == 0) {
            returnString = "UNNAMED";
        } else {
            returnString = StringUtils.join(list, ".", new StringUtils.StringUtilsFormatter<String>() {
                @Override
                public String toString(String obj) {
                    return StringUtils.defaultIfEmpty(obj, "UNNAMED");
                }
            });
        }

        if (type == null) {
            returnString += " (NO TYPE)";
        } else {
            returnString += " (" + type.getSimpleName().toUpperCase() + ")";
        }

        return returnString;
    }

    @Override
    public int compareTo(ObjectReference o) {
        if (o == null) {
            return 1;
        }
        return this.toString().compareTo(o.toString());
    }

    public boolean equalsIgnoreCase(ObjectReference name) {
        return this.name.equalsIgnoreCase(name.name);
    }


    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj instanceof ObjectReference) && obj.toString().equals(this.toString());
    }

    public boolean equals(ObjectReference obj, boolean ignoreLengthDifferences) {
        if (ignoreLengthDifferences) {
            List<String> thisNames = this.asList();
            List<String> otherNames = obj.asList();
            int precision = Math.min(thisNames.size(), otherNames.size());

            thisNames = thisNames.subList(thisNames.size() - precision, thisNames.size());
            otherNames = otherNames.subList(otherNames.size() - precision, otherNames.size());

            for (int i = 0; i < thisNames.size(); i++) {
                String thisName = thisNames.get(i);
                String otherName = otherNames.get(i);

                if (thisName == null) {
                    return otherName == null;
                }
                if (!thisName.equals(otherName)) {
                    return false;
                }
            }
            return true;
        } else {
            return this.toString().equals(obj.toString());
        }

    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }


    /**
     * Returns the {@link #asList()} result, but either null-padded out to the passed length, or truncated the the passed length
     */
    public List<String> asList(int length) {
        List<String> list = asList();
        if (length == list.size()) {
            return list;
        }
        if (length < list.size()) {
            return Collections.unmodifiableList(list.subList(list.size() - length, list.size()));
        }

        List<String> newList = new ArrayList<>(list);
        while (newList.size() < length) {
            newList.add(0, null);
        }
        return Collections.unmodifiableList(newList);
    }

    public List<String> asList() {
        if (name == null && container == null) {
            return new ArrayList<>();
        }

        List<String> returnList = new ArrayList<>();
        ObjectReference name = this;
        while (name != null) {
            returnList.add(0, name.name);
            name = name.container;
        }

        if (returnList.get(0) == null) {
            boolean sawNonNull = false;
            ListIterator<String> it = returnList.listIterator();
            while (it.hasNext()) {
                String next = it.next();
                if (next == null && !sawNonNull) {
                    it.remove();
                } else {
                    sawNonNull = true;
                }
            }
        }

        return Collections.unmodifiableList(returnList);
    }


    /**
     * Return the number of parent containers in this ObjectName.
     * Top-level containers with a null name are not counted in the depth, but null-named containers between named containers are counted.
     */
    public int depth() {
        List<String> array = asList();
        if (array.size() == 0) {
            return 0;
        }
        return array.size() - 1;
    }


    /**
     * Returns true if the names are equivalent, not counting null-value positions in either name
     */
    public boolean matches(ObjectReference objectReference) {
        if (objectReference == null) {
            return true;
        }

        List<String> thisList = this.asList();
        List<String> otherList = objectReference.asList();

        if (otherList.size() == 0) {
            return true;
        }

        int length = Math.max(thisList.size(), otherList.size());

        thisList = this.asList(length);
        otherList = objectReference.asList(length);

        for (int i = 0; i < length; i++) {
            String thisName = thisList.get(i);
            String otherName = otherList.get(i);
            if (thisName != null && otherName != null) {
                if (!thisName.equals(otherName)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns an objectName that is truncated to the given max length
     */
    public ObjectReference truncate(int maxLength) {
        List<String> names = this.asList();
        int length = Math.min(maxLength, names.size());

        return new ObjectReference(type, names.subList(names.size() - length, names.size()).toArray(new String[length]));
    }

    public boolean instanceOf(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(this.type);
    }
}
