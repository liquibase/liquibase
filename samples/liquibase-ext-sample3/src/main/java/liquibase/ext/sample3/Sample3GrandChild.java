package liquibase.ext.sample3;

public class Sample3GrandChild {
    private String columnName;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Sample3Child createGreatGrandChild() {
        return new Sample3Child();
    }
}
