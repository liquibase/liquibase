package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StandardObjectChangeFilter implements ObjectChangeFilter {

    private FilterType filterType;

    private List<Filter> filters = new ArrayList<>();
    private boolean catalogOrSchemaFilter;

    public StandardObjectChangeFilter(FilterType type, String filter) {
        this.filterType = type;
        parseFilter(filter);
    }

    protected void parseFilter(String filter) {
        filter = StringUtils.trimToNull(filter);
        if (filter == null) {
            return;
        }

        for (String subfilter : filter.split("\\s*,\\s*")) {
            String[] split = subfilter.split(":");
            if (split.length == 1) {
                filters.add(new Filter(null, Pattern.compile(split[0])));
            } else {
                String className = StringUtils.upperCaseFirst(split[0]);
                className = "liquibase.structure.core."+className;
                try {
                    Class<DatabaseObject> clazz = (Class<DatabaseObject>) Class.forName(className);
                    filters.add(new Filter(clazz, Pattern.compile(split[1])));
                    catalogOrSchemaFilter |= "Catalog".equals(className) || "Schema".equals(className);
                } catch (ClassNotFoundException e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            }
        }
    }

    @Override
    public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
    }

    @Override
    public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
    }

    @Override
    public boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
    }

    @Override
    public boolean include(DatabaseObject object) {
        if (filters.isEmpty()) {
            return true;
        }
        for (Filter filter : filters) {
            if (filter.matches(object)) {
                if (filterType == FilterType.INCLUDE) {
                    return true;
                }
                if (filterType == FilterType.EXCLUDE) {
                    return false;
                }
            }
        }
        // Assumes that if no filter is specified for a catalog or schema all should be accepted.
        return (filterType == FilterType.EXCLUDE) || (((object instanceof Catalog) || (object instanceof Schema)) &&
            !catalogOrSchemaFilter);
    }

    public enum FilterType {
        INCLUDE,
        EXCLUDE,
    }

    protected static class Filter {

        private Class<DatabaseObject> objectType;
        private Pattern nameMatcher;

        public Filter(Class<DatabaseObject> objectType, Pattern nameMatcher) {
            this.objectType = objectType;
            this.nameMatcher = nameMatcher;
        }

        protected boolean matches(DatabaseObject object) {
            if (object == null) {
                return false;
            }

            Boolean matches = null;
            if ((objectType != null) && !objectType.isAssignableFrom(object.getClass())) {
                matches = false;
            }
            if (matches == null) {
                matches = (object.getName() != null) && nameMatcher.matcher(object.getName()).matches();
            }

            if (!matches) {
                if (object instanceof Column) {
                    return matches(((Column) object).getRelation());
                }
                if (object instanceof Index) {
                    return matches(((Index) object).getTable());
                }
                if (object instanceof ForeignKey) {
                    return matches(((ForeignKey) object).getForeignKeyTable());
                }
                if (object instanceof PrimaryKey) {
                    return matches(((PrimaryKey) object).getTable());
                }
                if (object instanceof UniqueConstraint) {
                    return matches(((UniqueConstraint) object).getTable());
                }
                if (object instanceof Data) {
                    return matches(((Data) object).getTable());
                }
            }
            return matches;
        }
    }
}
