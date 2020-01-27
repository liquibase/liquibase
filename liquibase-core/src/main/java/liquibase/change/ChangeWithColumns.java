package liquibase.change;

import liquibase.exception.ValidationErrors;
import liquibase.util.StringUtils;

import java.util.List;

/**
 * Markers a Change class as containing one or more {@link ColumnConfig} configuration.
 */
public interface ChangeWithColumns<T extends ColumnConfig> {

    /**
     * Add a column configuration to the Change.
     */
    default void addColumn(T column) { getColumns().add(column); }

    /**
     * Return all the {@link ColumnConfig} objects defined for this {@link Change }
     */
    List<T> getColumns();

    void setColumns(List<T> columns);

    /**
     * Unique string for the column for better identification
     * @param index index of the column
     * @param columnConfig the column
     * @return
     */
    default String columnIDString(int index, T columnConfig) {
        return " / column[" + index + "]" +
                (StringUtils.trimToNull(columnConfig.getName()) != null ?
        " (name:'" + columnConfig.getName() + "')" : "") ;
    }

    /**
     * Validate all columns and collect errors in 'validationErrors'
     * @param validationErrors ValidationErrors to collect errors
     * @return validationErrors
     */
    default ValidationErrors validateColumns(ValidationErrors validationErrors) {
        if (getColumns() != null) {
            int i = 1;
            for (T columnConfig : getColumns()) {
                validateColumn(columnConfig, validationErrors, columnIDString(i, columnConfig));
                i++;
            }
        }
        return validationErrors;
    }

    /**
     * Validate a column. Validate the 'name' by default
     * @param columnConfig column to validate
     * @param validationErrors ValidationErrors to collect errors
     * @param columnIDString Unique string for the column
     */
    default void validateColumn(T columnConfig, ValidationErrors validationErrors, String columnIDString) {
        validationErrors.checkRequiredField( "name", columnConfig.getName(), columnIDString);
    }
}
