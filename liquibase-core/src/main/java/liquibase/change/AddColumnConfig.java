package liquibase.change;


public class AddColumnConfig extends ColumnConfig {

    private String afterColumn;
    private String beforeColumn;
    private Integer position;

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public String getBeforeColumn() {
        return beforeColumn;
    }

    public void setBeforeColumn(String beforeColumn) {
        this.beforeColumn = beforeColumn;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
