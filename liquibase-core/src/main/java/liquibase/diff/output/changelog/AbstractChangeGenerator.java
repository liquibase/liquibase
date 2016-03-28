package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.diff.compare.CompareControl;
import liquibase.util.ObjectUtil;

public abstract class AbstractChangeGenerator implements ChangeGenerator {

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
}
