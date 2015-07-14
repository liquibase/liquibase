package liquibase.structure;

import liquibase.AbstractExtensibleObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ObjectName extends AbstractExtensibleObject implements Comparable<ObjectName> {

    public String name;
    public ObjectName container;
    public boolean virtual;

    /**
     * Construct an ObjectName from the given string. If the string contains dots, it will be split into containers on the dots.
     * If null is passed, return an empty ObjectName
     */
    public static ObjectName parse(String string) {
        if (string == null) {
            return new ObjectName(null);
        }

        String[] split = string.split("\\.");
        return new ObjectName(split);
    }

    public ObjectName(ObjectName container, String name) {
        this.name = name;
        this.container = container;
    }

    /**
     * Construct a new ObjectName, from a passed list of container names.
     * Name list goes from most general to most specific: new ObjectName("catalogName", "schemaName", "tablenName")
     */
    public ObjectName(String... names) {
        if (names == null || names.length == 0) {
            this.name = null;
        } else {
            ObjectName container = null;
            for (int i = 0; i < names.length - 1; i++) {
                container = new ObjectName(container, names[i]);
            }
            this.name = names[names.length - 1];
            this.container = container;
        }
    }

    public String toShortString() {
        return StringUtils.defaultIfEmpty(name, "#DEFAULT");
    }

    @Override
    public String toString() {
        List<String> list = asList();
        if (list.size() == 0) {
            return "#DEFAULT";
        }
        return StringUtils.join(list, ".", new StringUtils.StringUtilsFormatter<String>() {
            @Override
            public String toString(String obj) {
                return StringUtils.defaultIfEmpty(obj, "#DEFAULT");
            }
        });
    }

    @Override
    public int compareTo(ObjectName o) {
        return this.name.compareTo(o.name);
    }

    public boolean equalsIgnoreCase(ObjectName name) {
        return this.name.equalsIgnoreCase(name.name);
    }

    public boolean equals(ObjectReference objectReference) {
        return this.equals(objectReference.objectName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ObjectName && toString().equals(obj.toString());
    }

    public boolean equals(ObjectName obj, boolean ignoreLengthDifferences) {
        if (ignoreLengthDifferences) {
            List<String> thisNames = this.asList();
            List<String> otherNames = obj.asList();
            int precision = Math.min(thisNames.size(), otherNames.size());

            thisNames = thisNames.subList(thisNames.size() - precision, thisNames.size());
            otherNames = otherNames.subList(otherNames.size() - precision, otherNames.size());

            for (int i=0; i<thisNames.size(); i++) {
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
            return this.equals(obj);
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
            return Collections.unmodifiableList(list.subList(list.size()-length, list.size()));
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
        ObjectName name = this;
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


}
