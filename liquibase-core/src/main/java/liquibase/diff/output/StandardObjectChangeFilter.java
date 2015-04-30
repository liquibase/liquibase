package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.UniqueConstraint;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StandardObjectChangeFilter implements ObjectChangeFilter {

    private FilterType filterType;

    private List<Filter> filters = new ArrayList<Filter>();

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
                filters.add(new Filter(null, Pattern.compile(split[0]), filterType));
            } else {
                String className = StringUtils.upperCaseFirst(split[0]);
                className = "liquibase.structure.core."+className;
                try {
                    Class<DatabaseObject> clazz = (Class<DatabaseObject>) Class.forName(className);
                    filters.add(new Filter(clazz, Pattern.compile(split[1]), filterType));
                } catch (ClassNotFoundException e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            }
        }
    }

    @Override
    public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object, referenceDatabase, comparisionDatabase);
    }

    @Override
    public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object, referenceDatabase, comparisionDatabase);
    }

    @Override
    public boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database comparisionDatabase) {
        return include(object, referenceDatabase, comparisionDatabase);
    }

    protected boolean include(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        if (filters.size() == 0) {
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
        return filterType == FilterType.EXCLUDE;
    }

    protected static class Filter {

        private final FilterType filterType;
        private Class<DatabaseObject> objectType;
        private Pattern nameMatcher;

        public Filter(Class<DatabaseObject> objectType, Pattern nameMatcher, FilterType filterType) {
            this.objectType = objectType;
            this.nameMatcher = nameMatcher;
            this.filterType = filterType;
        }

        protected boolean matches(DatabaseObject object) {
            if (object == null) {
                return false;
            }

            Boolean matches = null;
            if (objectType != null && !objectType.isAssignableFrom(object.getClass())) {
                matches = false;
            }
            if (matches == null) {
                matches = nameMatcher.matcher(object.getName()).matches();
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
                if (object instanceof Data) {
                    return matches(((Data) object).getTable());
                }
            }
            return matches;
        }
    }

    public static enum FilterType {
        INCLUDE,
        EXCLUDE,
    }
}
