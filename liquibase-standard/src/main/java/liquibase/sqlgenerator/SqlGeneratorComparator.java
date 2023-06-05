package liquibase.sqlgenerator;

import java.util.Comparator;

class SqlGeneratorComparator implements Comparator<SqlGenerator> {
    @Override
    public int compare(SqlGenerator o1, SqlGenerator o2) {
        return -1 * Integer.compare(o1.getPriority(), o2.getPriority());
    }
}
