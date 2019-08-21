package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.diff.compare.CompareControl;
import liquibase.util.ObjectUtil;

import java.util.Arrays;

public abstract class AbstractChangeGenerator implements ChangeGenerator {
    private boolean respectSchemaAndCatalogCase = false;

    @Override
    public Change[] fixSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        if ((changes == null) || (this instanceof UnexpectedObjectChangeGenerator)) {
            return changes;
        }
        for (Change change : changes) {
            for (String field : change.getSerializableFields()) {
                if (field.toLowerCase().contains("schemaname") || field.toLowerCase().contains("catalogname")) {
                    Object value = change.getSerializableFieldValue(field);
                    if ((value != null) && (value instanceof String)) {
                        String newValue = CompareControl.SchemaComparison.convertSchema((String) value, schemaComparisons);
                        if ((newValue != null) && !newValue.equalsIgnoreCase((String) value)) {
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
                    if ((schemaComparisons != null) && (value != null) && (value instanceof String)) {
                        for (CompareControl.SchemaComparison comparison : schemaComparisons) {
                            if (!respectSchemaAndCatalogCase) {
                                setPropertyIgnoreSchemaAndCatalogCase(change, field, (String)value, comparison);
                            } else {
                                setProperty(change, field, (String)value, comparison);
                            }
                        }
                    }
                }
            }
        }
        return changes;
    }

    /**
     *
     * Split a dot-annotated attribute value
     * Called only for schemaName and catalogName attributes
     * We fix up the string before the split to avoid
     * issues with '.' characters occurring in a property
     * We replace the string '#|#' with the single '.'
     * afterwards, and then split.
     *
     * @param  field     The attribute field to set
     * @param  value     The value to set
     * @return String    The new value
     *
     */
    private String valueToSet(String field, String value) {
        if (! (field.toLowerCase().contains("schemaname") || field.toLowerCase().contains("catalogname"))) {
            return value;
        }
        String doctored = doctor(value);
        if (! doctored.contains(".")) {
            return value;
        }
        String valueToSet;
        String[] parts = doctored.split("\\.");
        if (field.toLowerCase().contains("schemaname")) {
           valueToSet = parts[1].replaceAll("#\\|#",".");
        }
        else {
           valueToSet = parts[0].replaceAll("#\\|#",".");
        }
        return valueToSet;
    }

    /**
     *
     * For an input String, replace any '.'
     * characters which occur inside a property
     * expression like ${env.tag}.  By doing this,
     * correctly split the entire value on '.'.
     *
     * @param  stringToDoctor   The string to fix up
     * @return String

     */
    private String doctor(String stringToDoctor) {
        boolean startProp = false;
        int pos = 0;
        char[] chars = stringToDoctor.toCharArray();
        char[] output = new char[chars.length];
        for (int i=0;  i < chars.length; i++) {
          if (chars[i] == '$' && chars[i+1] == '{') {
            startProp = true;
            output[pos] = '$';
            output[pos+1] = '{';
            i += 1;
            pos += 2;
            continue;
          }
          if (startProp) {
            if (chars[i] == '}') {
              startProp  = false;
            }
            else {
              if (chars[i] == '.') {
                char[] newOutput = Arrays.copyOf(output, output.length+2);
                newOutput[pos] = '#';
                newOutput[pos+1] = '|';
                newOutput[pos+2] = '#';
                pos += 3;
                output = newOutput;
                continue;
              }
            }
          }
          output[pos] = chars[i];
          pos++;
        }
        String edited = new String(output);
        return edited;
    }

    private void setPropertyIgnoreSchemaAndCatalogCase(Change change, String field, String value, CompareControl.SchemaComparison comparison) {

        if (comparison.getOutputSchemaAs() != null && comparison.getComparisonSchema() != null
                && (comparison.getComparisonSchema().getSchemaName().equalsIgnoreCase(value)
                || comparison.getComparisonSchema().getCatalogName().equalsIgnoreCase(value))) {
            String newValue = valueToSet(field, comparison.getOutputSchemaAs());
            if (field.toLowerCase().equalsIgnoreCase("catalogname")) {
                if (!newValue.equalsIgnoreCase(comparison.getOutputSchemaAs())) {
                    ObjectUtil.setProperty(change, field, newValue);
                }
            } else {
                ObjectUtil.setProperty(change, field, newValue);
            }

        }
    }

    private void setProperty(Change change, String field, String value, CompareControl.SchemaComparison comparison) {
        if (comparison.getOutputSchemaAs() != null && comparison.getComparisonSchema() != null
                && (comparison.getComparisonSchema().getSchemaName().equals(value)
                || comparison.getComparisonSchema().getCatalogName().equals(value))) {
            String newValue = valueToSet(field, comparison.getOutputSchemaAs());
            if (field.toLowerCase().equalsIgnoreCase("catalogname")) {
                if (!newValue.equals(comparison.getOutputSchemaAs())) {
                    ObjectUtil.setProperty(change, field, newValue);
                }
            } else {
                ObjectUtil.setProperty(change, field, newValue);
            }
        }
    }

    public void setRespectSchemaAndCatalogCase(boolean respectSchemaAndCatalogCase) {
        this.respectSchemaAndCatalogCase = respectSchemaAndCatalogCase;
    }
}
