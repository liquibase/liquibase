package liquibase.ext.changewithnestedtags;

public class SampleGrandChild {
    private String columnName;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public SampleChild createGreatGrandChild() {
        return new SampleChild();
    }
}
