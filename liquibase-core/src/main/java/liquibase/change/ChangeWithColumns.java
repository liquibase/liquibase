package liquibase.change;

public interface ChangeWithColumns {
    public void addColumn(ColumnConfig column);
}
