package liquibase.sdk.verifytest;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.util.StringUtils;

import java.util.Collection;

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

            return value.toString();
        }
    }

    private static class FromFileFormat extends OutputFormat {
        @Override
        public String format(Object value) {
            if (value == null) {
                return null;
            }

            return (String) value;
        }
    }

    public static class CollectionFormat extends OutputFormat {

        private StringUtils.StringUtilsFormatter itemFormatter;

        public CollectionFormat() {
            this.itemFormatter = new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    return (String) obj;
                }
            };
        }

        public CollectionFormat(StringUtils.StringUtilsFormatter itemFormatter) {
            this.itemFormatter = itemFormatter;
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
