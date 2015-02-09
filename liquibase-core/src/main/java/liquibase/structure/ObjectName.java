package liquibase.structure;

import liquibase.AbstractExtensibleObject;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectName extends AbstractExtensibleObject implements Comparable<ObjectName> {

    public static enum Attr {
        name,
        container
    }

    public ObjectName(String name, ObjectName container) {
        set(Attr.name, name);
        set(Attr.container, container);
    }

    public ObjectName(String... names) {
        if (names == null || names.length == 0) {
            set(Attr.name, null);
        } else {
            ObjectName container = null;
            for (int i = 0; i < names.length - 1; i++) {
                container = new ObjectName(names[i], container);
            }
            set(Attr.name, names[names.length - 1]);
            set(Attr.container, container);
        }
    }

    public String getName() {
        return get(Attr.name, String.class);
    }

    public List<String> getNameList() {
        List<String> list = new ArrayList<>();
        list.add(this.getName());
        ObjectName container = this.getContainer();
        while (container != null) {
            list.add(0, container.getName());
            container = container.getContainer();
        }
        return Collections.unmodifiableList(list);
    }


    public ObjectName getContainer() {
        return get(Attr.container, ObjectName.class);
    }

    public String toShortString() {
        return StringUtils.defaultIfEmpty(get(Attr.name, String.class), "#DEFAULT");
    }

    @Override
    public String toString() {
        return StringUtils.join(getNameList(), ".", new StringUtils.StringUtilsFormatter<String>() {
            @Override
            public String toString(String obj) {
                return StringUtils.defaultIfEmpty(obj, "#DEFAULT");
            }
        });
    }

    @Override
    public int compareTo(ObjectName o) {
        return this.getName().compareTo(o.getName());
    }

    public boolean equalsIgnoreCase(ObjectName name) {
        return this.getName().equalsIgnoreCase(name.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ObjectName && toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
