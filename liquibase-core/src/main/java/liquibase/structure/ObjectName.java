package liquibase.structure;

import liquibase.AbstractExtensibleObject;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.util.StringUtils;

public class ObjectName extends AbstractExtensibleObject {

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
            set(Attr.name, names[names.length-1]);
            set(Attr.container, container);
        }
    }

    public String getName() {
        return get(Attr.name, String.class);
    }

    public ObjectName getContainer() {
        return get(Attr.container, ObjectName.class);
    }

    public String toShortString() {
        return StringUtils.defaultIfEmpty(get(Attr.name, String.class), "#DEFAULT");
    }

    @Override
    public String toString() {
        String name = StringUtils.defaultIfEmpty(get(Attr.name, String.class), "#DEFAULT");
        ObjectName container = get(Attr.container, ObjectName.class);
        while (container != null) {
            name = StringUtils.defaultIfEmpty(container.get(Attr.name, String.class), "#DEFAULT") + "." + name;
            container = container.get(Attr.container, ObjectName.class);
        }

        return name;
    }
}
