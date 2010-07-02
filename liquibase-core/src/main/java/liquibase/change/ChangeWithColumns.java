package liquibase.change;

import java.util.List;

public interface ChangeWithColumns {
    public void addColumn(ColumnConfig column);

    public List<ColumnConfig> getColumns();
}
