package liquibase.database.struture;

import java.sql.Connection;

public class Index implements DatabaseStructure {
    public int compareTo(Object o) {
        if (o instanceof Index) {
            return toString().compareTo(o.toString());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    /**
     * TODO: Implement
     */
    public Connection getConnection() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
