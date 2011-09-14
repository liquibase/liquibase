package liquibase.change;

import java.util.List;

public interface ChangeWithColumns<T extends ColumnConfig> {
    public void addColumn(T column);

    public List<T> getColumns();
}
