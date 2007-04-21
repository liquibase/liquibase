package liquibase.database.struture;

import java.sql.Connection;

public class Sequence implements DatabaseStructure {

    private String name;
    private Integer incrementBy;
    private Integer minValue;
    private Integer maxValue;

    public Sequence(String name, Integer incrementBy, Integer minValue, Integer maxValue) {
        this.name = name;
        this.incrementBy = incrementBy;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getName() {
        return name;
    }

    public Integer getIncrementBy() {
        return incrementBy;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return getName().equals(((Sequence) obj).getName());
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public int compareTo(Object o) {
        if (o instanceof Sequence) {
            return toString().compareTo(o.toString());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    public Connection getConnection() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
