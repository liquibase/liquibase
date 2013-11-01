package liquibase.structure;

import liquibase.util.StringUtils;

import java.util.Comparator;

public class DatabaseObjectComparator implements Comparator<DatabaseObject> {

    @Override
    public int compare(DatabaseObject o1, DatabaseObject o2) {
        String name1 = StringUtils.trimToEmpty(o1.getName());
        String name2 = StringUtils.trimToEmpty(o2.getName());

        int i = name1.compareTo(name2);
        if (i == 0) {
            return StringUtils.trimToEmpty(o1.toString()).compareTo(StringUtils.trimToEmpty(o2.toString()));
        }

        return i;
    }
}
