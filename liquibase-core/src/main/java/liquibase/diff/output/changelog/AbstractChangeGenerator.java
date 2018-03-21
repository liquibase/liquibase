package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.diff.compare.CompareControl;
import liquibase.util.ObjectUtil;

public abstract class AbstractChangeGenerator implements ChangeGenerator {
    private boolean respectSchemaAndCatalogCase = false;

    @Override
    public Change[] fixSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        if (changes == null || this instanceof UnexpectedObjectChangeGenerator) {
            return changes;
        }
        for (Change change : changes) {
            for (String field : change.getSerializableFields()) {
                if (field.toLowerCase().contains("schemaname") || field.toLowerCase().contains("catalogname")) {
                    Object value = change.getSerializableFieldValue(field);
                    if (value != null && value instanceof String) {
                        String newValue = CompareControl.SchemaComparison.convertSchema((String) value, schemaComparisons);
                        if (newValue != null && !newValue.equalsIgnoreCase((String) value)) {
                            ObjectUtil.setProperty(change, field, newValue);
                        }
                    }
                }
            }
        }
        return changes;
    }

    @Override
    public Change[] fixOutputAsSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        if (changes == null) {
            return null;
        }
        for (Change change : changes) {
            for (String field : change.getSerializableFields()) {
                if (field.toLowerCase().contains("schemaname") || field.toLowerCase().contains("catalogname")) {
                    Object value = change.getSerializableFieldValue(field);
                    if (schemaComparisons != null && value != null && value instanceof String) {
                        for (CompareControl.SchemaComparison comparison : schemaComparisons) {
                            if (!respectSchemaAndCatalogCase) {
                                setPropertyIgnoreSchemaAndCatalogCase(change, field, (String) value, comparison);
                            } else {
                                setProperty(change, field, (String) value, comparison);
                            }
                        }
                    }
                }
            }
        }
        return changes;
    }

    private void setPropertyIgnoreSchemaAndCatalogCase(Change change, String field, String value, CompareControl.SchemaComparison comparison) {
        if (comparison.getOutputSchemaAs() != null
                && comparison.getComparisonSchema() != null
                && (comparison.getComparisonSchema().getSchemaName().equalsIgnoreCase(value)
                || comparison.getComparisonSchema().getCatalogName().equalsIgnoreCase(value))
                ) {
            ObjectUtil.setProperty(change, field, comparison.getOutputSchemaAs());
        }
    }

    private void setProperty(Change change, String field, String value, CompareControl.SchemaComparison comparison) {
        if (comparison.getOutputSchemaAs() != null
                && comparison.getComparisonSchema() != null
                && (comparison.getComparisonSchema().getSchemaName().equals(value)
                || comparison.getComparisonSchema().getCatalogName().equals(value))
                ) {
            ObjectUtil.setProperty(change, field, comparison.getOutputSchemaAs());
        }
    }

    public void setRespectSchemaAndCatalogCase(boolean respectSchemaAndCatalogCase) {
        this.respectSchemaAndCatalogCase = respectSchemaAndCatalogCase;
    }
}
