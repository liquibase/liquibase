package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class is used by other classes to filter the set of database objects used
 * in diff-type operations including the diff command and the generateChangeLog
 * command.
 *
 * There are two basic types of filter - FilterType.INCLUDE and FilterType.EXCLUDE.
 * In Each filter type, a filter string can be supplied. That string is a
 * comma-separated list of subfilters. Each subfilter can either be a regular expression,
 * or a Database object type followed by a colon and then a regular expression.
 *
 */
public class StandardObjectChangeFilter implements ObjectChangeFilter {

    private FilterType filterType;

    private List<Filter> filters = new ArrayList<>();
    private boolean catalogOrSchemaFilter;

    public StandardObjectChangeFilter(FilterType type, String filter) {
        this.filterType = type;
        parseFilter(filter);
    }

    protected void parseFilter(String filter) {
        filter = StringUtil.trimToNull(filter);
        if (filter == null) {
            return;
        }

        // first, split the string on commas to get the subfilters
        for (String subfilter : filter.split("\\s*,\\s*")) {
            // each subfilter can be either "objecttype:regex" or just "regex", so split on colon to decide
            String[] split = subfilter.split(":");
            if (split.length == 1) {
                filters.add(new Filter(null, Pattern.compile(split[0])));
            } else {
                String className = StringUtil.upperCaseFirst(split[0]);
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

    /**
     * The Filter class is used internally to do the actual work. A Filter consists of
     * an objectType and a regex Pattern.
     *
     * The main method is matches(), which returns true if the given DatabaseObject
     * matches the filter, and false if it does not match the Filter.
     *
     * If the objectType is null, then just the Pattern is used to compare the "name" of
     * the object whether it matches or not.
     *
     * If the objectType is not null, then the objectType of the Filter must be
     * assignableFrom the given DatabaseObject, AND the "name" of the DatabaseObject
     * must match the Pattern.
     *
     * The "name" of the object might be what is returned from getName(), or it might
     * be a different 'identifier' for different objet types.
     */
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
                    return matches(((Index) object).getRelation());
                }
                if (object instanceof ForeignKey) {
                    return matches(((ForeignKey) object).getForeignKeyTable());
                }
                if (object instanceof PrimaryKey) {
                    return matches(((PrimaryKey) object).getTable());
                }
                if (object instanceof UniqueConstraint) {
                    return matches(((UniqueConstraint) object).getRelation());
                }
                if (object instanceof Data) {
                    return matches(((Data) object).getTable());
                }
            }
            return matches;
        }
    }
}
