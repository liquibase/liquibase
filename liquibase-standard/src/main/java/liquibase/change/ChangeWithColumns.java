package liquibase.change;

import java.util.List;

/**
 * Markers a Change class as containing one or more {@link ColumnConfig} configuration.
 */
public interface ChangeWithColumns<T extends ColumnConfig> {

    /**
     * Add a column configuration to the Change.
     */
    void addColumn(T column);

    /**
     * Return all the {@link ColumnConfig} objects defined for this {@link Change}
     */
    List<T> getColumns();

    void setColumns(List<T> columns);
}
