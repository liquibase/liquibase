package liquibase.sdk.verifytest;

import liquibase.database.Database;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sql.Sql;
import liquibase.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class OutputFormat {


    public static final OutputFormat DefaultFormat = new OutputFormat.DefaultFormat();
    public static final OutputFormat FromFile = new OutputFormat.FromFileFormat();

    public abstract String format(Object value);

    private static class DefaultFormat extends OutputFormat {
        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }

            if (value instanceof Class) {
                return ((Class) value).getName();
            }

            if (value instanceof Database) {
                return ((Database) value).getShortName();
            }

            if (value instanceof Sql) {
                return ((Sql) value).toSql();
            }

            if (value instanceof Object[]) {
                return new ArrayFormat(this).format(value);
            }

            if (value instanceof Collection) {
                return new CollectionFormat(this).format(value);
            }

            if (value instanceof TestPermutation.Value) {
                return ((TestPermutation.Value) value).serialize();
            }

            if (value instanceof LiquibaseSerializable) {
                Map<String, String> map = new HashMap<String, String>();
                for (String field : ((LiquibaseSerializable) value).getSerializableFields()) {
                    Object serializedValue = ((LiquibaseSerializable) value).getSerializableFieldValue(field);
                    if (serializedValue != null) {
                        map.put(field, serializedValue.toString());
                    }
                }
                return StringUtils.join(map, ",");
            }

            return value.toString();
        }
    }

    private static class FromFileFormat extends OutputFormat {
        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }

            if (value instanceof TestPermutation.Value) {
                return ((TestPermutation.Value) value).serialize();
            }

            return (String) value;
        }
    }

    public static class ArrayFormat extends OutputFormat {

        private StringUtils.StringUtilsFormatter itemFormatter;

        public ArrayFormat(final OutputFormat itemFormatter) {
            this.itemFormatter = new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    return itemFormatter.format(obj);
                }
            };
        }

        @Override
        public String format(Object value) {
            return StringUtils.join((Object[]) value, ", ", itemFormatter);
        }
    }

    public static class CollectionFormat extends OutputFormat {

        private StringUtils.StringUtilsFormatter itemFormatter;

        public CollectionFormat(final OutputFormat itemFormatter) {
            this.itemFormatter = new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    return itemFormatter.format(obj);
                }
            };
        }

        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }
            return StringUtils.join(((Collection) value), ", ", itemFormatter);
        }
    }

    static class OutputData {
        Object value;
        OutputFormat formatter;

        OutputData(Object value, OutputFormat formatter) {
            this.value = value;
            if (formatter == null) {
                formatter = DefaultFormat;
            }

            this.formatter = formatter;
        }

        @Override
        public String toString() {
            return formatter.format(value);
        }
    }
}
