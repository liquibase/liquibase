package liquibase.serializer.core.yaml;

import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtils;

import java.io.*;
import java.util.*;

public class YamlChangeLogSerializer extends YamlSerializer implements ChangeLogSerializer {

    protected Comparator<String> getComparator(LiquibaseSerializable object) {
        if (object instanceof ChangeSet) {
            return new ChangeSetComparator();
        } else {
            return super.getComparator(object);
        }
    }

    @Override
    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("databaseChangeLog:\n");
        for (T child : children) {
            writer.write(StringUtils.indent(serialize(child, true), 2));
            writer.write("\n");
        }
        writer.flush();
    }


    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }



    private static class ChangeSetComparator implements Comparator<String> {
        private static final Map<String, Integer> order = new HashMap<String, Integer>();

        static {
            order.put("id", 1);
            order.put("author", 2);
            order.put("changes", Integer.MAX_VALUE);
        }

        @Override
        public int compare(String o1, String o2) {
            Integer o1Order = order.get(o1);
            if (o1Order == null) {
                o1Order = 10;
            }

            Integer o2Order = order.get(o2);
            if (o2Order == null) {
                o2Order = 10;
            }

            int orderCompare = o1Order.compareTo(o2Order);

            if (orderCompare == 0) {
                return o1.compareTo(o2);
            }
            return orderCompare;
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
