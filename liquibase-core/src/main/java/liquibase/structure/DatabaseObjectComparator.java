package liquibase.structure;

import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.Comparator;

public class DatabaseObjectComparator implements Comparator<DatabaseObject> {

    @Override
    public int compare(DatabaseObject o1, DatabaseObject o2) {
        Schema schema1 = o1.getSchema();
        Schema schema2 = o2.getSchema();

        if ((schema1 != null) && (schema2 != null)) {
            int i = schema1.toString().compareTo(schema2.toString());
            if (i != 0) {
                return i;
            }

        }

        String name1 = StringUtils.trimToEmpty(o1.getName());
        String name2 = StringUtils.trimToEmpty(o2.getName());

        int i = name1.compareTo(name2);
        if (i == 0) {
            return StringUtils.trimToEmpty(o1.toString()).compareTo(StringUtils.trimToEmpty(o2.toString()));
        }

        return i;
    }
}
